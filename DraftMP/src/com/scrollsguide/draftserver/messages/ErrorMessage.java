package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrorMessage extends Message {

	private String error;
	
	public ErrorMessage(String error) {
		super("e");
		
		this.error = error;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("d", this.error);
		
		return out;		
	}

}
