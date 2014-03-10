package com.scrollsguide.draftserver;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebSocketServer {

	public static void main(String[] args) {
		try {
			// load scrolls from url
			String complete = Util.loadWebpage("http://a.scrollsguide.com/scrolls");
			ScrollList sl = new ScrollList();
			JSONObject allScrolls = new JSONObject(complete.toString());
			JSONArray scrolls = allScrolls.getJSONArray("data");
			for (int i = 0; i < scrolls.length(); i++) {
				JSONObject scroll = scrolls.getJSONObject(i);

				sl.add(new Scroll(scroll));
			}

			System.out.println("Loaded " + sl.size() + " scrolls");

			// Create a Jetty server with the 9873 port.
			Server server = new Server(Settings.PORT);

			// Register the draftserver
			DraftServer draftserver = new DraftServer(sl);
			// Pass the DefaultHandler so 404 errors will be handled correctly.
			draftserver.setHandler(new DefaultHandler());
			server.setHandler(draftserver);

			// Start the Jetty server.
			System.out.println("Starting server...");
			server.start();
			server.join();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
