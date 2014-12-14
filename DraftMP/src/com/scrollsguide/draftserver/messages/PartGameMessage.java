package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class PartGameMessage extends Message {

	public PartGameMessage() {
		super("pg");
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		return new JSONObject();
	}

}
