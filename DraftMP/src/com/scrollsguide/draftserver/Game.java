package com.scrollsguide.draftserver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.messages.ChatMessage;
import com.scrollsguide.draftserver.messages.CreatorDisconnectMessage;
import com.scrollsguide.draftserver.messages.DeckMessage;
import com.scrollsguide.draftserver.messages.FinalizePickMessage;
import com.scrollsguide.draftserver.messages.GameFinishedMessage;
import com.scrollsguide.draftserver.messages.GameInfoMessage;
import com.scrollsguide.draftserver.messages.JoinMessage;
import com.scrollsguide.draftserver.messages.Message;
import com.scrollsguide.draftserver.messages.PackMessage;
import com.scrollsguide.draftserver.messages.PartLobbyMessage;
import com.scrollsguide.draftserver.messages.PlayerListMessage;
import com.scrollsguide.draftserver.messages.PlayerPickMessage;
import com.scrollsguide.draftserver.messages.RoundFinishedMessage;
import com.scrollsguide.draftserver.messages.StartGameMessage;

public class Game {

	private final String name;
	private final String id;

	private final int maxplayers;
	private final String password;
	private final int bots;
	private int timeout;

	private DraftServer server;

	private final int numPacks;

	private ScrollList scrolls;

	private boolean started = false;
	private boolean finished = false;

	private Pack[][] packs; // [PACKS][players.size()]
	private final List<Player> players = new CopyOnWriteArrayList<Player>();
	private boolean[] hasPicked;
	private Scroll[] picks;

	private HashMap<String, Deck> decks = new HashMap<String, Deck>();
	// private Deck[] decks;

	private int totalPicked = 0; // total number of players that have picked a scroll
	private int currentRound = 0;
	private int packOffset = 0;
	private int packDirection = -1; // start going downwards

	public Game(HumanPlayer creator, DraftServer server, ScrollList scrolls, String id, String name, int packs,
			int maxplayers, int bots, int timeout, String password) {
		this.scrolls = scrolls;
		this.name = name;
		this.id = id;
		this.numPacks = Math.min(Math.max(1, packs), Settings.MAX_PACKS);
		this.maxplayers = Math.min(Math.max(2, maxplayers), Settings.MAX_PLAYERS);
		this.password = password;
		this.bots = bots;
		this.timeout = Math.min(Math.max(1, timeout), Settings.MAX_TIMEOUT);
		this.server = server;

		players.add(creator);

		sendGameInfo(creator);

		broadcastPlayerList();
	}

	public void initBots() {
		// add bots
		Random r = new Random();
		int idOffset = r.nextInt(Settings.RESOURCES.length); // so playing with 1 bot doesn't always play the same resource
		for (int i = 0; i < bots; i++) {
			BotPlayer bp = new BotPlayer(server, scrolls, i + idOffset);
			bp.setUsername("Bot" + (i + 1));

			bp.inGame = server.gameJoin(bp, id, password);
		}
	}

	public String getName() {
		return this.name;
	}

	public String getID() {
		return this.id;
	}

	public Player getCreator() {
		return players.get(0);
	}

	public void playerJoins(Player p) {
		broadcast(new JoinMessage(p.getName(),  this.getName()));

		players.add(p);

		sendGameInfo(p);

		broadcastPlayerList();
	}

	private void sendGameInfo(Player p) {
		p.send(new GameInfoMessage(name, numPacks, timeout, p == getCreator()));
	}

	private void broadcastPlayerList() {
		broadcast(new PlayerListMessage(PlayerListMessage.SCOPE.GAME, players));
	}

	public void creatorDC() {
		broadcast(new CreatorDisconnectMessage());

		for (Player p : players) {
			if (getCreator() != p) {
				p.partFromGames();
			}
			if (p.isBot) {
				p = null;
			}
		}
	}

	/**
	 * Starts the first round, notifies players of start. Fails when there are not enough players
	 */
	public void start() {
		// is the game already started?
		if (this.isStarted()){
			return;
		}
		if (players.size() < 2) {
			getCreator().sendError("You can't start the game with less than 2 players.");
		} else {
			started = true;
			broadcast(new StartGameMessage());

			picks = new Scroll[players.size()];
			// decks = new Deck[players.size()];
			for (int i = 0; i < players.size(); i++) {
				decks.put(players.get(i).getName(), new Deck());
			}

			preparePacks();

			round(); // real start
		}
	}

	private void round() {
		Arrays.fill(picks, null); // empty picks
		hasPicked = new boolean[players.size()];
		totalPicked = 0;
		packOffset += packDirection;

		for (int i = 0; i < players.size(); i++) {
			int packIndex = getPackIndex(i);
			System.err.println("PackIndex: " + packIndex);
			sendPackToPlayer(players.get(i), packIndex);
		}
	}

	private int getPackIndex(int playerIndex) {
		int packIndex = (playerIndex + packOffset) % players.size();
		if (packIndex < 0) {
			packIndex += players.size();
		}
		return packIndex;
	}

	private void sendPackToPlayer(Player player, int packIndex) {
		Pack pack = packs[currentRound][packIndex];
		
		player.send(new PackMessage(pack, packIndex + 1, currentRound + 1, timeout));
	}

	private void preparePacks() {
		packs = new Pack[numPacks][players.size()];

		for (int i = 0; i < numPacks; i++) {
			for (int j = 0; j < players.size(); j++) {
				Pack p = new Pack(this);
				p.init(7, 2, 1);
				packs[i][j] = p;

				// p.print(); // debug
			}
		}
	}

	public void broadcast(Message msg) {
		if (players != null) {
			for (Player p : players) {
				if (!p.isBot) {
					p.send(msg);
				}
			}
		}
	}

	public Scroll getRandomScroll(int rarity) {
		Random r = new Random();
		Scroll s;
		do {
			s = scrolls.get(r.nextInt(scrolls.size()));
		} while (s.getRarity() != rarity);
		return s;
	}

	public synchronized void pickScroll(Player pl, int scroll) {
		int playerIndex = players.indexOf(pl);

		System.err.println(pl.getName() + " picked " + scroll);
		Scroll picked = getScrollFromId(scroll);
		picks[playerIndex] = picked;

		System.err.println(pl.getName() + " picked " + picked.getName());

		if (!hasPicked[playerIndex]) {
			hasPicked[playerIndex] = true;
			broadcast(new PlayerPickMessage(pl));

			totalPicked++;

			if (totalPicked == players.size()) { // every player has played
				finalizePicks();
				cyclePacks();
			}
		}
	}

	public void finalizePicks() {
		for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) {
			Scroll picked = picks[playerIndex];

			int packIndex = getPackIndex(playerIndex);
			if (!packs[currentRound][packIndex].contains(picked)) {
				System.err.println("Pack " + packIndex + " for player " + players.get(playerIndex).getName()
						+ " doesn't contain " + picked.getId() + " " + picked.getName() + "!!!");
			} else {
				packs[currentRound][packIndex].remove(picked);
				decks.get(players.get(playerIndex).getName()).add(picked);
			}

			players.get(playerIndex).send(new FinalizePickMessage(picked));
		}
	}

	private void cyclePacks() {
		if (packs[currentRound][0].size() > 0) { // Still scrolls remain in this pack
			round();
		} else {
			if (currentRound < numPacks - 1) {
				packOffset = 0;
				packDirection = -packDirection; // switch direction
				currentRound++;

				broadcast(new RoundFinishedMessage(currentRound, numPacks, packDirection));

				round();
			} else { // all done! :D
				gameFinished();
			}
		}
	}

	private void gameFinished() {
		finished = true;
		broadcast(new GameFinishedMessage());

		broadcastPlayerList();
	}

	private Scroll getScrollFromId(int id) {
		for (Scroll s : scrolls) {
			if (s.getId() == id) {
				return s;
			}
		}
		return null;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean part(Player player) {
		if (players.contains(player)) {
			players.remove(player);
			broadcast(new PartLobbyMessage(player, this));
			broadcastPlayerList();
			return true;
		}
		return false;
	}

	public int getPlayerCount() {
		return players.size();
	}

	public boolean hasPassword() {
		return !password.equals("");
	}

	public String getPassword() {
		return password;
	}

	public int getMaxPlayers() {
		return maxplayers;
	}

	public boolean isFull() {
		return players.size() == maxplayers;
	}

	/**
	 * Sends deck from player playerName to player player (can only be called after finished = true)
	 * @param player to send the deck to
	 * @param playerName
	 */
	public void sendDeck(Player player, String playerName) {
		if (!decks.containsKey(playerName)) {
			player.sendError("Player " + playerName + " not found in the current game");
		} else {
			player.send(new DeckMessage(playerName, decks.get(playerName)));
		}
	}
}
