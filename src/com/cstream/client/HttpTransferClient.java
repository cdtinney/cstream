package com.cstream.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import com.cstream.torrent.TorrentManager;

public class HttpTransferClient {

	private static Logger LOGGER = Logger.getLogger(HttpTransferClient.class.getName());
	
	private static RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2 * 1000).build();
	private static HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();	
	
	public HttpTransferClient() { }
	
	public static boolean uploadTorrent(String name, byte[] torrentBytes, String ip, String port) {
		
		String url = "http://" + ip + ":" + port + "/upload/";
		HttpResponse response = post(url, name, torrentBytes);	
		
		if (response == null) {
			LOGGER.warning("POST to /upload returned null response");
			return false;
		}
		
		int status = response.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_OK) {
			LOGGER.warning("POST to /upload returned: " + status);
			return false;
		}

		LOGGER.warning("POST to /upload returned: " + status);
		return true;
		
	}
	
	public static void requestTorrent(String ip, String port, String songId) {
			
		String url = "http://" + ip + ":" + port;
		
		HttpResponse response = get(url, new BasicHeader("User-Agent", "cstream/1.0"), new BasicHeader("songId", songId));
		parseResponse(response);		
		
	}
	
	private static void parseResponse(HttpResponse response) {
				
		try {
			
			Header header = response.getFirstHeader("filename");
			if (header == null) {
				LOGGER.warning("Response header did not contain a valid filename");
				return;
			}
			
			String fileName = header.getValue();
			
			BufferedInputStream input = new BufferedInputStream(response.getEntity().getContent());
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(TorrentManager.createTorrentFile(fileName)));
			
			int inByte;
			while ((inByte = input.read()) != -1) { 
				output.write(inByte);
			}
			
			input.close();
			output.close(); 
			
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			
		}
		
	}
	
	private static HttpResponse get(String url, Header... headers) {

		try {
			
			HttpGet get = new HttpGet(url);
			
			for (Header h : headers) {
				get.addHeader(h);
			}
			
			HttpResponse response = client.execute(get);
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return null;

	}
	
	private static HttpResponse post(String url, String name, byte[] torrentBytes) {

		
		try {
			
			HttpPost post = new HttpPost(url);
			
			HttpEntity entity = new ByteArrayEntity(torrentBytes);
			post.setEntity(entity);
			
			post.setHeader("filename", name);
			post.setHeader("User-Agent", "cstream");
			
			HttpResponse response =  client.execute(post);
			
			// We need to release the connection immediately or else multiple POST requests to the same server fail
			post.releaseConnection();
			
			return response;
			
		} catch (SocketException e) {
			LOGGER.warning("SocketException: " + e.getMessage());			

		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return null;

	}

}
