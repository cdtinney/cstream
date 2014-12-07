package com.ttorrent.tracker;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.turn.ttorrent.tracker.Tracker;

public class TorrentTracker {
	
	private static int PORT = 6969;
	
	private Tracker tracker;
	
	private TorrentTrackerServer httpServer;
	
	public TorrentTracker() {
		
	}
	
	public void start() {
		
		try {
			tracker = new Tracker(new InetSocketAddress(PORT));
			tracker.start();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
	}
	
	public static void main(String[] args) {
		
		TorrentTracker tracker = new TorrentTracker();
		tracker.start();
		
	}
	

}
