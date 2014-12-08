package com.cstream.torrent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.cstream.model.Song;
import com.cstream.util.OSUtils;
import com.turn.ttorrent.common.Torrent;

public class TorrentManager {
	
	private static Logger LOGGER = Logger.getLogger(TorrentManager.class.getName());
	
	// Static constants
	public final static String FILE_DIR = System.getProperty("user.home") + (OSUtils.isWindows() ? "\\cstream\\" : "/cstream/");
	public final static String TORRENT_DIR = FILE_DIR + (OSUtils.isWindows() ? "torrent\\" : "torrent/");
	public final static String TRACKER_ANNOUNCE = "http://192.168.1.109:6969/announce";

	// Singleton instance
	private static TorrentManager instance = null;
	
	// Queue of torrents to be shared	
	public List<Torrent> queue = new ArrayList<Torrent>();
	
	private TorrentManager() { }
	
	public static TorrentManager getInstance() {
		
		if (instance == null) {
			instance = new TorrentManager();
		}
		
		return instance;
		
	}
	
	public List<Torrent> getQueue() {
		return queue;
	}
	
	public void addTorrentsFromLibrary(Map<String, Song> library, String createdBy) {
			
		for (Song s : library.values()) {
			
			String fileName = s.getPath();
			String path = FILE_DIR + fileName;
			File file = new File(path);
			
			Torrent t = buildTorrentFromFile(file, createdBy);
			if (t != null) {
				queue.add(t);
			}
			
		}
		
	}
	
	public static File createTorrentFile(String filename) {
		return new File(TorrentManager.TORRENT_DIR + filename + ".torrent");	
	}
	
	private static Torrent buildTorrentFromFile(File file, String createdBy) {
		
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

}
