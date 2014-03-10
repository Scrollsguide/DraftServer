package com.scrollsguide.draftserver.messages;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage extends Message {

	private final String username;
	private final String text;

	public ChatMessage(String username, String text) {
		super("c");

		this.username = username;
		this.text = text;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();

		out.put("d", StringEscapeUtils.escapeHtml4(this.text));
		out.put("u", this.username);

		return out;
	}
}
