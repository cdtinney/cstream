package com.cstream.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.cstream.media.MediaBarController;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Song {
	
	private static Logger LOGGER = Logger.getLogger(MediaBarController.class.getName());
	
	// Unique identifier for the song (MD5 hash)
	private String id;
	
	private Mp3File mp3;
	private ID3v1 v1tag;
	private ID3v2 v2tag;
	private String path;
	
	public Song(String filepath) {
		
		generateId(filepath);
		
		try {
			mp3 = new Mp3File(filepath);
			
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();
			return;
			
		}
		
		setupTagVersion();
		saveTagChanges();
		
	}

	public String getPath() {		
		return path;		
	}

	public void setPath(String path) {		
		this.path = path;		
	}
	
	public String getId() {		
		return id;		
	}
	
	public long getLengthInSeconds() {		
		return mp3.getLengthInSeconds();		
	}
	
	public int getSampleRate() {		
		return mp3.getSampleRate();		
	}
	
	public long getLastModified() {		
		return mp3.getLastModified();		
	}

	public String getTrack() {		
		return mp3.hasId3v2Tag() ? v2tag.getTrack() : v1tag.getTrack();		
	}
	
	public String getTitle() {		
		return mp3.hasId3v2Tag() ? v2tag.getTitle() : v1tag.getTitle();		
	}
	
	public String getArtist() {		
		return mp3.hasId3v2Tag() ? v2tag.getArtist() : v1tag.getArtist();		
	}
	
	public String getAlbum() {		
		return mp3.hasId3v2Tag() ? v2tag.getAlbum() : v1tag.getAlbum();		
	}
	
	public String getYear() {		
		return mp3.hasId3v2Tag() ? v2tag.getYear() : v1tag.getYear();		
	}
	
	public int getGenre() {		
		return mp3.hasId3v2Tag() ? v2tag.getGenre() : v1tag.getGenre();		
	}
	
	public String getGenreDes() {		
		return mp3.hasId3v2Tag() ? v2tag.getGenreDescription() : v1tag.getGenreDescription();		
	}
	
	public String getComment() {		
		return mp3.hasId3v2Tag() ? v2tag.getComment() : v1tag.getComment();		
	}
	
	public String getComposer() {		
		return mp3.hasId3v2Tag() ? v2tag.getComposer() : "";		
	}
	
	public String getPublisher() {		
		return mp3.hasId3v2Tag() ? v2tag.getPublisher() : "";		
	}
	
	public String getOriginalArtist() {		
		return mp3.hasId3v2Tag() ? v2tag.getOriginalArtist() : "";		
	}
	
	public String getAlbumArtist() {		
		return mp3.hasId3v2Tag() ? v2tag.getAlbumArtist() : "";		
	}
	
	public String getCopyRight() {		
		return mp3.hasId3v2Tag() ? v2tag.getCopyright() : "";		
	}
	
	public String getURL() {		
		return mp3.hasId3v2Tag() ? v2tag.getUrl() : "";		
	}
	
	public String getEncoder() {		
		return mp3.hasId3v2Tag() ? v2tag.getEncoder() : "";		
	}
	
	public Mp3File getMp3() {
		return this.mp3;
	}
	
	private void setupTagVersion() {
		
		if (mp3.hasId3v1Tag()) {
			 v1tag = mp3.getId3v1Tag();
			 
		} else if (!mp3.hasId3v2Tag()) {
			  
			  // mp3 does not have v1 or v2 .. create v1
			  v1tag = new ID3v1Tag();
			  mp3.setId3v1Tag(v1tag);
			  v1tag.setTrack("");
			  v1tag.setArtist("");
			  v1tag.setTitle("");
			  v1tag.setAlbum("");
			  v1tag.setYear("");
			  v1tag.setGenre(1);
			  v1tag.setComment("");
			  
		} else {
			v2tag = mp3.getId3v2Tag();
			
		}
		
	}
	
	private void saveTagChanges() {
		
		try {

			if (mp3 != null && path != null) {
				mp3.save(path);
			}
			
		} catch (NotSupportedException|IOException e) {
			e.printStackTrace();
			
		}
		
	}
	
	private void generateId(String path) {
		
		if (id != null) {
			LOGGER.warning("Re-generating ID for file: " + id);			
		}

		try (FileInputStream fis = new FileInputStream(new File(path)) ){
			id = DigestUtils.md5Hex(fis);
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
	}
}
