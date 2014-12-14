package com.scrollsguide.draftserver.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.Deck;

public class DeckMessage extends Message {

	private String playerName;
	private Deck deck;
	
	public DeckMessage(String playerName, Deck deck) {
		super("sp");
		
		this.playerName = playerName;
		this.deck = deck;
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		JSONObject out = new JSONObject();
		
		out.put("p", this.playerName);
		out.put("d", deck.getDeck());
		
		return out;
	}

}
