package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

public class RoundFinishedMessage extends Message {

	private int currentRound;
	private int numPacks;
	private int packDirection;
	
	public RoundFinishedMessage(int currentRound, int numPacks, int packDirection) {
		super("fr");
		
		this.currentRound = currentRound;
		this.numPacks = numPacks;
		this.packDirection = packDirection;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("f", this.currentRound);
		out.put("m", this.numPacks);
		out.put("d", this.packDirection);
		
		return out;
	}

}
