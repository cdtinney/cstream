package com.cstream.media;

import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;

import com.cstream.controller.Controller;

public class MediaBarController extends Controller{

	private static Logger LOGGER = Logger.getLogger(MediaBarController.class.getName());
	
	// View
	private MediaBarView view; 
	
	// Model
	private MediaPlayer mp;
	
	public void initialize(MediaPlayer mp) {
		this.mp = mp;
		
		view = new MediaBarView();
		view.initialize();
		
		addHandlers();
		addListeners();
		
	}
	
	public Node getView() {
		return view;
	}
	
	@SuppressWarnings("unused")
	private void handlePlayButton(Event event) {
		LOGGER.info("Play button pressed");
		// TODO
	}
	
	@SuppressWarnings("unused")
	private void handlePauseButton(Event event) {
		LOGGER.info("Pause button pressed");
		// TODO
	}
	
	@SuppressWarnings("unused")
	private void handleStopbutton(Event event) {
		LOGGER.info("Stop button pressed");
		// TODO
	}

    private void addListeners() {
    	
    	// TODO - Listen to model changes
    	//PropertyChangeDispatcher.getInstance().addListener(PhaseManager.class, "currentPhase", this, "onPhaseChanged");
    	
    }
	
	private void addHandlers() {
		
		addEventHandler(view, "playButton", "setOnAction", "handlePlayButton");
		addEventHandler(view, "pauseButton", "setOnAction", "handlePauseButton");
		addEventHandler(view, "stopButton", "setOnAction", "handleStopbutton");
		// Event handler arguments =  parentNode, childId, action, functionHandler
		
	}
	
}
