package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.Player;

public class PlayerPickMessage extends Message {
	
	private Player player;

	public PlayerPickMessage(Player player) {
		super("pp");
		
		this.player = player;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("d", this.player.getName());
		
		return out;
	}

}
