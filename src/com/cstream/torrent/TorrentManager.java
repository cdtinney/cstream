package com.cstream.torrent;

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

import com.cstream.client.HTTPTorrentClient;
import com.cstream.model.Song;
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
	
	public final static String TRACKER_URL = "http://192.168.1.109";
	public final static String TRACKER_HTTP = "6970";
	public final static String TRACKER_TCP = "6969";
	public final static String TRACKER_ANNOUNCE = TRACKER_URL + ":" + TRACKER_TCP + "/announce";

	private static TorrentManager instance = null;
	
	private Map<String, Song> songs;
	private ConcurrentHashMap<String, SharedTorrent> torrents;
	
	private Thread collector;
	private boolean stop;
	
	private TorrentManager() { 
		
		this.songs = new ConcurrentHashMap<String, Song>();		
		this.torrents = new ConcurrentHashMap<String, SharedTorrent>();		
		
	}
	
	public static TorrentManager getInstance() {
		
		if (instance == null) {
			instance = new TorrentManager();
		}
		
		return instance;
		
	}
	
	public void start() {
		
		// Load in all of our local song files
		this.songs = LibraryUtils.buildLocalLibrary(TorrentManager.FILE_DIR);
		
		// Load in all of our local .torrent files
		Map<String, Torrent> torrents = loadTorrents();
		
		int numTorrents = torrents.values().size();
		
		// Find any song files that have no corresponding torrent file. A match is denoted
		// by an identical file name for now. TODO encode bytes -> check hex_info_hash
		for (String songName : this.songs.keySet()) {
			
			Torrent existing = torrents.get(songName);
			if (existing != null) {
				LOGGER.info("Torrent already exists for song: " + songName);
				continue;
			}
			
			// Create a new .torrent file, and load it into a new Torrent object
			File torrentFile = createTorrentFile(songName);
			Torrent torrent = buildTorrentFromFile(torrentFile, "createdBy");
			
			// Add the torrent to our temporary map
			torrents.put(torrent.getHexInfoHash(), torrent);
			
		}
		
		int numNewTorrents = torrents.values().size() - numTorrents;
		if (numNewTorrents != 0) {
			LOGGER.info(numNewTorrents + " new torrents created.");
		}
		
		// Convert all of the Torrent objects to SharedTorrent, and put
		// them in our map.
		for (Torrent t : torrents.values()) {

			try {
				
				LOGGER.info("Creating new shared torrent: " + t.getName());				
				SharedTorrent st = new SharedTorrent(t, new File(FILE_DIR));
				try {
					st.init();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				this.torrents.put(st.getHexInfoHash(), st);
				
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
		
	}
	
	public void stop() {
		this.stop = true;
	}
	
	public ConcurrentHashMap<String, SharedTorrent> getTorrents() {
		return torrents;
	}
	
	public Map<String, Song> getSongs() {
		return songs;
	}
	
	public void addTorrentsFromLibrary(Map<String, Song> library, String createdBy) {
			
		for (Song s : library.values()) {
			
			String fileName = s.getPath();
			String path = FILE_DIR + fileName;
			File file = new File(path);
			
			Torrent t = buildTorrentFromFile(file, createdBy);
			if (t != null) {
				
				try {
					
					LOGGER.info("Creating new shared torrent: " + t.getName());
					SharedTorrent st = new SharedTorrent(t, new File(FILE_DIR));
					
					// TODO - Check if already in map?
					torrents.put(st.getHexInfoHash(), st);
					
				} catch (IOException e) {
					e.printStackTrace();
					
				} 
				
			}
			
		}
		
	}
	
	public static File createTorrentFile(String filename) {
		return new File(TorrentManager.TORRENT_DIR + filename + ".torrent");	
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
	
	private Torrent buildTorrentFromFile(File file, String createdBy) {
		
		try {
			
			// Strip the extension
			// TODO - If we strip the extension we don't know what to save it as
			//String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
			
			String fileName = file.getName();
			
			// Load .torrent files that already exist
			File existingFile = new File(TORRENT_DIR + fileName + ".torrent");
			if (existingFile.exists()) {
				LOGGER.info("Ignoring existing .torrent file: " + fileName);
				return Torrent.load(existingFile);
			}
			
			LOGGER.info("Creating .torrent file: " + fileName);			
			
			// Create the new torrent object and write to file
			Torrent t = Torrent.create(file, new URI(TRACKER_ANNOUNCE), createdBy);
			OutputStream output = new FileOutputStream(createTorrentFile(fileName));
			t.save(output);
			
			return t;
			
		} catch (InterruptedException | IOException | URISyntaxException e) {
			e.printStackTrace();
			
		}
		
		return null;
		
	}

	private class TorrentCollectorThread extends Thread {

		private static final int TORRENT_COLLECTION_FREQUENCY = 30;

		@Override
		public void run() {
			
			LOGGER.info("Starting torrent collection from tracker: " + TRACKER_URL + ":" + TRACKER_HTTP);

			while (!stop) {
				
				LOGGER.info("Collecting torrents from tracker: " + TRACKER_URL + ":" + TRACKER_HTTP + " ... ");
				
				// TODO - Add new torrents to map
				//HTTPTorrentClient.downloadTorrents(TRACKER_URL, TRACKER_HTTP);

				try {
					Thread.sleep(TorrentCollectorThread.TORRENT_COLLECTION_FREQUENCY * 1000);
					
				} catch (InterruptedException ie) {
					// Ignore
					
				}
				
			}
			
		}
		
	}

}
