package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class StartGameMessage extends Message {

	public StartGameMessage() {
		super("sg");
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		return new JSONObject();
	}

}
