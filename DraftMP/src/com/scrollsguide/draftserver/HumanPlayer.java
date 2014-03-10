package com.scrollsguide.draftserver;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jetty.websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.messages.ChatMessage;
import com.scrollsguide.draftserver.messages.LoginMessage;
import com.scrollsguide.draftserver.messages.PingMessage;

public class HumanPlayer extends Player implements WebSocket.OnTextMessage {

	private Connection connection;

	public HumanPlayer(DraftServer server) {
		super(server);
	}

	@Override
	public void onOpen(Connection connection) {
		System.out.println("[SERVER] Opened connection");
		this.connection = connection;
		server.add(this);
	}

	@Override
	public void onMessage(String data) {
		System.out.println("Received from " + username + ": " + data);
		try {
			JSONObject j = new JSONObject(data);

			if (!j.has("msg")) {
				send("{\"msg\":\"e\"}");
				return;
			}

			String msg = j.getString("msg");
			if (msg.equals("p")) {
				send((new PingMessage()).toString());
			} else if (msg.equals("login")) { // login
				String inUsername = j.getString("d");
				if (server.login(inUsername)) {
					username = inUsername;

					send((new LoginMessage(username, true)).toString());
				} else { // can't login
					send((new LoginMessage(inUsername, false)).toString());// socket is closed by client...
				}
			} else if (msg.equals("plist")) { // request player list

				JSONObject playerList = new JSONObject();
				playerList.put("msg", "plist");
				playerList.put("f", "s"); // for entire server, f:g is game
											// player list
				JSONArray playerArray = new JSONArray();

				Set<HumanPlayer> players = server.getPlayers();

				for (HumanPlayer p : players) {
					playerArray.put(p.getName());
				}

				playerList.put("data", playerArray);
				send(playerList.toString());
			} else if (msg.equals("glist")) {
				send(server.getGameList().toString());
			} else if (msg.equals("c")) { // chat
				server.broadcast(new ChatMessage(username, j.getString("d")));
			} else if (msg.equals("cr")) { // create new game
				this.inGame = server.createGame(this, j.getString("id"), j.getString("n"), j.getInt("p"),
						j.getInt("mp"), j.getInt("b"), j.getInt("t"), j.getString("pw"));
			} else if (msg.equals("jg")) { // join game
				this.inGame = server.gameJoin(this, j.getString("d"), j.getString("pw"));
			} else if (msg.equals("pg")) { // part game
				if (this.inGame != null) {
					if (this.inGame.getCreator() == this) {
						this.inGame.creatorDC();
						server.deleteGame(this.inGame);
					}
					if (this.inGame.part(this)) {
						send("{\"msg\":\"pg\"}");
						this.inGame = null;
						this.send(server.getGameList().toString());
					} else {
						sendError("You are not in a game");
					}
				} else {
					sendError("You are not in a game");
				}
			} else if (msg.equals("sg")) {// start game
				if (this.inGame != null) {
					if (this.inGame.getCreator() == this) { // is owner, can start
						this.inGame.start();
						server.broadcastGameList(); // so that it's removed from the game list
					} else {
						send("Owner is " + this.inGame.getCreator().getName() + ", you are " + this.getName());
					}
				} else {
					System.err.println("Null");
				}
			} else if (msg.equals("ps")) { // pick a scroll
				if (this.inGame != null) {
					this.inGame.pickScroll(this, j.getInt("d"));
				} else {
					sendError("You are currently not in a game");
				}
			} else if (msg.equals("sp")) { // see picks of other player
				if (this.inGame != null) {
					if (this.inGame.isFinished()) {
						String playerName = j.getString("d");

						this.inGame.sendDeck(this, playerName);
					} else {
						sendError("This game has not finished yet");
					}
				} else {
					sendError("You are currently not in a game");
				}
			} else {
				sendError("Unhandled msg '" + msg + "'");
			}
		} catch (JSONException e) {
			sendError("Invalid JSON");
			System.out.println("JSONException: ");
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(int closeCode, String message) {
		System.out.println("[SERVER] Client has closed the connection");
		server.part(this);
		server.remove(this);
	}

	@Override
	public void sendError(String error) {
		try {
			JSONObject err = new JSONObject();
			err.put("msg", "e");
			err.put("d", error);

			send(err.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Yes, public, also used by DrawServer
	 * @param msg to send
	 */
	@Override
	public void send(String msg) {
		System.out.println("Sending to " + username + ": " + msg);
		try {
			connection.sendMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isLoggedIn() {
		// ugly
		return !username.equals("");
	}
}