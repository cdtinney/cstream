package com.cstream.utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.cstream.model.Song;

public class LibraryUtils {
	
	private static Logger LOGGER = Logger.getLogger(LibraryUtils.class.getName());
	
	// For now, only support MP3
	private final static String[] extensions = {"mp3"};

	/**
	 * Builds a map of songs, where the key is the song ID, from a specific directory and
	 * all its sub directories.
	 */
	public static Map<String, Song> buildLocalLibrary(String directoryName) {
		
		if (directoryName == null || directoryName.isEmpty()) {
			LOGGER.warning("Cannot build library from null or empty directory path");
			return null;
		}
		
		Map<String, Song> results = new HashMap<String, Song>();
		List<File> fileList = FileUtils.listFiles(directoryName, extensions);
		
		for (int i = 0; i < fileList.size(); i++) {
			
			Song song = new Song(fileList.get(i).getAbsolutePath());
			
			if (song.getMp3() != null && song.getId() != null) {
				results.put(song.getId(), song);
				
			} else {
				LOGGER.warning("Mp3 Object or Song Id were not created correctly");
				
			}
			
		}
		
		return results;
		
	}
	
}
