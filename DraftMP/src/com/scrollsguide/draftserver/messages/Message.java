package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Message {

	private final String id;

	public Message(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		try {
			JSONObject out = this.getJSON();
			out.put("msg", this.id);

			return out.toString();
		} catch (JSONException e) {
			return "{\"msg\":\"e\"}";
		}
	}

	protected abstract JSONObject getJSON() throws JSONException;
}
