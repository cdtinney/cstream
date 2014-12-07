package com.cstream.tracker;

import java.io.UnsupportedEncodingException;
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
import com.cstream.notifier.Notifier;
import com.cstream.util.JsonUtils;

public final class TrackerClient {

	private static Logger LOGGER = Logger.getLogger(TrackerClient.class.getName());

	// Server URL constants
	private final static String SERVER_URL = "https://cstream-tracker-venom889.c9.io";
	private static final String LIB_URL = SERVER_URL;
	private static final String JOIN_URL = SERVER_URL + "/join";
	private static final String REMOVE_URL = SERVER_URL + "/remove";

	// Networking objects
	private HttpClient client = HttpClientBuilder.create().build();
	
	// Model classes
	private TrackerPeer peer;
	private Map<String, Song> sharedLibrary;

	@SuppressWarnings("unused")
	private TrackerClient() { }
	
	public TrackerClient(TrackerPeer peer) {
		this.peer = peer;
	}
	
	public TrackerPeer getPeer() {
		return peer;
	}
	
	public void setFiles(Map<String, Song> files) {
		sharedLibrary = files;
		peer.setFiles(files);
	}
	
	public boolean start() {
		
		boolean joined = join(peer);
		if (!joined) {
			LOGGER.warning("Failed to join tracker");
			return false;
		}
		
		LOGGER.info("Joined tracker successfully");
		//initializeSocket();
		return true;
		
	}
	
	public boolean stop() {
			
		boolean removed = remove(peer);
		if (!removed) {
			LOGGER.warning("Request to remove peer from tracker was not successful");
			return false;
		}

		LOGGER.info("Reqeust to remove peer from tracker was successful");		
		//closeSocket();
		return true;
		
	}
	
	// TODO - Call this
	private void updateLibrary() {

		// Fetch the library from the tracker
		Map<String, Song> updatedLibrary = getLibrary();
		
		// Set necessary songs to local
		for (Song s : updatedLibrary.values()) {
			
			if (s.sharedByPeer(peer.getId())) {
				s.setLocal(true);
			}
			
		}
		
		// Notify any listeners that the library has been updated
		Notifier.getInstance().notify(this, "sharedLibrary", sharedLibrary, sharedLibrary = updatedLibrary);
		
	}
	
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
	
	private static boolean isResponseOk(String code) {
		return code.equals("OK");
	}

	public String findSongPathById(String songId) {
		
		Song song = sharedLibrary.get(songId);
		if (song == null) {
			return null;
		}
		
		return song.getPath();	
		
	}
	
}
