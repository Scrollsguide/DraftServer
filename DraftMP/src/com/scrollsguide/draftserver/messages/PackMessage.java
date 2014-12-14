package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.Pack;

public class PackMessage extends Message {

	private Pack pack;
	private int packIndex;
	private int currentRound;
	private int timeout;
	
	public PackMessage(Pack pack, int packIndex, int currentRound, int timeout) {
		super("pl");
		
		this.pack = pack;
		this.packIndex = packIndex;
		this.currentRound = currentRound;
		this.timeout = timeout;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("d", this.pack.getJSONArray());
		out.put("p", this.packIndex);
		out.put("r", this.currentRound);
		out.put("t", this.timeout);
		
		return out;
	}

}
