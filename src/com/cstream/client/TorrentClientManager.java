package com.cstream.client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.cstream.media.LibraryView;
import com.cstream.media.MediaController;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

public class TorrentClientManager implements Observer {

	private static Logger LOGGER = Logger.getLogger(TorrentClientManager.class.getName());
	
	private static final int MAX_CLIENTS = 5;
	
	private static final String TRACKER_IP = "192.168.1.109";
	//private static final String TRACKER_IP = "192.168.1.100";
	private static final String TRACKER_PORT = "6970";

	// Singleton instance
	private static TorrentClientManager instance = null;
	
	// Map current clients to the torrents they are sharing
	private Map<Client, SharedTorrent> clients = new ConcurrentHashMap<Client, SharedTorrent>();
	
	private TorrentManager torrentManager = TorrentManager.getInstance();

	private List<TorrentActivityListener> listeners;
	
	// Private constructor so no other instances can be created
	private TorrentClientManager() {
		this.listeners = new ArrayList<TorrentActivityListener>();
	}
	
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
	
	public void share(SharedTorrent torrent) {
		
		if (clients.keySet().size() == MAX_CLIENTS) {
			LOGGER.warning("Maximum number of clients reached.");
			return;			
		}
		
		try {
		
			Client c = findClient(torrent);
			if (c != null) {
				LOGGER.warning("Torrent is already being shared: " + torrent.getName());
				return;
			}

			// Create a new client object to share the torrent with
			Client client = new Client(InetAddress.getLocalHost(), torrent);
			client.addObserver(this);
			
			
			client.share();
			clients.put(client, torrent);
			
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
					
	}
	
	public void stopShare(SharedTorrent torrent) {
			
		try {
			
			Client client = findClient(torrent);
			if (client == null) {
				LOGGER.warning("Torrent is not being shared: " + torrent.getName());
				return;
			}
			
			client.stop(true);
			clients.remove(client);
			LOGGER.info("Torrent stopped successfully: " + torrent.getName());
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}

	}

	public void shareAll(LibraryView view) {
		
		new Thread(() -> {
			
			ObservableList<SharedTorrent> shared = FXCollections.observableArrayList();
			
			for (SharedTorrent torrent : torrentManager.getTorrents().values()) {

				try {
					
					Client c = new Client(InetAddress.getLocalHost(), torrent);
					c.addObserver(this);
				
					shared.add(torrent);	
					
					LOGGER.info("Uploading torrent to tracker: " + torrent.getName());
					boolean success = HTTPTorrentClient.uploadTorrent(torrent.getName(), torrent.getEncoded(), TRACKER_IP, TRACKER_PORT);
					if (success) {					
						LOGGER.info("Torrent uploaded successfully: " + torrent.getName());					
						clients.put(c, c.getTorrent());	
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					
				}
				
			}
			
			view.setItems(shared);
			
		}).start();
		
	}
	
	public void register(TorrentActivityListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void update(Observable object, Object property) {
		
		if (!(object instanceof Client)) {
			return;
		}
		
		Client c = (Client) object;		
		SharedTorrent t = c.getTorrent();		
		fireTorrentChangedEvent(t);
		
	}
	
	private void fireTorrentChangedEvent(SharedTorrent torrent) {
		
		for (TorrentActivityListener listener : listeners) {
			LOGGER.info("Firing torrent changed event");
			listener.handleTorrentChanged(torrent);
		}
		
	}
	
}
