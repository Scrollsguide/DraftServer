package com.scrollsguide.draftserver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		broadcast("{\"msg\":\"join\", \"d\": \"" + p.getName() + "\",\"f\":\"" + this.getName() + "\"}");

		players.add(p);

		sendGameInfo(p);

		broadcastPlayerList();
	}

	private void sendGameInfo(Player p) {
		try {
			JSONObject toPlayer = new JSONObject();
			toPlayer.put("msg", "jg");
			toPlayer.put("d", name);
			toPlayer.put("p", numPacks);
			toPlayer.put("t", timeout);
			if (p == getCreator()) {
				toPlayer.put("cr", true);
			} else {
				toPlayer.put("cr", false);
			}
			p.send(toPlayer.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void broadcastPlayerList() {
		try {
			JSONArray playerList = new JSONArray();

			for (Player p : players) {
				playerList.put(p.getName());
			}

			JSONObject j = new JSONObject();
			j.put("msg", "plist");
			j.put("f", "g"); // for: game. Other is f:s, playerlist for entire server
			j.put("data", playerList);

			String msg = j.toString();

			broadcast(msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void creatorDC() {
		broadcast("{\"msg\":\"cdc\"}");

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
		if (players.size() < 2) {
			getCreator().send("{\"msg\":\"c\",\"d\":\"You can't start with less than 2 players\", \"u\":\"System\"}");
		} else {
			started = true;
			broadcast("{\"msg\":\"sg\"}");

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

		try {
			JSONArray scrollList = pack.getJSONArray();
			JSONObject toPlayer = new JSONObject();
			toPlayer.put("msg", "pl");
			toPlayer.put("d", scrollList);
			toPlayer.put("p", (packIndex + 1));
			toPlayer.put("r", (currentRound + 1));
			toPlayer.put("t", timeout);
			player.send(toPlayer.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	public void broadcast(String msg) {
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
			broadcast("{\"msg\":\"pp\", \"d\":\"" + pl.getName() + "\"}");

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

			try {
				JSONObject pickedJSON = new JSONObject();
				pickedJSON.put("msg", "ps");
				pickedJSON.put("d", picked.getId()); // picked.getJSONObject();
				players.get(playerIndex).send(pickedJSON.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
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

				broadcast("{\"msg\":\"fr\",\"f\":" + currentRound + ",\"m\":" + numPacks + ",\"d\":" + packDirection
						+ "}");

				round();
			} else { // all done! :D
				gameFinished();
			}
		}
	}

	private void gameFinished() {
		finished = true;
		broadcast("{\"msg\":\"fg\"}");

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
			broadcast("{\"msg\":\"part\",\"d\":\"" + player.getName() + "\", \"f\": \"" + this.getName() + "\"}");
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
			try {
				JSONArray deckJSON = decks.get(playerName).getDeck();

				JSONObject out = new JSONObject();
				out.put("msg", "sp");
				out.put("p", playerName);
				out.put("d", deckJSON);

				player.send(out.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
