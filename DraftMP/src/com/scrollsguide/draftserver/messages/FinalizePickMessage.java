package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.Scroll;

public class FinalizePickMessage extends Message {

	private Scroll scroll;
	
	public FinalizePickMessage(Scroll scroll){
		super("ps");
		
		this.scroll = scroll;
	}
	
	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("d", this.scroll.getId());
		
		return out;
	}

}
