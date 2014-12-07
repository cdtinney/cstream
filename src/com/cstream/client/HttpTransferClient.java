package com.cstream.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

public class HttpTransferClient {
	
	private HttpClient client = HttpClientBuilder.create().build();	
	
	public void requestDownload(String ip, String port, String songId, String filePath, int startRange, int endRange) {
		
		String url = ip + ":" + port + "/" + songId;
		Header header = new BasicHeader("User-Agent", "cstream/1.0");
		
		HttpResponse response = get(url, header);
		parseResponse(response, filePath);		
		
	}
	
	private void parseResponse(HttpResponse response, String filePath) {
				
		try {
			
			BufferedInputStream input = new BufferedInputStream(response.getEntity().getContent());
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
			
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
	
	private HttpResponse get(String url, Header header) {

		try {
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return null;

	}

}
