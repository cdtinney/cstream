package com.cstream.tracker;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public final class TrackerClient {

	@SuppressWarnings("unused")
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
	
	public static String getLibrary() {
		
		String responseJson = "";
		
		try {
			HttpResponse response = getRequest(LIB);
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		return responseJson;
		
	}
	
	public static String join(TrackerPeer peer) {
		
		String responseJson = "";
		
		try {
			HttpResponse response = postRequest(JOIN, new StringEntity(getJson(peer)));
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		return responseJson;
		
	}
	
	public static String remove(TrackerPeer peer) {
		
		String responseJson = "";

		try {
			HttpResponse response = postRequest(REMOVE, new StringEntity(getJson(peer.getId())));
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		return responseJson;
		
	}
	
	private static HttpResponse getRequest(String url) throws IOException {
		
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		return response;
		
	}
	
	private static HttpResponse postRequest(String url, StringEntity params) throws IOException {
		
		HttpPost post = new HttpPost(url);
		post.setEntity(params);
		post.setHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(post);
		return response;
		
	}
	
	private static String getJson(Object obj) {
		return json.toJson(obj);
	}
	
}
