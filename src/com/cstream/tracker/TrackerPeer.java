package com.cstream.tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.cstream.model.Song;

public class TrackerPeer {

	// TODO: Link Port and IP to the RTP Session
	private String id;
	private String ip;
	private String port;
	
	private ArrayList<Song> files;
	
	public TrackerPeer(String id, String port, ArrayList<Song> files) {
		
		this.id = id;
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

	public ArrayList<Song> getFiles() {
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

}
