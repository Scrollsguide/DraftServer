package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class GameFinishedMessage extends Message {

	public GameFinishedMessage() {
		super("fg");
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		return new JSONObject();
	}

}
