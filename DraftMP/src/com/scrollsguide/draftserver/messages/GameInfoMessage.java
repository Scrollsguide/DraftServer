package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class GameInfoMessage extends Message {

	private String name;
	private int numPacks;
	private int timeout;
	private boolean isCreator;	
	
	public GameInfoMessage(String name, int numPacks, int timeout, boolean isCreator) {
		super("jg");
		
		this.name = name;
		this.numPacks = numPacks;
		this.timeout = timeout;
		this.isCreator = isCreator;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("d", this.name);
		out.put("p", this.numPacks);
		out.put("t",  this.timeout);
		out.put("cr", this.isCreator);
		
		return out;
	}

}
