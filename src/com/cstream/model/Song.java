package com.cstream.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import com.cstream.util.FileUtils;
import com.cstream.util.LibraryUtils;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Song implements Comparable<Song> {
	
	@SuppressWarnings("unused")
	private transient static Logger LOGGER = Logger.getLogger(Song.class.getName());
	
	// Unique identifier for the song (MD5 hash)
	private String id;
	
	// The file itself, and an absolute path to the file
	private transient Mp3File mp3;	
	private String path;
	
	// Set of peer IDs that are sharing this song
	private Set<String> peers;
	
	// Primitive string properties required for JSON parsing
	private String artist;
	private String album;
	private String title;
	private String track;
	private int length = 0;
	
	// Indicates whether this song is stored locally or not
	private transient boolean local = false;
	
	// Constructor
	public Song(String fileName, String path) {
		
		try {
			mp3 = new Mp3File(path);
			
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();
			return;
			
		}
		
		this.id = FileUtils.generateMd5(path);	
		this.path = fileName;
		this.peers = new HashSet<String>();
		
		if (mp3 != null) {
			
			ID3v1 tag = LibraryUtils.getTagFromMp3(mp3);
			
			this.artist = tag.getArtist();
			this.title = tag.getTitle();
			this.album = tag.getAlbum();
			this.length = (int) mp3.getLengthInSeconds();
			
		}
		
	}

	public String getPath() {		
		return path;		
	}
	
	public String getId() {		
		return id;		
	}
	
	public String getArtist() {
		return artist;
	}
	
	public String getAlbum() {
		return album;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getLength() {
		return length;
	}
	
	public Mp3File getMp3() {
		return this.mp3;
	}
	
	public boolean isLocal() {
		return local;
	}
	
	public void setLocal(boolean local) {
		this.local = local;
	}
	
	public void setPeers(Set<String> peers) {
		this.peers = peers;
	}
	
	public Set<String> getPeers() {
		return peers;
	}
	
	public boolean sharedByPeer(String id) {
		return peers.contains(id);
	}
	
	public SimpleStringProperty artistProperty() {
		return new SimpleStringProperty(artist);
	}
	
	public SimpleStringProperty titleProperty() {
		return new SimpleStringProperty(title);
	}
	
	public SimpleStringProperty albumProperty() {
		return new SimpleStringProperty(album);
	}
	
	public SimpleIntegerProperty peersProperty() {
		return new SimpleIntegerProperty(peers == null ? 0 : peers.size());
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

	@Override
	public int compareTo(Song s) {
		
		int i = artist == null ? -1 : artist.compareToIgnoreCase(s.getArtist() == null ? "" : s.getArtist());
		if (i != 0) {
			return i;
		}

		i = title == null ? -1 : title.compareToIgnoreCase(s.getTitle() == null ? "" : s.getTitle());
		if (i != 0) {
			return i;
		}

		i = album == null ? -1 : album.compareToIgnoreCase(s.getAlbum() == null ? "" : s.getAlbum());
		if (i != 0) {
			return i;
		}
		
		return id.compareTo(s.getId());
		
	}
	
}
