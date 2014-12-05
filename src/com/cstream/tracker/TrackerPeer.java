package com.cstream.tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

import com.cstream.model.Song;
import com.cstream.utils.LibraryUtils;
import com.cstream.utils.OSUtils;
import com.cstream.utils.logging.LogLevel;

public class TrackerPeer {

	private static Logger LOGGER = Logger.getLogger(TrackerPeer.class.getName());
	
	// TODO: Link Port and IP to the RTP Session
	private String id;
	private String ip;
	private String port;
	
	private Map<String, Song> files;
	
	public TrackerPeer(String pathToLib) {
		
		this.id = generateId();
		this.port = "23499";		//Standard client port
		this.ip = getLocalIp();
		this.files = buildSongMap(pathToLib);
		
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
	
	public boolean joinTracker() {
		return TrackerClient.join(this);
	}
	
	public boolean removeTracker() {
		return TrackerClient.remove(this);
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
	
	private Map<String, Song> buildSongMap(String path) {
		
		Map<String, Song> library = null;
		
		if(!path.isEmpty()) {
			library = LibraryUtils.buildLocalLibrary(path);
		} else {
			LOGGER.log(LogLevel.DEBUG, "Failed to build song map because users lib path is empty");
		}
		
		return library;
	}

}
