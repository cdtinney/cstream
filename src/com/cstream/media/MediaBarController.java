package com.cstream.media;

import java.util.logging.Logger;

import com.cstream.common.Controller;
import com.cstream.utils.logging.CLogHandler;

import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;

public class MediaBarController extends Controller{

	private static Logger LOGGER = Logger.getLogger(MediaBarController.class.getName());
	
	private MediaBarView view; 
	
	private MediaPlayer mp;
	
	//Networking Object
		
	public void initialize(MediaPlayer mp) {
		this.mp = mp;
		
		view = new MediaBarView();
		view.initialize();
		
		CLogHandler.setView(view);
		
		addHandlers();
		addListeners();
		
	}
	
    
    private void addListeners() {
    	
    	// TODO: Volume listener, time listener
    	
    	//PropertyChangeDispatcher.getInstance().addListener(PhaseManager.class, "currentPhase", this, "onPhaseChanged");
    	
    	// TODO: Add Media Control listeners
    	
    }
	
	private void addHandlers() {
		
		addEventHandler(view, "playButton", "setOnAction", "handlePlayButton");
		
	}
	
	public Node getView() {
		return view;
	}
	
}
