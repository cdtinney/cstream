package com.cstream;

import java.util.logging.Logger;

import javafx.scene.Scene;

import com.cstream.client.TorrentClientManager;
import com.cstream.client.TorrentManager;
import com.cstream.controller.Controller;
import com.cstream.media.MediaController;

public class CApplicationController extends Controller {

	private static Logger LOGGER = Logger.getLogger(CApplicationController.class.getName());
	
	// Primary scene
	private CApplicationView scene; 

	// Sub-Controller
	private MediaController mediaController = new MediaController();
	
	// Manager classes
	private TorrentClientManager clientManager;
	private TorrentManager torrentManager;

	public void initialize() {

		scene = new CApplicationView(CApplication.WIDTH, CApplication.HEIGHT);
		scene.initialize();
		
		mediaController.initialize();
		
		addViews();
		
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public void start() {
		
		torrentManager = TorrentManager.getInstance();		
		torrentManager.register(mediaController);
				
		clientManager = TorrentClientManager.getInstance();
		clientManager.register(mediaController);
		
		torrentManager.start();		
		
	}

	public void stop() {

		LOGGER.info("Attempting to stop the application...");
		
		torrentManager.stop();
		clientManager.stop();
		
	}

	private void addViews() {

		scene.addToBorderPane(mediaController.getActionView(), "top");
		scene.addToBorderPane(mediaController.getLibraryView(), "center");
		scene.addToBorderPane(mediaController.getMediaView(), "bottom");

	}

}
