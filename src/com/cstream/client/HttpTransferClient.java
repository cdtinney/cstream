package com.cstream.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import com.cstream.torrent.TorrentManager;

public class HttpTransferClient {

	private static Logger LOGGER = Logger.getLogger(HttpTransferClient.class.getName());
	
	private static HttpClient client = HttpClientBuilder.create().build();	
	
	public HttpTransferClient() {		
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

}
