package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class PartMessage extends Message {

	private final String username;
	private final String room;

	public PartMessage(String username, String room) {
		super("part");

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
