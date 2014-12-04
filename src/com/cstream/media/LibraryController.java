package com.cstream.media;

import javafx.event.Event;
import javafx.scene.Node;
import com.cstream.controller.Controller;

public class LibraryController extends Controller {

	private LibraryView view;
	
	public void initialize() {
		
		view = new LibraryView();
		view.initialize();
		
		addHandlers();
		addListeners();
		
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
