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

import com.cstream.utils.UrlBank;


public class TrackerClient {

	private static Logger LOGGER = Logger.getLogger(TrackerClient.class.getName());
	private HttpClient client;
	
	public TrackerClient() {
		client = HttpClientBuilder.create().build();
	}
	
	/**
	 * Takes a url and does an HTTP get request. Returns the response as a string.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private HttpResponse getRequest(String url) throws IOException {
		
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		return response;
		
	}
	
	/**
	 * Takes a url and parameters and does an HTTPS post request
	 * 
	 * @param url
	 * @param urlParameters
	 * @return
	 * @throws IOException
	 */
	private HttpResponse postRequest(String url, StringEntity parms) throws IOException {
		
		HttpPost post = new HttpPost(url);
		post.setEntity(parms);
		post.setHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(post);
		return response;
		
	}
	
	/**
	 * Get request for the library of available files. Returns the JSON response unparsed
	 * in a string or null if the request failed.
	 * 
	 * @return responseJson
	 */
	public String getLibrary() {
		
		String responseJson = "";
		
		try {
			HttpResponse response = getRequest(UrlBank.getLib);
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
			
		} catch (IOException e) {
			//TODO: Exception handling
			e.printStackTrace();
		}
		
		return responseJson;
		
	}
	
	/**
	 * Takes a peer and posts its data in JSON to the tracker. Returns the JSON response
	 * unparsed in a string or null if the request failed. Posts to add peer to tracker
	 * with their files.
	 * 
	 * @param peer
	 * @return
	 */
	public String postJoin(TrackerPeer peer) {
		
		String responseJson = "";
		
		try {
			HttpResponse response = postRequest(UrlBank.postJoin, new StringEntity(peer.toJson()));
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
			
		} catch (IOException e) {
			//TODO: Exception handling
			e.printStackTrace();
		}
		
		return responseJson;
		
	}
	
	/**
	 * Takes a peer and posts its ID in JSON to the tracker. Returns the JSON response
	 * unparsed in a string or null if the request failed. Posts to remove peer from
	 * tracker.
	 * 
	 * @param peer
	 * @return
	 */
	public String postRemove(TrackerPeer peer) {
		
		String responseJson = "";

		try {
			HttpResponse response = postRequest(UrlBank.postJoin, new StringEntity(peer.userIdToJson()));
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
			
		} catch (IOException e) {
			//TODO: Exception handling
			e.printStackTrace();
		}
		
		return responseJson;
		
	}
}
