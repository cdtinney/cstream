package com.cstream.tracker;

import java.util.Map;
import java.util.logging.Logger;

import com.cstream.model.Song;
import com.cstream.util.OSUtils;

public class TrackerPeer {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(TrackerPeer.class.getName());
	
	private final String HTTP_PORT = "8080";
	private final String TCP_PORT = "9000";
	
	private String id;
	private String ip;
	
	private String httpPort;
	private String tcpPort;
	
	private Map<String, Song> files;
	
	public TrackerPeer() {
		
		this.id = OSUtils.generateUserId();
		this.ip = OSUtils.getLocalIp();
		
		this.httpPort = HTTP_PORT;
		this.tcpPort = TCP_PORT;
		
	}
	
	public TrackerPeer(Map<String, Song> files) {
		this();
		
	}

	public String getId() {
		return id;
	}
	
	public String getIp() {
		return ip;
	}
	
	public String getTcpPort() {
		return tcpPort;
	}

	public String getHttpPort() {
		return httpPort;
	}

	public Map<String, Song> getFiles() {
		return files;
	}
	
	public void setFiles(Map<String, Song> files) {
		this.files = files;
	}

}
