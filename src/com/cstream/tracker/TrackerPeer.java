package com.cstream.tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import com.cstream.model.Song;
import com.cstream.utils.OSUtils;

public class TrackerPeer {

	// TODO: Link Port and IP to the RTP Session
	private String id;
	private String ip;
	private String port;
	
	private Map<String, Song> files;
	
	public TrackerPeer(String port, Map<String, Song> files) {
		
		this.id = generateId();
		this.port = port;
		
		this.ip = getLocalIp();
		
		this.files = files;
		
	}

	public String getId() {
		return id;
	}
	
	public String getIp() {
		return ip;
	}

	public String getPort() {
		return port;
	}

	public Map<String, Song> getFiles() {
		return files;
	}
	
	private String getLocalIp() {
		
		try {
			InetAddress ip = InetAddress.getLocalHost();
			return ip.getHostAddress();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
	private String generateId() {
		
		String os = "unknown";
		String name = System.getProperty("user.home");
		
		if(OSUtils.isMac()) {
			os = "mac";
		} else if(OSUtils.isWindows()) {
			os = "windows";
		} else if(OSUtils.isUnix()) {
			os = "unix";
		}
		
		return name + "_" + os;
	}

}
