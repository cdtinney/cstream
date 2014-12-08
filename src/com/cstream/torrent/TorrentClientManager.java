package com.cstream.torrent;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.cstream.client.HttpTransferClient;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

public class TorrentClientManager {

	private static Logger LOGGER = Logger.getLogger(TorrentClientManager.class.getName());
	
	// Constants
	private static final int MAX_CLIENTS = 5;

	// Singleton instance
	private static TorrentClientManager instance = null;
	
	// Map current clients to the torrents they are sharing
	private Map<Client, SharedTorrent> clients = new HashMap<Client, SharedTorrent>();
	
	// Torrent manager
	private TorrentManager torrentManager = TorrentManager.getInstance();
	
	// Private constructor so no other instances can be created
	private TorrentClientManager() {}
	
	// Global access point for this class
	public static TorrentClientManager getInstance() {
		
		if (instance == null) {
			instance = new TorrentClientManager();
		}
		
		return instance;
		
	}

	public void shareAll() {
		
		//new Thread(() -> {
			
			List<Torrent> torrents = torrentManager.getQueue();
			for (Torrent t : torrents) {
				
				String outputDir = TorrentManager.FILE_DIR;
				
				try {
					
					LOGGER.info("Creating new shared torrent: " + t.getName());
					
					SharedTorrent st = new SharedTorrent(t, new File(outputDir));
					Client c = new Client(InetAddress.getLocalHost(), st);
					
					if (c.getTorrent() == null) {
						LOGGER.warning("Client created without torrent: " + st.getName());
						continue;
					}
					
					if (clients == null) {
						LOGGER.warning("clients map is null");
						continue;
					}
					
					LOGGER.info("Uploading torrent to tracker: " + t.getName());
					
					boolean success = HttpTransferClient.uploadTorrent(st.getName(), st.getEncoded(), "192.168.1.109", "6970");
					if (success) {
						clients.put(c, c.getTorrent());				
						c.share();		
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					
				}
				
			}
			
		//}).start();
		
	}
	
}
