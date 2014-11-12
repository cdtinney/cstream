package com.cstream.Media;

import javafx.scene.Node;

public class MediaInfoController {

	private MediaInfoView view;
	
	public void initialize() {
		
		view = new MediaInfoView();
		view.initialize();
		
		addHandlers();
		addListeners();
		
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
