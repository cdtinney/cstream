package com.cstream.tracker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.cstream.logging.LogLevel;
import com.cstream.model.Song;
import com.cstream.notifier.Notifier;
import com.cstream.socket.IOSocket;
import com.cstream.socket.MessageCallback;
import com.cstream.util.JsonUtils;

public final class TrackerClient {

	private static Logger LOGGER = Logger.getLogger(TrackerClient.class.getName());

	// Server URL constants
	private final static String SERVER_URL = "https://cstream-tracker-venom889.c9.io";
	private static final String LIB_URL = SERVER_URL;
	private static final String JOIN_URL = SERVER_URL + "/join";
	private static final String REMOVE_URL = SERVER_URL + "/remove";
	
	// Socket event types
	private static final String PEER_CONNECT = "PEER_CONNECT";
	private static final String PEER_DISCONNECT = "PEER_DISCONNECT";
	
	// Keep alive message interval (in seconds)
	private static final int KEEP_ALIVE = 10;

	// Networking objects
	private HttpClient client = HttpClientBuilder.create().build();	
	private IOSocket socket;
	
	// Model classes
	private TrackerPeer peer;
	private Map<String, Song> sharedLibrary;

	// Empty private constructor so no instances can be created without a peer
	@SuppressWarnings("unused")
	private TrackerClient() { 
		
	}
	
	public TrackerClient(TrackerPeer peer) {
		this.peer = peer;
	}
	
	public TrackerPeer getPeer() {
		return peer;
	}
	
	public boolean start() {
		
		boolean joined = join(peer);
		if (!joined) {
			LOGGER.warning("Failed to join tracker");
			return false;
		}
		
		LOGGER.info("Joined tracker successfully");
		initializeSocket();
		return true;
		
	}
	
	public boolean stop() {
			
		boolean removed = remove(peer);
		if (!removed) {
			LOGGER.warning("Request to remove peer from tracker was not successful");
			return false;
		}

		LOGGER.info("Reqeust to remove peer from tracker was successful");		
		closeSocket();
		return true;
		
	}
	
	private void updateLibrary() {

		Map<String, Song> updatedLibrary = getLibrary();
		Notifier.getInstance().notify(this, "sharedLibrary", sharedLibrary, sharedLibrary = updatedLibrary);
		
	}
	
	private void closeSocket() {
		
		if (socket == null) {
			return;
		}
		
		socket.disconnect();
		
	}
	
	private void initializeSocket() {
		
		socket = new IOSocket(SERVER_URL.replace("https", "ws"), new MessageCallback() {

			@Override
			public void on(String event, JSONObject... data) {
				
				switch(event) {
				
					case PEER_CONNECT:
						LOGGER.info("PEER_CONNECT event received: " + data);
						updateLibrary();
						break;
					
					case PEER_DISCONNECT:
						LOGGER.info("PEER_DISCONNECT event received: " + data);
						updateLibrary();
						break;
						
					default:
						LOGGER.info("Socket event received - " + event + " - " + data);						
					
				}
				
			}

			@Override
			public void onMessage(String message) {
				LOGGER.info("Message received: " + message);
			}

			@Override
			public void onMessage(JSONObject json) {
				LOGGER.info("JSON message received: " + json);
			}

			@Override
			public void onConnect() {
				LOGGER.info("Connected to socket server");
				startKeepAlive();
				
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
	private Map<String, Song> getLibrary() {

		String response = getRequest(LIB_URL);
		if (response == null || response.isEmpty()) {
			LOGGER.warning("A GET library request returned a null or empty response");
			return null;
		}
		
		Map<String, String> jsonMap = JsonUtils.parseJsonStringMap(response);
		if (jsonMap != null && isResponseOk(jsonMap.get("status"))) {
			return JsonUtils.parseJsonSongMap(jsonMap.get("library"));
			
		} else {
			LOGGER.warning("A GET library request returned a response that is not a valid JSON library");
			
		}

		return null;
		
	}

	/**
	 * Do a POST join request to the tracker. Returns true if the request returned OK, false otherwise.
	 */
	private boolean join(TrackerPeer peer) {

		try {
			
			String response  = postRequest(JOIN_URL, new StringEntity(JsonUtils.getJson(peer)));
			if (response == null || response.isEmpty()) {
				LOGGER.warning("Join POST request returned null or empty response");
				return false;
			}
			
			Map<String, String> jsonMap = JsonUtils.parseJsonStringMap(response);
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
	private boolean remove(TrackerPeer peer) {

		Map<String, String> jsonMap = null;
		String response = "";
		
		try {
			response = postRequest(REMOVE_URL, new StringEntity(JsonUtils.toJson("id", peer.getId())));	
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}

		if (!response.isEmpty()) {
			jsonMap = JsonUtils.parseJsonStringMap(response);
			
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
	private String getRequest(String url) {

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
	private String postRequest(String url, StringEntity params) {

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
	
	private static boolean isResponseOk(String code) {
		return code.equals("OK");
	}
	
	/*
	 * Schedules a task that sends a KEEP_ALIVE message to the  socket server once every 10 seconds.
	 * If the server doesn't receive the message, it will kill the socket and remove the peer from the tracker.
	 */
	private void startKeepAlive() {
		
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		Runnable task = new Runnable() {

			@Override
			public void run() {
				
				try {
					LOGGER.log(LogLevel.DEBUG, "Sending KEEP_ALIVE from ID = " + peer.getId());
					socket.sendKeepAlive(peer.getId());
					
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		};
		
		executor.scheduleAtFixedRate(task, 0, KEEP_ALIVE, TimeUnit.SECONDS);
		
	}

}
