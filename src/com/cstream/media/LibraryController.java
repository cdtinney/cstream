package com.cstream.media;

import java.util.Collection;
import java.util.logging.Logger;

import com.cstream.controller.Controller;
import com.cstream.model.Song;

public class LibraryController extends Controller {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(LibraryController.class.getName());

	// View
	private LibraryView view;
	
	public void initialize() {
		view = new LibraryView();
		view.initialize();
	}
	
	public void addData(Collection<Song> songs) {
		view.addData(songs);
	}
	
	public Song getSelectedSong() {
		return view.getTable().getSelectionModel().getSelectedItem();
	}
	
	public LibraryView getView() {
		return view;
	}
	
}
