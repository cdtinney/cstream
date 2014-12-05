package com.cstream.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.cstream.model.Song;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.Mp3File;

public class LibraryUtils {
	
	private static Logger LOGGER = Logger.getLogger(LibraryUtils.class.getName());
	
	// For now, only support MP3
	private final static String[] extensions = {"mp3"};

	/**
	 * Builds a map of songs, where the key is the song ID, from a specific directory and
	 * all its sub directories.
	 */
	public static Map<String, Song> buildLocalLibrary(String directoryName, String peerId) {
		
		if (directoryName == null || directoryName.isEmpty()) {
			LOGGER.warning("Cannot build library from null or empty directory path");
			return null;
		}
		
		LOGGER.info("Building local library from: " + directoryName);
		
		Map<String, Song> results = new HashMap<String, Song>();
		List<File> fileList = FileUtils.listFiles(directoryName, extensions);
		
		int count = 0;
		for (int i = 0; i < fileList.size(); i++) {
			
			Song song = new Song(fileList.get(i).getAbsolutePath());
			song.getPeers().add(peerId);
			song.setLocal(true);
			
			if (song.getMp3() != null && song.getId() != null) {
				results.put(song.getId(), song);
				count++;
				
			} else {
				LOGGER.warning("Mp3 Object or Song Id were not created correctly");
				
			}
			
		}
		
		LOGGER.info("Added " + count + " songs");
		
		return results;
		
	}
	
	public static ID3v1 getTagFromMp3(Mp3File mp3) {
		
		if (mp3 == null) {
			return null;
		}
		
		ID3v1 tag = new ID3v1Tag();
		if (mp3.hasId3v1Tag()) {
			tag = mp3.getId3v1Tag();
			 
		} else if (mp3.hasId3v2Tag()) {
			tag = new ID3v1Tag();
			tag.setTrack(mp3.getId3v2Tag().getTrack());
			tag.setTitle(mp3.getId3v2Tag().getTitle());
			tag.setArtist(mp3.getId3v2Tag().getArtist());
			tag.setAlbum(mp3.getId3v2Tag().getAlbum());
			tag.setYear(mp3.getId3v2Tag().getYear());
			tag.setTrack(mp3.getId3v2Tag().getTrack());
			 
		} else if (mp3.hasCustomTag()) {
			// Ignore custom tags for now
			
		}
		
		// If none of {album, title, artist} are set, default the title to the filename
		if ((tag.getArtist() == null || tag.getArtist().isEmpty()) && (tag.getAlbum() == null || tag.getAlbum().isEmpty()) &&
				(tag.getTitle() == null || tag.getTitle().isEmpty())) { 
			
			String fileName = mp3.getFilename();
			
			// Strip directories from the file path
			int index = fileName.lastIndexOf(File.separatorChar);
			fileName = fileName.substring(index+1);
			
			// Strip the extension
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));

			tag.setTitle(fileName);
			
		}
		
		return tag;
		
	}
	
}
