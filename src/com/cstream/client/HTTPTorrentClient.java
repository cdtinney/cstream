package com.cstream.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.cstream.util.FileUtils;
import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.common.Torrent;

public class HTTPTorrentClient {

	private static Logger LOGGER = Logger.getLogger(HTTPTorrentClient.class.getName());
	
	private static final String UPLOAD_CONTEXT = "/upload/";
	private static final String DOWNLOAD_CONTEXT = "/download/";
	
	private static RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2 * 1000).build();
	private static HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();	
	
	public HTTPTorrentClient() { }
	
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

		LOGGER.info("POST to /upload returned: " + status);
		return true;
		
	}
	
	public static Map<String, Torrent> downloadTorrents(String ip, String port) {

		Map<String, Torrent> results = new HashMap<String, Torrent>();
		String url = "http://" + ip + ":" + port + DOWNLOAD_CONTEXT;
		
		HttpResponse response = get(url);	
		if (response == null) {
			LOGGER.warning("GET to /download returned null response");
			return results;
		}
		
		int status = response.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_OK) {
			LOGGER.warning("GET to /download returned: " + status);
			return results;
		}

		LOGGER.info("GET to /download returned: " + status);
		
		try {
			 return parseDownloadResponse(response);	
			
		} catch (ZipException e) {
			LOGGER.warning("ZipException: " + e.getMessage());
			
		}

		return results;
		
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Torrent> parseDownloadResponse(HttpResponse response) throws ZipException {
		
		Map<String, Torrent> results = new HashMap<String, Torrent>();
		
		try {
			
			if (response == null || response.getEntity() == null || response.getEntity().getContent() == null) {
				LOGGER.warning("Torrent download returned no response");
				return results;
			}

			File temp = FileUtils.getFile(TorrentManager.TORRENT_TMP_DIR + "torrents.zip");
			ZipFile zip = FileUtils.unzip(response.getEntity().getContent(), temp);
			if (!zip.isValidZipFile()) {
				throw new ZipException("HTTP client received an invalid torrent zip from the server");
			}
			
			Map<String, Torrent> torrents = new HashMap<String, Torrent>();

			LOGGER.info("Extracting " + zip.getFileHeaders().size() + " .torrent files..");
			List<FileHeader> headers = zip.getFileHeaders();
			for (FileHeader h : headers) {
				
				// Convert the input stream to a byte array, which we can then compute
				// the torrent info hash from.
				InputStream is = zip.getInputStream(h);
				byte[] bytes = IOUtils.toByteArray(is);
				String hexInfoHash = getInfoHash(bytes);
				
				// The client is already storing this torrent - ignore it
				if (TorrentManager.getInstance().getTorrents().get(hexInfoHash) != null) {
					LOGGER.info("Tracker returned a torrent we already have - ignoring it - hash: " + hexInfoHash);
					
				} else {
					
					// Create a new torrent object, extract the file, and add it to the results map
					Torrent t = new Torrent(bytes, false);
					zip.extractFile(h.getFileName(), TorrentManager.TORRENT_DIR);
					torrents.put(t.getHexInfoHash(), t);
					
				}
				
				// We need to close the file handler regardless of whether we loaded the torrent or not,
				// otherwise we cannot delete the temporary .zip file
				if (is != null){
					is.close();
					is = null;
				}
				
			}
			
			if (!temp.delete() || !temp.getParentFile().delete()) {
				LOGGER.warning("Failed to delete temporary zip file at: " + temp.getAbsolutePath());
			}
			
			return torrents;
			
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			
		}
		
		return results;
		
	}
	
	/*
	 * Parses torrent meta-info binary to determine the info hash. This is easier
	 * than creating an entirely new Torrent object simply to check the hash.
	 */
	private static String getInfoHash(byte[] bytes) throws InvalidBEncodingException, IOException {
		
		Map<String, BEValue> decoded = BDecoder.bdecode(new ByteArrayInputStream(bytes)).getMap();
		
		byte[] encoded_info;
		Map<String, BEValue> decoded_info;
		
		byte[] info_hash;
		String hex_info_hash;
		
		decoded_info = decoded.get("info").getMap();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BEncoder.bencode(decoded_info, baos);
		encoded_info = baos.toByteArray();
		info_hash = Torrent.hash(encoded_info);
		hex_info_hash = Torrent.byteArrayToHexString(info_hash);
		
		// Close the output stream!
		if (baos != null) {
			baos.close();
			baos = null;
		}
		
		return hex_info_hash;
		
	}
	
	private static HttpResponse get(String url) {

		try {
			
			HttpGet get = new HttpGet(url);			
			get.setHeader("User-Agent", "cstream");
			
			HttpResponse response = client.execute(get);
			return response;
			
		} catch (HttpHostConnectException e) {
			LOGGER.warning(e.getMessage());

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
			
		} catch (SocketException | ConnectTimeoutException e) {
			LOGGER.warning(e.getClass().getSimpleName() + ": " + e.getMessage());	

		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return null;

	}

}
