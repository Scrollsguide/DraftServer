package com.scrollsguide.draftserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scrollsguide.draftserver.messages.Message;

public class BotPlayer extends Player {

	private ScrollList scrolls;

	private HashMap<String, Integer> bias = new HashMap<String, Integer>();
	private final float biasIncrement = 1.5f;
	private final int stopIncrementRound = 30; // stop changing bias after pack 3
	private int round = 0;

	private Random r = new Random();

	public BotPlayer(DraftServer server, ScrollList scrolls, int id) {
		super(server);

		this.scrolls = scrolls;
		isBot = true;

		// main resource
		// do id % resources to give all bots
		// an even spead across resources
		int mainResource = id % Settings.RESOURCES.length;
		bias.put(Settings.RESOURCES[mainResource], 30);

		for (int i = 0; i < Settings.RESOURCES.length; i++) {
			if (i != mainResource) {
				bias.put(Settings.RESOURCES[i], r.nextInt(15) + 1); // +1 to prevent r.nextInt(0)
			}
		}
		printBias();
	}

	private void printBias() {
		for (int i = 0; i < Settings.RESOURCES.length; i++) {
			System.out.print(Settings.RESOURCES[i] + ": " + bias.get(Settings.RESOURCES[i]) + "   ");
		}
		System.out.println();
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override 
	public void send(Message msg){
		send(msg.toString());
	}

	public void send(String fromServer) {
		try {
			JSONObject in = new JSONObject(fromServer);
			String msg = in.getString("msg");

			if (msg.equals("pl")) { // pack list
				round++;
				JSONArray packList = in.getJSONArray("d");

				ArrayList<String> availRes = new ArrayList<String>();

				// create list of resources available in this pack
				for (int i = 0; i < packList.length(); i++) {
					Scroll s = scrolls.getById(packList.getInt(i));

					String resource = s.getResource();
					if (!availRes.contains(resource)) {
						availRes.add(resource);
					}
				}

				// get the total bias
				int totalBias = 0;
				for (String s : availRes) {
					totalBias += bias.get(s);
				}

				// pick a random number
				Random r = new Random();
				int randomNr = r.nextInt(totalBias);

				// see in which bias part this number is
				int start = 0;

				String resource = "";
				for (String s : availRes) {
					if (randomNr >= start && randomNr < start + bias.get(s)) {
						resource = s;
						break;
					}
					start += bias.get(s);
				}

				if (round < stopIncrementRound) { // don't increment bias anymore after this
					bias.put(resource, (int) (bias.get(resource) * biasIncrement));
				}
				// debug
				printBias();

				ArrayList<Integer> canPick = new ArrayList<Integer>();
				for (int i = 0; i < packList.length(); i++) {
					Scroll s = scrolls.getById(packList.getInt(i));

					if (s.getResource().equals(resource)) {
						canPick.add(i); // yes, i, not scroll.getInt("id");
					}
				}

				int maxValue = -1; // -1 to make sure the loop always picks a scroll from canPick when r.nextInt() == 0
				// select scroll with highest value
				int scrollIndex = 0;
				for (Integer i : canPick) {
					int valueForScroll = scrolls.getById(packList.getInt(i)).getRandomizedValue(r);// r.nextInt(10) * scrolls.getById(packList.getInt(i)).getRarity();
					if (valueForScroll > maxValue) {
						scrollIndex = i;
						maxValue = valueForScroll;
					}
				}

				// send pick message
				this.inGame.pickScroll(this, packList.getInt(scrollIndex));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendError(String error) { // ignore
		System.err.println(username + " got error: " + error);
	}

}
