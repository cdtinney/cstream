package com.cstream.tracker;

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
import org.json.JSONObject;

import com.cstream.model.Song;
import com.cstream.socket.io.IOSocket;
import com.cstream.socket.io.MessageCallback;
import com.cstream.utils.logging.LogLevel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public final class TrackerClient {

	private static Logger LOGGER = Logger.getLogger(TrackerClient.class.getName());

	private final static String SERVER_URL = "https://cstream-tracker-venom889.c9.io";

	private static final String LIB_URL = SERVER_URL;
	private static final String JOIN_URL = SERVER_URL + "/join";
	private static final String REMOVE_URL = SERVER_URL + "/remove";

	private static HttpClient client = HttpClientBuilder.create().build();	
	private static IOSocket socket;
	
	// Only one instance of a JSON parser is necessary since no state is preserved
	private static Gson json = new Gson();

	// Empty private constructor so no extra instances can be created
	private TrackerClient() { }
	
	public static void closeSocket() {
		
		if (socket == null) {
			return;
		}
		
		socket.disconnect();
		
	}
	
	public static void initializeSocket() {
		
		socket = new IOSocket(SERVER_URL.replace("https", "ws"), new MessageCallback() {

			@Override
			public void on(String event, JSONObject... data) {
				// TODO - Client Socket - Handle JSON event
			}

			@Override
			public void onMessage(String message) {
				// TODO - Client Socket - Handle message
			}

			@Override
			public void onMessage(JSONObject json) {
				// TODO - Client Socket - Handle JSON message
			}

			@Override
			public void onConnect() {
				LOGGER.info("Connected to socket server");
			}

			@Override
			public void onDisconnect() {
				LOGGER.info("Disconnected from socket server");
			}
			
		});
		
		try {
			socket.connect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Do a GET request for the library from the tracker. If the request returns OK, attempt to parse the
	 * JSON returned.
	 */
	public static Map<String, Song> getLibrary() {

		String response = getRequest(LIB_URL);
		if (response == null || response.isEmpty()) {
			LOGGER.warning("A GET library request returned a null or empty response");
			return null;
		}
		
		Map<String, String> jsonMap = parseJsonMap(response);
		if (jsonMap != null && isResponseOk(jsonMap.get("status"))) {
			return parseJsonSongLibrary(jsonMap.get("library"));
			
		} else {
			LOGGER.warning("A GET library request returned a response that is not a valid JSON library");
			
		}

		return null;
		
	}

	/**
	 * Do a POST join request to the tracker. Returns true if the request returned OK, false otherwise.
	 */
	public static boolean join(TrackerPeer peer) {

		try {
			
			String response  = postRequest(JOIN_URL, new StringEntity(getJson(peer)));
			if (response == null || response.isEmpty()) {
				LOGGER.warning("Join POST request returned null or empty response");
				return false;
			}
			
			Map<String, String> jsonMap = parseJsonMap(response);
			if (jsonMap != null  && isResponseOk(jsonMap.get("status"))) {
				return true;
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}

		LOGGER.warning("The join POST request was not successful");
		return false;
		
	}

	/**
	 * Do a POST remove request to the tracker. Returns true if the request returned OK, false otherwise.
	 */
	public static boolean remove(TrackerPeer peer) {

		Map<String, String> jsonMap = null;
		String response = "";
		
		try {
			response = postRequest(REMOVE_URL, new StringEntity(getBasicJson("id", peer.getId())));	
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}

		if (!response.isEmpty()) {
			jsonMap = parseJsonMap(response);
			
		}
		
		if (jsonMap == null || !isResponseOk(jsonMap.get("status"))) {
			LOGGER.warning("The remove POST request was not successful");
			return false;
		}
		
		return true;
		
	}

	/**
	 * Performs a GET request to the given URL and returns the response as an (ideally) JSON string.
	 */
	private static String getRequest(String url) {

		try {
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			return EntityUtils.toString(response.getEntity(), "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return null;

	}

	/**
	 * Performs a POST to the tracker with the URL and parameters given as JSON in the 
	 * body. Returns the response from the tracker as an (ideally) JSON string.
	 */
	private static String postRequest(String url, StringEntity params) {

		try {
			HttpPost post = new HttpPost(url);
			post.setEntity(params);
			post.setHeader("Content-Type", "application/json");
			HttpResponse response = client.execute(post);
			return EntityUtils.toString(response.getEntity(), "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return "";

	}
	/**
	 * Tries to parse a JSON string to a map of type <SongID, Song>. Returns null if the parse
	 * fails (i.e. if the input string is not valid JSON).
	 */
	private static Map<String, Song> parseJsonSongLibrary(String jString) {
		
		try {			
			Type type = new TypeToken<Map<String, Song>>(){}.getType();
			return json.fromJson(jString, type);

		} catch (JsonParseException e) {
			LOGGER.warning("JSON parse error");
			LOGGER.log(LogLevel.DEBUG, "Could not parse: " + jString);
			LOGGER.log(LogLevel.DEBUG, e.getMessage());
			
		}
		
		return null;
		
	}

	/**
	 * Tries to parse a JSON string to a map of properties. Returns null if the parse
	 * fails (i.e. if the input string is not valid JSON).
	 */
	private static Map<String, String> parseJsonMap(String jString) {
		
		try {
			Type type = new TypeToken<Map<String, String>>(){}.getType();
			return json.fromJson(jString, type);

		} catch (JsonParseException e) {
			LOGGER.warning("JSON parse error");
			LOGGER.log(LogLevel.DEBUG, "Could not parse: " + jString);
			
		}

		return null;
		
	}
	
	private static String getBasicJson(String property, String value) {
		
		JsonObject object = new JsonObject();
		object.add(property, new JsonPrimitive(value));
		return object.toString();
		
	}
	
	private static String getJson(Object obj) {
		return json.toJson(obj);
	}
	
	private static boolean isResponseOk(String code) {
		return code.equals("OK");
	}

}
