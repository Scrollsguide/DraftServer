package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginMessage extends Message {

	private final String username;
	private final boolean success;

	public LoginMessage(String username, boolean success) {
		super("login");

		this.username = username;
		this.success = success;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();

		out.put("d", this.username);
		out.put("s", this.success);

		return out;
	}

}
