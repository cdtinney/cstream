package com.cstream.song;

import java.io.IOException;
import java.util.logging.Logger;

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
	
	// Primitive string properties
	private String artist;
	private String album;
	private String title;
	private String track;
	private int length = 0;
	
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
