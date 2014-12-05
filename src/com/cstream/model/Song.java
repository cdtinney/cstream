package com.cstream.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;

import org.apache.commons.codec.digest.DigestUtils;

import com.cstream.util.LibraryUtils;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Song {
	
	private transient static Logger LOGGER = Logger.getLogger(Song.class.getName());
	
	// Unique identifier for the song (MD5 hash)
	private String id;
	
	// The file itself, and an absolute path to the file
	private transient Mp3File mp3;	
	private String path;
	
	// Observable properties to bind to library view
	private transient SimpleStringProperty artistProperty = new SimpleStringProperty("none");
	private transient SimpleStringProperty titleProperty = new SimpleStringProperty("none");
	private transient SimpleStringProperty albumProperty = new SimpleStringProperty("none");
	
	// Primitive string properties required for JSON parsing
	private String artist;
	private String album;
	private String title;
	private String track;
	
	// Default constructor
	public Song(String path) {
		
		try {
			mp3 = new Mp3File(path);
			
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();
			return;
			
		}
		
		generateId(path);
		
		this.path = path;
		
		initializeProperties();
		
	}

	public String getPath() {		
		return path;		
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
	
	public SimpleStringProperty artistProperty() {
		return artistProperty;
	}
	
	public SimpleStringProperty titleProperty() {
		return titleProperty;
	}
	
	public SimpleStringProperty albumProperty() {
		return albumProperty;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("[Song] ");
		sb.append("Artist/" + (artist == null ? "?" : artist) + " - ");
		sb.append("Title/" + (title == null ? "?" : title) + " - ");
		sb.append("Album/" + (album == null ? "?" : album) + " - ");
		sb.append("Track/" + (track == null ? "?" : track) + " - ");
		sb.append("Path=" + (path == null ? "?" : path));
		
		return sb.toString();
		
	}
	
	private void initializeProperties() {
		
		if (mp3 == null) {
			return;
		}
		
		ID3v1 tag = LibraryUtils.getTagFromMp3(mp3);
		
		// TODO - Add more properties - year, track... ?
		this.artistProperty = new SimpleStringProperty(tag.getArtist());
		this.titleProperty = new SimpleStringProperty(tag.getTitle());
		this.albumProperty = new SimpleStringProperty(tag.getAlbum());
		
		this.artist = artistProperty.get();
		this.title = titleProperty.get();
		this.album = albumProperty.get();
		
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
