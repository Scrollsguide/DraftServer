package com.scrollsguide.draftserver;

import java.util.HashMap;

public class Settings {
	public static final int PORT = 9873;
	public static final int MAX_PACKS = 20;
	public static final int MAX_PLAYERS = 20;
	public static final int MAX_TIMEOUT = 180;

	public static final String[] RESOURCES = new String[] { "Growth", "Order", "Energy", "Decay" };

	public static final HashMap<String, Integer> kindMultipliers = new HashMap<String, Integer>() {
		{
			put("CREATURE", 7);
			put("STRUCTURE", 4);
			put("ENCHANTMENT", 2);
			put("SPELL", 2);
		}
	};
}
