package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinMessage extends Message {

	private final String username;
	private final String room;

	public JoinMessage(String username, String room) {
		super("join");

		this.username = username;
		this.room = room;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		out.put("d", this.username);
		out.put("f", this.room);

		return out;
	}
}
