package com.cstream.tracker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.cstream.model.Song;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public final class TrackerClient {

	private static Logger LOGGER = Logger.getLogger(TrackerClient.class.getName());

	private final static String SERVER = "https://cstream-tracker-venom889.c9.io";

	private static final String LIB = SERVER;
	private static final String JOIN = SERVER + "/join";
	private static final String REMOVE = SERVER + "/remove";

	private static HttpClient client = HttpClientBuilder.create().build();	
	private static Gson json = new Gson();

	private TrackerClient() {
		// Empty
	}

	/**
	 * Gets the json string from a get request for the library and checks the status before
	 * trying to parse the song library
	 * 
	 * @return Map<String, Song> song library from tracker
	 */
	public static Map<String, Song> getLibrary() {

		Map<String, String> jsonMap = null;
		Map<String, Song> library = null;
		String response = getRequest(LIB);

		if(!response.isEmpty()) {
			jsonMap = getValidJsonMap(response);
			
		}
		
		if(jsonMap != null && isResponseOk(jsonMap.get("status"))) {
			library = getValidJsonSongLibrary(jsonMap.get("library"));
			
		} else {
			LOGGER.warning("Response from Tracker was invalid");
			
		}

		return library;
	}

	/**
	 * Gets the response from a join post to the tracker and checks the status
	 * returns true if server accepted join and false if something went wrong
	 * 
	 * @param peer
	 * @return
	 */
	public static boolean join(TrackerPeer peer) {

		Map<String, String> jsonMap = null;
		String response = "";

		try {
			response  = postRequest(JOIN, new StringEntity(getJson(peer)));
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}
		
		if(!response.isEmpty()) {
			jsonMap = getValidJsonMap(response);
			
		}
		
		if(jsonMap != null && isResponseOk(jsonMap.get("status"))) {
			return true;
			
		} else {
			LOGGER.warning("Tracker didnt confirm peer join");
			return false;
			
		}

	}

	/**
	 * Gets the response from a remove post to the tracker and checks the status
	 * returns true if server processed the removal and false if something went wrong
	 * 
	 * @param peer
	 * @return
	 */
	public static boolean remove(TrackerPeer peer) {

		Map<String, String> jsonMap = null;
		String response = "";

		try {
			response = postRequest(REMOVE, new StringEntity(getJson(peer.getId())));	
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}

		if(!response.isEmpty()) {
			jsonMap = getValidJsonMap(response);
			
		}
		
		if(jsonMap != null && isResponseOk(jsonMap.get("status"))) {
			return true;
			
		} else {
			LOGGER.warning("Tracker didnt confirm peer removal");
			return false;
			
		}

	}

	/**
	 * Performs a get request to the given URL and returns the response as a string
	 * 
	 * @param url
	 * @return
	 */
	private static String getRequest(String url) {

		String responseJson = "";

		try {
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");

		} catch (IOException e) {
			LOGGER.warning("Exception in processing get request: " + e.getLocalizedMessage());	
			e.printStackTrace();
			
		}

		return responseJson;

	}

	/**
	 * Performs a post to the tracker with the URL and params given as json in the 
	 * body. Returns the response from the tracker as a string
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	private static String postRequest(String url, StringEntity params) {

		String responseJson = "";

		try {
			HttpPost post = new HttpPost(url);
			post.setEntity(params);
			post.setHeader("Content-Type", "application/json");
			HttpResponse response = client.execute(post);
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");

		} catch (IOException e) {
			LOGGER.warning("Exception in processing post request: " + e.getLocalizedMessage());	
			e.printStackTrace();
			
		}

		return responseJson;

	}
	
	/**
	 * Checks if a given string code is an OK status from tarcker
	 * 
	 * @param code
	 * @return
	 */
	private static boolean isResponseOk(String code) {
		
		if(code.equals("OK")) {
			return true;
			
		} else {
			return false;
			
		}
		
	}

	/**
	 * Takes the given object and returns it as a JSON string
	 * 
	 * @param obj
	 * @return
	 */
	private static String getJson(Object obj) {
		
		return json.toJson(obj);
		
	}
	
	/**
	 * Tries to parse a json string to a Song Library Map. Returns null if the parse
	 * fails
	 * 
	 * @param jString
	 * @return
	 */
	private static Map<String, Song> getValidJsonSongLibrary(String jString) {
		
		Map<String, Song> map = null;
		
		try {
			Type type = new TypeToken<Map<String, Song>>(){}.getType();
			map = json.fromJson(jString, type);

		} catch (JsonParseException e) {
			LOGGER.warning("Error parsing json song library: " + jString);	
			return null;
			
		}
		
		return map;
	}

	/**
	 * Tries to parse a json string to a Map of properties. Returns null if the parse
	 * fails
	 * 
	 * @param jString
	 * @return
	 */
	private static Map<String, String> getValidJsonMap(String jString) {

		Map<String, String> map = null;
		
		try {
			Type type = new TypeToken<Map<String, String>>(){}.getType();
			map = json.fromJson(jString, type);

		} catch (JsonParseException e) {
			LOGGER.warning("Error parsing json string: " + jString);	
			return null;
			
		}

		return map;
	}

}
