package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class WrongPasswordMessage extends Message {

	public WrongPasswordMessage() {
		super("wp");
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		return new JSONObject();
	}

}
