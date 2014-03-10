package com.scrollsguide.draftserver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Deck {

	private HashMap<Scroll, Integer> inDeck = new HashMap<Scroll, Integer>();
	private JSONArray deckJSON = null;

	public Deck() {

	}

	public void add(Scroll s) {
		if (inDeck.containsKey(s)) {
			inDeck.put(s, inDeck.get(s) + 1);
		} else {
			inDeck.put(s, 1);
		}
	}

	private void generateJSON() {
		try {
			deckJSON = new JSONArray();

			Iterator<Entry<Scroll, Integer>> it = inDeck.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Scroll, Integer> pairs = (Map.Entry<Scroll, Integer>) it.next();

				JSONObject singleScroll = new JSONObject();
				singleScroll.put("id", pairs.getKey().getId());
				singleScroll.put("c", pairs.getValue());

				deckJSON.put(singleScroll);

				it.remove();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JSONArray getDeck() {
		if (deckJSON == null) {
			generateJSON();
		}
		return deckJSON;
	}
}
