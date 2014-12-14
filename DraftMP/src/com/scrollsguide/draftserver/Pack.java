package com.scrollsguide.draftserver;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

public class Pack {

	private Game g;
	private ArrayList<Scroll> inPack = new ArrayList<Scroll>();

	public Pack(Game g) {
		this.g = g;
	}

	public void init(int common, int uncommon, int rare) {
		int[] rarities = new int[] { common, uncommon, rare };

		for (int i = 0; i < rarities.length; i++) {
			for (int j = 0; j < rarities[i]; j++) {
				// select random scroll that is not yet in the pack
				Scroll singleScroll;
				do {
					singleScroll = g.getRandomScroll(i);
				} while (inPack.contains(singleScroll));
				inPack.add(singleScroll);
			}
		}
	}

	public void print() {
		System.out.println("In this pack: ");

		for (Scroll s : inPack) {
			System.out.print(s.getName() + ", ");
		}
		System.out.println();
	}

	public JSONArray getJSONArray() throws JSONException {
		JSONArray out = new JSONArray();

		for (Scroll s : inPack) {
			// out.put(s.getJSONObject());
			out.put(s.getId());
		}

		return out;
	}

	public int size() {
		return inPack.size();
	}

	public boolean contains(Scroll s) {
		return inPack.contains(s);
	}

	public void remove(Scroll picked) {
		inPack.remove(picked);
	}
}
