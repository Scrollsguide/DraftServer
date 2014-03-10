package com.scrollsguide.draftserver;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class Scroll {

	private int id;
	private String name;
	private int cardImg;
	private int rarity;
	private String kind;

	private String costType;

	public Scroll(JSONObject s) throws JSONException {
		this.id = s.getInt("id");
		this.name = s.getString("name");
		this.cardImg = s.getInt("image");
		this.rarity = s.getInt("rarity");
		this.kind = s.getString("kind");

		// get cost type
		for (int i = 0; i < Settings.RESOURCES.length; i++) {
			if (s.getInt("cost" + Settings.RESOURCES[i].toLowerCase()) > 0) {
				costType = Settings.RESOURCES[i];
				break;
			}
		}
	}

	public int getRarity() {
		return rarity;
	}

	public String getName() {
		return name;
	}

	// unused?
	public JSONObject getJSONObject() throws JSONException {
		JSONObject out = new JSONObject();
		out.put("name", name);
		out.put("id", id);
		out.put("img", cardImg);
		out.put("r", costType);
		out.put("rr", rarity);
		return out;
	}

	public String getResource() {
		return costType;
	}

	public int getId() {
		return id;
	}

	public int getRandomizedValue(Random r) {
		return (3 + r.nextInt(7)) * rarity * Settings.kindMultipliers.get(kind);
	}
}
