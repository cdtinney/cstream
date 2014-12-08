package com.cstream.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.cstream.torrent.TorrentManager;
import com.cstream.util.FileUtils;

public class HttpTransferClient {

	private static Logger LOGGER = Logger.getLogger(HttpTransferClient.class.getName());
	
	private static final String UPLOAD_CONTEXT = "/upload/";
	private static final String DOWNLOAD_CONTEXT = "/download/";
	
	private static RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2 * 1000).build();
	private static HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();	
	
	public HttpTransferClient() { }
	
	public static boolean uploadTorrent(String name, byte[] torrentBytes, String ip, String port) {
		
		String url = "http://" + ip + ":" + port + UPLOAD_CONTEXT;
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
	
	public static boolean downloadTorrents(String ip, String port) {

		String url = "http://" + ip + ":" + port + DOWNLOAD_CONTEXT;
		HttpResponse response = get(url);
		
		int status = response.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_OK) {
			LOGGER.warning("GET to /download returned: " + status);
			return false;
		}

		LOGGER.warning("GET to /download returned: " + status);
		
		try {
			 parseDownloadResponse(response);	
			 return true;
			
		} catch (ZipException e) {
			LOGGER.warning("ZipException: " + e.getMessage());
			
		}

		return false;
		
	}
	
	private static void parseDownloadResponse(HttpResponse response) throws ZipException {
				
		try {

			File temp = FileUtils.getFile(TorrentManager.TORRENT_TMP_DIR + "torrents.zip");
			LOGGER.info("Creating temporary zip file at: " + temp.getAbsolutePath());
			
			if (response == null || response.getEntity() == null || response.getEntity().getContent() == null) {
				LOGGER.warning("Torrent download returned no response");
				return;
			}
			
			BufferedInputStream input = new BufferedInputStream(response.getEntity().getContent());
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(temp));
			
			int inByte;
			while ((inByte = input.read()) != -1) { 
				output.write(inByte);
			}
			
			input.close();
			output.close(); 
			
			ZipFile zip = new ZipFile(temp.getAbsolutePath());
			if (!zip.isValidZipFile()) {
				throw new ZipException("HTTP client received an invalid torrent zip from the server");
			}
			
			LOGGER.info("Extracting " + zip.getFileHeaders().size() + " .torrent files..");
			
			// TODO - Do not overwrite current .torrent files...
			zip.extractAll(TorrentManager.TORRENT_DIR);		
			
			LOGGER.info("Torrent files successfully extracted to: " + TorrentManager.TORRENT_DIR);
			
			LOGGER.info("Deleting temporary zip file at: " + temp.getAbsolutePath() + "...");
			boolean zipDeleted = temp.delete();
			boolean tmpDeleted = temp.getParentFile().delete();
			if (!zipDeleted || !tmpDeleted) {
				LOGGER.warning("Failed to delete temporary zip file at: " + temp.getAbsolutePath());
				return;
			}

			LOGGER.info("Temporary zip file at: " + temp.getAbsolutePath() + " deleted successfully");
			
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			
		}
		
	}
	
	private static HttpResponse get(String url) {

		try {
			
			HttpGet get = new HttpGet(url);			
			get.setHeader("User-Agent", "cstream");
			
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
