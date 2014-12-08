package com.cstream.tracker;

import java.util.Map;
import java.util.logging.Logger;

import com.cstream.model.Song;

public final class TrackerClient {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(TrackerClient.class.getName());
	
	// Model classes
	private TrackerPeer peer;
	private Map<String, Song> sharedLibrary;

	@SuppressWarnings("unused")
	private TrackerClient() { }
	
	public TrackerClient(TrackerPeer peer) {
		this.peer = peer;
	}
	
	public TrackerPeer getPeer() {
		return peer;
	}
	
	public void setFiles(Map<String, Song> files) {
		sharedLibrary = files;
		peer.setFiles(files);
	}
	
}
