package com.scrollsguide.draftserver;


public abstract class Player {

	protected String username = "";
	protected Game inGame = null;
	protected DraftServer server;

	protected boolean isBot = false;

	public Player(DraftServer server) {
		this.server = server;
	}

	/**
	 * @return username of player
	 */
	public String getName() {
		return username;
	}

	/**
	 * Part from active game
	 * @return true if the player was active in a game
	 */
	public boolean partFromGames() {
		if (inGame != null) {
			inGame.part(this);
			inGame = null;
			return true;
		}
		return false;
	}

	public boolean isInGame() {
		return inGame != null;
	}

	public abstract void send(String msg);

	public abstract void sendError(String error);
}
