package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.Game;
import com.scrollsguide.draftserver.Player;

public class PartLobbyMessage extends Message {

	private Player player;
	private Game game;

	public PartLobbyMessage(Player player, Game game) {
		super("part");

		this.player = player;
		this.game = game;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();

		out.put("d", this.player.getName());
		out.put("f", this.game.getName());

		return out;
	}

}
