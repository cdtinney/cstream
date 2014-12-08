package com.cstream.torrent;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.cstream.client.HttpTransferClient;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

public class TorrentClientManager implements Observer {

	private static Logger LOGGER = Logger.getLogger(TorrentClientManager.class.getName());
	
	private static final int MAX_TORRENTS = 5;
	private static final String TRACKER_IP = "192.168.1.109";
	private static final String TRACKER_PORT = "6970";

	// Singleton instance
	private static TorrentClientManager instance = null;
	
	// Map current clients to the torrents they are sharing
	private Map<Client, SharedTorrent> clients = new ConcurrentHashMap<Client, SharedTorrent>();
	
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
		
		new Thread(() -> {
			
			List<Torrent> torrents = torrentManager.getQueue();
			for (Torrent t : torrents) {
				
				String outputDir = TorrentManager.FILE_DIR;
				
				try {
					
					LOGGER.info("Creating new shared torrent: " + t.getName());
					
					SharedTorrent st = new SharedTorrent(t, new File(outputDir));
					Client c = new Client(InetAddress.getLocalHost(), st);
					c.addObserver(this);
					
					// TODO - We want to always share local files. Regardless of whether the tracker is up/down at the moment, since the client
					// will resolve that.
					
					LOGGER.info("Uploading torrent to tracker: " + t.getName());
					boolean success = HttpTransferClient.uploadTorrent(st.getName(), st.getEncoded(), TRACKER_IP, TRACKER_PORT);
					if (success) {					
						LOGGER.info("Torrent uploaded successfully: " + t.getName());
						//c.addObserver(this);						
						clients.put(c, c.getTorrent());	
						c.share();		
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					
				}
				
			}
			
		}).start();
		
	}

	@Override
	public void update(Observable object, Object property) {
		
		if (!(object instanceof Client)) {
			return;
		}
		
		Client c = (Client) object;
		ClientState state = (ClientState) property;
		
		Torrent t = c.getTorrent();
		LOGGER.info("Torrent state changed: " + t.getName() + " to " + state.toString());
		
		// TODO - Publish state to UI		
		
	}
	
}
