package com.cstream.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.cstream.song.Song;
import com.cstream.util.FileUtils;
import com.cstream.util.LibraryUtils;
import com.cstream.util.OSUtils;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

public class TorrentManager {
	
	private static Logger LOGGER = Logger.getLogger(TorrentManager.class.getName());
	
	public final static String FILE_DIR = System.getProperty("user.home") + (OSUtils.isWindows() ? "\\cstream\\" : "/cstream/");
	public final static String TORRENT_DIR = FILE_DIR + (OSUtils.isWindows() ? "torrent\\" : "torrent/");
	public final static String TORRENT_TMP_DIR = FILE_DIR + (OSUtils.isWindows() ? "torrent\\tmp\\" : "torrent/tmp/");
	
	public final static String TRACKER_IP = "192.168.1.109";
	public final static String TRACKER_HTTP = "6970";
	public final static String TRACKER_TCP = "6969";
	public final static String TRACKER_ANNOUNCE = TRACKER_IP + ":" + TRACKER_TCP + "/announce";

	private static TorrentManager instance = null;
	
	private Map<String, Song> songs;
	
	private ConcurrentHashMap<String, SharedTorrent> torrents;
	
	private List<TorrentActivityListener> listeners;
	
	private Thread collector;
	private boolean stop;
	
	private TorrentManager() { 
		
		this.songs = new ConcurrentHashMap<String, Song>();		
		this.torrents = new ConcurrentHashMap<String, SharedTorrent>();	
		
		this.listeners = new ArrayList<TorrentActivityListener>();
		
	}
	
	public static TorrentManager getInstance() {
		
		if (instance == null) {
			instance = new TorrentManager();
		}
		
		return instance;
		
	}
	
	public void register(TorrentActivityListener listener) {
		this.listeners.add(listener);
	}
	
	public void start() {
		
		// Load in all of our local song files
		this.songs = LibraryUtils.buildLocalLibrary(TorrentManager.FILE_DIR);
		
		// Load in all of our local .torrent files
		Map<String, Torrent> torrents = loadTorrents();
		
		// Store the number of torrents we already have
		int numTorrents = torrents.values().size();
		
		// Find any song files that have no corresponding torrent file. A match is denoted
		// by an identical file name for now. TODO encode bytes -> check hex_info_hash
		for (String songName : this.songs.keySet()) {
			
			Torrent existing = torrents.get(songName);
			if (existing != null) {
				LOGGER.info("Torrent already exists: " + songName);
				continue;
			}
			
			// Create a new .torrent file, and load it into a new Torrent object
			// TODO - Change createdBy to user.name!
			//File torrentFile = createTorrentFile(songName);
			File song = new File(FILE_DIR + this.songs.get(songName).getPath());
			Torrent torrent = loadTorrentFromFile(song, "createdBy");
			
			// Add the torrent to our temporary map
			torrents.put(torrent.getHexInfoHash(), torrent);
			
		}
		
		int newTorrents = torrents.values().size() - numTorrents;
		if (newTorrents != 0) {
			LOGGER.info(newTorrents + " new torrents created.");
		}
		
		// Convert all of the Torrent objects to SharedTorrent, initialize
		// them, and store in our map.
		for (Torrent t : torrents.values()) {

			try {
				
				LOGGER.info("Creating new shared torrent: " + t.getName());				
				SharedTorrent st = new SharedTorrent(t, new File(FILE_DIR));
				try {
					st.init();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					
				}
				
				this.torrents.put(st.getHexInfoHash(), st);
				fireTorrentAddedEvent(st);
				
			} catch (IOException e) {
				e.printStackTrace();
				
			}
			
		}
		
		LOGGER.info(this.torrents.values().size() + " shared torrents created.");
		
		// Start the collector thread
		if (this.collector == null || !this.collector.isAlive()) {
			this.collector = new TorrentCollectorThread();
			this.collector.setName("torrent-collector");
			this.collector.start();
		}
		
		// Start the torrent upload on a new thread
		Thread upload = new Thread(new TorrentUpload());
		upload.setName("torrent-upload");
		upload.start();
		
	}
	
	public void stop() {
		this.stop = true;

		if (this.collector != null && this.collector.isAlive()) {
			this.collector.interrupt();
			LOGGER.info("Torrent collection terminated.");
		}
		
	}
	
	public ConcurrentHashMap<String, SharedTorrent> getTorrents() {
		return torrents;
	}
	
	public void addCompletedTorrent(SharedTorrent torrent) {

		Song song = new Song(torrent.getName(), FILE_DIR + torrent.getName());
		
		if (song.getMp3() == null) {		
			LOGGER.warning("Mp3 Object or Song Id were not created correctly");	
			return;			
		} 
		
		songs.put(song.getPath(), song);
		
	}
	
	public Map<String, Song> getSongs() {
		return songs;
	}
	
	private Map<String, Torrent> loadTorrents() {

		List<File> torrentFiles = new ArrayList<File>(FileUtils.listFiles(TORRENT_DIR, new String[] {".torrent"}));
		
		Map<String, Torrent> torrents = new HashMap<String, Torrent>();
		for (File f : torrentFiles) {
			
			try {
				Torrent t = Torrent.load(f);
				torrents.put(t.getName(), t);
				
			} catch (IOException e) {
				e.printStackTrace();
				
			}
			
		}
		
		LOGGER.info(torrents.size() + " torrents loaded from: " + TORRENT_DIR);		
		return torrents;
		
	}
	
	private Torrent loadTorrentFromFile(File file, String createdBy) {
		
		try {
			
			// File name includes the extension, which is necessary
			String fileName = file.getName();
			
			// Load .torrent files that already exist
			File existingFile = new File(TORRENT_DIR + fileName + ".torrent");
			if (existingFile.exists()) {
				LOGGER.info("Loading existing .torrent file for: " + fileName);
				return Torrent.load(existingFile);
			}
			
			LOGGER.info("Creating new .torrent file for: " + fileName);			
			
			// Create the new torrent object from our song file, and write it to a .torrent file
			Torrent t = Torrent.create(file, new URI("http://" + TRACKER_ANNOUNCE), createdBy);
			OutputStream output = new FileOutputStream(createTorrentFile(fileName));
			t.save(output);
			
			return t;
			
		} catch (InterruptedException | IOException | URISyntaxException e) {
			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
	private File createTorrentFile(String filename) {
		return new File(TorrentManager.TORRENT_DIR + filename + ".torrent");	
	}
	
	private void fireTorrentAddedEvent(SharedTorrent torrent) {
		
		for (TorrentActivityListener listener : listeners) {
			listener.handleTorrentAdded(torrent);
		}
		
	}

	private class TorrentCollectorThread extends Thread {

		private static final int TORRENT_COLLECTION_FREQUENCY = 30;

		@Override
		public void run() {
			
			LOGGER.info("Starting torrent collection from tracker: " + TRACKER_IP + ":" + TRACKER_HTTP);

			while (!stop) {
				
				LOGGER.info("Collecting torrents from tracker: " + TRACKER_IP + ":" + TRACKER_HTTP + " ... ");
				
				// TODO - Move checking for local torrents into this class rather than the HTTP client parser
				Map<String, Torrent> downloaded = HTTPTorrentClient.downloadTorrents(TRACKER_IP, TRACKER_HTTP);
				LOGGER.info(downloaded.size() + " new torrents downloaded from tracker.");		
				
				for (Torrent t : downloaded.values()) {

					LOGGER.info("Creating new shared torrent: " + t.getName());	
					try {
						SharedTorrent st = new SharedTorrent(t, new File(FILE_DIR));
						torrents.put(st.getHexInfoHash(), st);
						fireTorrentAddedEvent(st);
						
					} catch (Exception e) {
						e.printStackTrace();
						
					}
					
				}

				try {
					Thread.sleep(TorrentCollectorThread.TORRENT_COLLECTION_FREQUENCY * 1000);
					
				} catch (InterruptedException ie) {
					// Ignore
					
				}
				
			}
			
		}
		
	}
	
	private class TorrentUpload implements Runnable {

		@Override
		public void run() {
			
			for (SharedTorrent torrent : torrents.values()) {
				
				LOGGER.info("Uploading torrent to tracker: " + torrent.getName());
				boolean success = HTTPTorrentClient.uploadTorrent(torrent.getName(), torrent.getEncoded(), TRACKER_IP, TRACKER_HTTP);
				if (success) {					
					LOGGER.info("Torrent uploaded successfully: " + torrent.getName());	
				}
				
			}
			
		}
		
	}

}
