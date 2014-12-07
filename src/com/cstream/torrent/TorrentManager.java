package com.cstream.torrent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import com.cstream.model.Song;
import com.cstream.util.OSUtils;
import com.turn.ttorrent.common.Torrent;

public class TorrentManager {
	
	private static Logger LOGGER = Logger.getLogger(TorrentManager.class.getName());
	
	public final static String FILE_DIR = System.getProperty("user.home") + (OSUtils.isWindows() ? "\\cstream\\" : "/cstream/");
	public final static String TORRENT_DIR = FILE_DIR + (OSUtils.isWindows() ? "torrent\\" : "torrent/");
	
	public final static String TRACKER_ANNOUNCE = "http://192.168.1.109:6969/announce";
	
	public static void buildTorrentsFromLibrary(Map<String, Song> library, String createdBy) {
		
		if (library == null) {
			return;
		}
		
		for (Song s : library.values()) {
			
			String fileName = s.getPath();
			String path = FILE_DIR + fileName;
			File file = new File(path);
			
			buildTorrentFromFile(file, createdBy);
			
		}
		
	}
	
	public static void buildTorrentFromFile(File file, String createdBy) {
		
		try {
			
			// Strip the extension
			String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
			
			// Ignore .torrent files that already exist
			File existingFile = new File(TORRENT_DIR + fileName + ".torrent");
			if (existingFile.exists()) {
				LOGGER.info("Ignoring existing .torrent file: " + fileName);		
				return;
			}
			
			Torrent t = Torrent.create(file, new URI(TRACKER_ANNOUNCE), createdBy);
			
			LOGGER.info("Creating .torrent file: " + fileName);			
			OutputStream output = new FileOutputStream(createTorrentFile(fileName));
			t.save(output);
			
		} catch (InterruptedException | IOException | URISyntaxException e) {
			e.printStackTrace();
			
		}
		
	}
	
	public static File createTorrentFile(String filename) {
		return new File(TorrentManager.TORRENT_DIR + filename + ".torrent");	
	}

}
