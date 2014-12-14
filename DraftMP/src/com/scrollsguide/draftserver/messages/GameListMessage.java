package com.scrollsguide.draftserver.messages;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.Game;

public class GameListMessage extends Message {

	private JSONObject out = new JSONObject();

	public GameListMessage(Set<Game> games) {
		super("glist");

		try {
			JSONArray gameArray = new JSONArray();
			for (Game g : games) {
				if (!g.isStarted()) {
					JSONObject j = new JSONObject();
					j.put("n", g.getName());
					j.put("id", g.getID());
					j.put("p", g.getPlayerCount());
					j.put("m", g.getMaxPlayers());
					gameArray.put(j);
				}
			}
			
			out.put("data", gameArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		return out;
	}

}
