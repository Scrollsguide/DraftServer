package com.scrollsguide.draftserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Util {
	public static String loadWebpage(String u) {
		String result = "";
		try {
			URL url = new URL(u);
			URLConnection spoof = url.openConnection();

			// Spoof the connection so we look like a web browser
			spoof.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0;    H010818)");
			BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
			String strLine = "";
			// Loop through every line in the source
			while ((strLine = in.readLine()) != null) {
				result += strLine;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
