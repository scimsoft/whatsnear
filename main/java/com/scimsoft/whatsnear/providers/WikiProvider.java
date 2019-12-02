package com.scimsoft.whatsnear.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.scimsoft.whatsnear.MainActivity;
import com.scimsoft.whatsnear.helpers.Coordinates;
import com.scimsoft.whatsnear.helpers.WikiEntries;
import com.scimsoft.whatsnear.helpers.WikiEntry;

public class WikiProvider extends Providers {

	private WikiEntries allWikiEntries;
	public WikiProvider(MainActivity mainActivity) {
		super(mainActivity);
		allWikiEntries = new WikiEntries();
	}



	private JSONObject wikiResponse;
	private JSONArray titles;

	@SuppressWarnings("unchecked")
	@Override
	public WikiEntry getDetails(String title) {
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		params.add(new BasicNameValuePair("action", "query"));
		params.add(new BasicNameValuePair("prop", "extracts"));
		params.add(new BasicNameValuePair("exintro", ""));
		params.add(new BasicNameValuePair("format", "json"));
		params.add(new BasicNameValuePair("explaintext", ""));
		params.add(new BasicNameValuePair("titles", title));

		try {
			String country = mainActivity.localeProvider.getLocale().getLanguage();
			String wikiUrl = "https://" + country + ".wikipedia.org/w/api.php";
			wikiResponse = parser.makeHttpRequest(wikiUrl, params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WikiEntry wikientry = allWikiEntries.getWikiEntrie(title);
		try {
			JSONObject query = wikiResponse.getJSONObject("query");
			JSONObject pages = query.getJSONObject("pages");
			Iterator<String> iterator = pages.keys();
			String key = "";
			while (iterator.hasNext()) {
				key = iterator.next();
				wikientry.addExtract((String) ((JSONObject) pages.get(key)).get("extract"));
			}

		} catch (JSONException e) {

			e.printStackTrace();
		}

		return wikientry;
	}

	public List<String> getNearbyWikiEntries(Coordinates coordinates) {
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		params.add(new BasicNameValuePair("action", "query"));
		params.add(new BasicNameValuePair("list", "geosearch"));
		params.add(new BasicNameValuePair("gsradius", "10000"));
		params.add(new BasicNameValuePair("format", "json"));
		String coordinatesParam = coordinates.getLatitude() + "|" + coordinates.getLongtitude();
		// String coordinatesParam ="37.2141571|-7.4021457";
		params.add(new BasicNameValuePair("gscoord", coordinatesParam));

		try {
			//String country = mainActivity.locationProvider.getLocationLocale().toLowerCase(Locale.getDefault());
			String lang = mainActivity.localeProvider.getLocale().getLanguage();
			String wikiUrl = "https://" + lang + ".wikipedia.org/w/api.php";
			wikiResponse = parser.makeHttpRequest(wikiUrl, params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			JSONObject query = wikiResponse.getJSONObject("query");
			titles = query.getJSONArray("geosearch");

			// JSONObject geoLoc = query.getJSONObject("geosearch");
			// titles = results.getJSONArray(1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < titles.length(); i++) {
			try {				
				String wikiEntryTitle = titles.getJSONObject(i).getString("title");				
				WikiEntry wikiEntry = new WikiEntry(wikiEntryTitle);
				Location location = new Location("");
				location.setLatitude(titles.getJSONObject(i).getDouble("lat"));
				location.setLongitude(titles.getJSONObject(i).getDouble("lon"));				
				wikiEntry.setLocation(location);
				allWikiEntries.addWikiEntry(wikiEntry);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return allWikiEntries.getNameList();
	}
	@Override
	public void setListShown(){
		allWikiEntries.setListShown();
	}

	public void renewAllResults() {
		allWikiEntries.renewAllEntries();
		
	}

}
