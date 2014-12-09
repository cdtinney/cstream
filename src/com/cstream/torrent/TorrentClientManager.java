package com.cstream.torrent;

import java.net.InetAddress;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.cstream.client.HttpTransferClient;
import com.cstream.media.LibraryView;
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
	
	public Client findClient(SharedTorrent torrent) {
		
		for (Client c : clients.keySet()) {
			
			if (c.getTorrent() == torrent) {
				return c;
			}
			
		}
		
		return null;		
		
	}

	public void shareAll(LibraryView view) {
		
		new Thread(() -> {
			
			ObservableList<SharedTorrent> shared = FXCollections.observableArrayList();
			
			for (SharedTorrent torrent : torrentManager.getTorrents().values()) {

				try {
					Client c = new Client(InetAddress.getLocalHost(), torrent);
					c.addObserver(this);
				
					shared.add(torrent);
	
					//c.download();
					
					// TODO - We want to always share local files. Regardless of whether the tracker is up/down at the moment, since the client
					// will resolve that.
					
					LOGGER.info("Uploading torrent to tracker: " + torrent.getName());
					boolean success = HttpTransferClient.uploadTorrent(torrent.getName(), torrent.getEncoded(), TRACKER_IP, TRACKER_PORT);
					if (success) {					
						LOGGER.info("Torrent uploaded successfully: " + torrent.getName());					
						clients.put(c, c.getTorrent());	
						c.share();		
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					
				}
				
			}
			
			view.setItems(shared);
			
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
