package com.cstream.media;

import java.util.Collection;

import javafx.event.Event;
import javafx.scene.Node;

import com.cstream.controller.Controller;
import com.cstream.model.Song;

public class LibraryController extends Controller {

	private LibraryView view;
	
	public void initialize() {
		
		view = new LibraryView();
		view.initialize();
		
		addHandlers();
		addListeners();
		
	}
	
	public void addData(Collection<Song> songs) {
		view.addData(songs);
	}
	
	protected void handleArtistItemAction(Event e) {
		// TODO
	}
	
	protected void handleSongItemAction(Event e) {
		// TODO
	}
	
	private void addHandlers() {
		
		// TODO
		//addEventHandler(root, "quitAppMenuItem", "setOnAction", "handleQuitAppMenuItemAction");
		//addEventHandler(root, "aboutMenuItem", "setOnAction", "handleAboutMenuItemAction");

	}
	
	private void addListeners() {
		
	}
	
	public Node getView() {
		return view;
	}
	
}
