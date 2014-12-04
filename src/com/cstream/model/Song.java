package com.cstream.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.apache.commons.codec.digest.DigestUtils;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Song {
	
	private static Logger LOGGER = Logger.getLogger(Song.class.getName());
	
	// Unique identifier for the song (MD5 hash)
	private String id;
	
	private SimpleStringProperty artist = new SimpleStringProperty("test");
	private SimpleStringProperty title = new SimpleStringProperty("test");
	private SimpleStringProperty album = new SimpleStringProperty("test");
	private StringProperty track;
	
	private Mp3File mp3;
	private ID3v1 tag;
	
	private String path;
	
	public Song(String filepath) {
		
		generateId(filepath);
		
		try {
			mp3 = new Mp3File(filepath);
			
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();
			return;
			
		}
		
		setPath(filepath);
		
		initializeProperties();
		
	}
	
	private void initializeProperties() {
		
		if (mp3 == null) {
			return;
		}
		
		initializeTag();
		
		this.artist = new SimpleStringProperty(tag.getArtist());
		this.title = new SimpleStringProperty(tag.getTitle());
		this.album = new SimpleStringProperty(tag.getAlbum());
		
		this.track = new SimpleStringProperty(tag.getTrack());
		
	}
	
	public SimpleStringProperty artistProperty() {
		return artist;
	}
	
	public StringProperty titleProperty() {
		return title;
	}
	
	public StringProperty albumProperty() {
		return album;
	}
	
	public String getArtist() {
		return artist.get();
	}
	
	public void setArtist(String artist) {
		this.artist.set(artist);
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
	
	public Mp3File getMp3() {
		return this.mp3;
	}
	
	public long getLengthInSeconds() {		
		return mp3.getLengthInSeconds();		
	}
	
	public int getSampleRate() {		
		return mp3.getSampleRate();		
	}
	
	private void initializeTag() {
		
		if (mp3.hasId3v1Tag()) {
			tag = mp3.getId3v1Tag();
			 
		} else if (mp3.hasId3v2Tag()) {
			tag = new ID3v1Tag();
			tag.setTrack(mp3.getId3v2Tag().getTrack());
			tag.setTitle(mp3.getId3v2Tag().getTitle());
			tag.setArtist(mp3.getId3v2Tag().getArtist());
			tag.setYear(mp3.getId3v2Tag().getYear());
			tag.setTrack(mp3.getId3v2Tag().getTrack());
			 
		} else if (mp3.hasCustomTag()) {
			// TODO - Probably just ignore
			
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
