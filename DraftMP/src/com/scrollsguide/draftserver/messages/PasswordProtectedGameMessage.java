package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.Game;

public class PasswordProtectedGameMessage extends Message {

	private Game game;
	
	public PasswordProtectedGameMessage(Game game){
		super("hp");

		this.game = game;
	}
	
	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("d", this.game.getID());
		
		return out;
	}

}
