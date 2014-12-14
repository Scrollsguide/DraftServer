package com.scrollsguide.draftserver.messages;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.HumanPlayer;
import com.scrollsguide.draftserver.Player;

public class PlayerListMessage extends Message {

	// player list for game, or for entire server?
	public static enum SCOPE { SERVER, GAME };
	
	private JSONObject out = new JSONObject();

	public PlayerListMessage(SCOPE scope, List<Player> players) {
		this(scope, players.toArray(new Player[players.size()]));
	}

	public PlayerListMessage(SCOPE scope, Set<HumanPlayer> players) {
		this(scope, players.toArray(new Player[players.size()]));
	}

	public PlayerListMessage(SCOPE scope, Player[] players) {
		super("plist");

		try {
			out.put("f", scope == SCOPE.SERVER ? "s" : "g"); // g is game list, s is server list
			JSONArray playerArray = new JSONArray();

			for (Player p : players){
				playerArray.put(p.getName());
			}
			out.put("data", playerArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected JSONObject getJSON() throws JSONException {
		return out;
	}

}
