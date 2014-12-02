package com.cstream.tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.cstream.model.Song;
import com.google.gson.Gson;

public class TrackerPeer {

	// TODO: Link Port and IP to the RTP Session
	private String userId;
	private String userIP;
	private String userPort;
	private ArrayList<Song> usersFiles;
	
	public TrackerPeer(String id, ArrayList<Song> files) throws UnknownHostException {
		setUserId(id);
		setUserIP();
		setUsersFiles(files);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void setUserIP() throws UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		this.userIP = ip.getHostAddress();
	}
	
	public String getUserIP() {
		return this.userIP;
	}

	public String getUserPort() {
		return userPort;
	}

	public void setUserPort(String userPort) {
		this.userPort = userPort;
	}

	public ArrayList<Song> getUsersFiles() {
		return usersFiles;
	}

	public void setUsersFiles(ArrayList<Song> usersFiles) {
		this.usersFiles = usersFiles;
	}
	
	public String toJson() {
		Gson json = new Gson();
		json.toJson(this);
		return json.toString();
	}
	
	public String userIdToJson() {
		Gson json = new Gson();
		json.toJson(getUserId());
		return json.toString();
	}

}
