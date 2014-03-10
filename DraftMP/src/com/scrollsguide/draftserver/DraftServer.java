package com.scrollsguide.draftserver;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.messages.JoinMessage;
import com.scrollsguide.draftserver.messages.Message;
import com.scrollsguide.draftserver.messages.PartMessage;

public class DraftServer extends WebSocketHandler {

	private ScrollList scrolls;

	/**
	 * A threadsafe list of open WebSockets
	 */
	private final Set<HumanPlayer> players = new CopyOnWriteArraySet<HumanPlayer>();
	private final Set<Game> games = new CopyOnWriteArraySet<Game>();

	public DraftServer(ScrollList sl) {
		this.scrolls = sl;
	}

	/**
	 * This method will be called on every client connect. This method must return a WebSocket-implementing-class to work on. WebSocket is just an interface with different types of communication
	 * possibilities. You must create and return a class which implements the necessary WebSocket interfaces.
	 */
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new HumanPlayer(this);
	}

	public void add(HumanPlayer player) {
		players.add(player);
	}

	public void remove(HumanPlayer player) {
		players.remove(player);
	}

	public Set<HumanPlayer> getPlayers() {
		return players;
	}

	public Set<Game> getGames() {
		return games;
	}

	public void broadcast(Message m) {
		String msg = m.toString();
		broadcast(msg);
	}

	public void broadcast(String msg) {
		for (HumanPlayer p : players) {
			p.send(msg);
		}
	}

	/**
	 * Player wants to login to the server
	 * @param username from player
	 * @return
	 */
	public boolean login(String username) {
		boolean canJoin = true;
		for (HumanPlayer p : players) {
			if (p.getName().equals(username)) {
				canJoin = false;
			}
		}

		if (canJoin) {
			broadcast(new JoinMessage(username, "the lobby"));
		}
		return canJoin;
	}

	public void part(HumanPlayer player) {
		if (!player.isLoggedIn()) { // player was never logged in, no need for messaging
			return;
		}
		String username = player.getName();

		boolean gameChanged = false;
		for (Game g : games) {
			if (g.getCreator() == player) {
				// playtime's over :(
				g.creatorDC();
				games.remove(g);
				gameChanged = true;
			}
		}

		if (!gameChanged) {
			if (player.partFromGames()) {
				broadcastGameList();
			}
		} else {
			broadcastGameList();
		}

		String partMsg = (new PartMessage(username, "the lobby")).toString();
		for (HumanPlayer p : players) {
			if (p != player) {
				p.send(partMsg);
			}
		}
	}

	/**
	 * Broadcasts all open, not-started games to every connected client.
	 */
	public void broadcastGameList() {
		JSONObject gameList = getGameList();
		for (HumanPlayer p : players) {
			p.send(gameList.toString());
		}
	}

	/**
	 * Deletes game from server and broadcasts new game list to every client
	 * @param g game to delete
	 */
	public void deleteGame(Game g) {
		games.remove(g);
		broadcastGameList();
	}

	/**
	 * Creates a new game with specified parameters
	 * @param p creator of the game
	 * @param id of the game, sent by client...
	 * @param name of the game
	 * @param packs number of packs
	 * @param maxplayers
	 * @param bots
	 * @param password
	 * @return Game g if successfully created, null otherwise
	 */
	public Game createGame(HumanPlayer p, String id, String name, int packs, int maxplayers, int bots, int timeout,
			String password) {
		boolean allowed = true;
		for (Game g : games) {
			if (g.getCreator() == p) {
				allowed = false;
				break;
			}
		}

		Game g = null;
		if (!allowed) {
			p.sendError("You already have an open game.");
		} else {
			// now check for illegal arguments
			if (maxplayers < 2) {
				allowed = false;
				p.sendError("You cannot start a game with less than 2 players.");
			} else if (maxplayers < bots + 1) { // +1 for creator himself
				allowed = false;
				p.sendError("You cannot have more bots than the maximum game size.");
			} else if (packs < 1) {
				allowed = false;
				p.sendError("You cannot have less than 1 pack.");
			}

			if (allowed) {
				g = new Game(p, this, scrolls, id, name, packs, maxplayers, bots, timeout, password);
				games.add(g);
				g.initBots();

				broadcastGameList();
			}
		}

		return g;
	}

	/**
	 * @param p Player that joins a game
	 * @param gameID of the game to join
	 * @param password of the game, "" if none
	 * @return Game that has been joined, null if not joined
	 */
	public Game gameJoin(Player p, String gameID, String password) {
		if (p.isInGame()) {
			p.sendError("You are already in a game");
			return null;
		}
		Game toJoin = null;
		for (Game g : games) {
			if (g.getID().equals(gameID)) {
				toJoin = g;
				break;
			}
		}

		if (toJoin == null) {
			p.sendError("Game does not exist.");
		} else {
			if (toJoin.isStarted()) {
				p.sendError("The game has already started");
				toJoin = null;
			} else if (toJoin.isFull()) {
				p.sendError("The game is full");
				toJoin = null;
			} else {
				if (toJoin.hasPassword()) {
					if (password.equals("")) { // not filled out yet
						p.send("{\"msg\":\"hp\",\"d\":\"" + toJoin.getID() + "\"}");
						toJoin = null;
					} else {
						if (password.equals(toJoin.getPassword())) {
							toJoin.playerJoins(p);
							broadcastGameList();
						} else {
							p.send("{\"msg\":\"wp\"}");
							toJoin = null;
						}
					}
				} else {
					toJoin.playerJoins(p); // game does messaging for event
					broadcastGameList();
				}
			}
		}
		return toJoin;
	}

	public JSONObject getGameList() {
		JSONObject gameList = null;
		try {
			gameList = new JSONObject();
			gameList.put("msg", "glist");
			JSONArray gameArray = new JSONArray();

			Set<Game> games = getGames();

			for (Game g : games) {
				if (!g.isStarted()) {
					JSONObject j = new JSONObject();
					j.put("n", g.getName());
					j.put("id", g.getID());
					j.put("p", g.getPlayerCount());
					j.put("m", g.getMaxPlayers());
					gameArray.put(j);
				}
			}

			gameList.put("data", gameArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return gameList;
	}
}