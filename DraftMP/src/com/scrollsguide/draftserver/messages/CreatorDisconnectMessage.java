package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class CreatorDisconnectMessage extends Message {

	public CreatorDisconnectMessage() {
		super("cdc");
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		return new JSONObject();
	}

}
