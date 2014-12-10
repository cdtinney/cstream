package com.cstream.client;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

public class TorrentClientManager implements Observer {

	private static Logger LOGGER = Logger.getLogger(TorrentClientManager.class.getName());
	
	private static final int MAX_CLIENTS = 5;

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
			
			if (c.getTorrent().getHexInfoHash().equals(torrent.getHexInfoHash())) {
				return c;
			}
			
		}
		
		return null;		
		
	}

	public void stop() {

		// Stop the client gracefully
		clients.keySet().forEach((client) -> client.stop());
		
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
			
			LOGGER.info("Creating new torrent for already created shared torrent");
			Torrent t = Torrent.load(new File(TorrentManager.TORRENT_DIR + torrent.getName() + ".torrent"));
			SharedTorrent st = new SharedTorrent(t, new File(TorrentManager.FILE_DIR));
			
			torrent = null;

			// Create a new client object to share the torrent with
			Client client = new Client(InetAddress.getLocalHost(), st);
			client.addObserver(this);
			
			client.share();
			clients.put(client, st);
			
		
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
	
	public void register(TorrentActivityListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void update(Observable object, Object property) {
		
		if (!(object instanceof Client)) {
			return;
		}
		
		Client c = (Client) object;		
		ClientState state = c.getState();
		SharedTorrent t = c.getTorrent();		
		
		if (state == ClientState.DONE || state == ClientState.SEEDING) {
			TorrentManager.getInstance().addCompletedTorrent(t);
		}
		
		fireTorrentChangedEvent(t);
		
	}
	
	private void fireTorrentChangedEvent(SharedTorrent torrent) {
		
		for (TorrentActivityListener listener : listeners) {
			LOGGER.info("Firing torrent changed event");
			listener.handleTorrentChanged(torrent);
		}
		
	}
	
}
