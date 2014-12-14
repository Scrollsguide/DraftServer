package com.scrollsguide.draftserver;

import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.messages.ChatMessage;
import com.scrollsguide.draftserver.messages.ErrorMessage;
import com.scrollsguide.draftserver.messages.GameListMessage;
import com.scrollsguide.draftserver.messages.LoginMessage;
import com.scrollsguide.draftserver.messages.Message;
import com.scrollsguide.draftserver.messages.PartGameMessage;
import com.scrollsguide.draftserver.messages.PingMessage;
import com.scrollsguide.draftserver.messages.PlayerListMessage;

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
				send(new ErrorMessage("Missing parameter 'msg'."));
				return;
			}

			String msg = j.getString("msg");
			if (msg.equals("p")) {
				send(new PingMessage());
			} else if (msg.equals("login")) { // login
				String inUsername = j.getString("d");
				if (server.login(inUsername)) {
					username = inUsername;

					send(new LoginMessage(username, true));
				} else { // can't login
					send(new LoginMessage(inUsername, false));// socket is closed by client...
				}
			} else if (msg.equals("plist")) { // request player list
				send(new PlayerListMessage(PlayerListMessage.SCOPE.SERVER, server.getPlayers()));
			} else if (msg.equals("glist")) {
				send(new GameListMessage(server.getGames()));
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
						send(new PartGameMessage());
						this.inGame = null;
						send(new GameListMessage(server.getGames()));
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
						sendError("You are not the owner of this game.");
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
		send(new ErrorMessage(error));
	}

	@Override 
	public void send(Message msg){
		System.out.println("Sending to " + username + ": " + msg.toString());
		try {
			connection.sendMessage(msg.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Yes, public, also used by DrawServer
	 * @param msg to send
	 */
//	@Override
//	public void send(String msg) {
//		System.out.println("Sending to " + username + ": " + msg);
//		try {
//			connection.sendMessage(msg);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public boolean isLoggedIn() {
		// ugly
		return !username.equals("");
	}
}