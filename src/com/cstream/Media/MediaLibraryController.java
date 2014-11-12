package com.cstream.Media;

import javafx.event.Event;
import javafx.scene.Node;

import com.cstream.common.Controller;

public class MediaLibraryController extends Controller {

	private MediaLibraryView view;
	
	public void initialize() {
		
		view = new MediaLibraryView();
		view.initialize();
		
		addHandlers();
		addListeners();
		
	}
	
	protected void handleArtistItemAction(Event e) {
		
	}
	
	protected void handleSongItemAction(Event e) {
		
	}
	
	private void addHandlers() {
		//addEventHandler(root, "quitAppMenuItem", "setOnAction", "handleQuitAppMenuItemAction");
		//addEventHandler(root, "aboutMenuItem", "setOnAction", "handleAboutMenuItemAction");
	}
	
	private void addListeners() {
		
	}
	
	public Node getView() {
		return view;
	}
	
}
