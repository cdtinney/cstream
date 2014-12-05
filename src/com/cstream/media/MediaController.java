package com.cstream.media;

import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;

import com.cstream.controller.Controller;

public class MediaController extends Controller{

	private static Logger LOGGER = Logger.getLogger(MediaController.class.getName());
	
	// View
	private MediaView view; 
	
	// Model
	// TODO: MediaPlayer needs a source of Media to be initialized
	private MediaPlayer mp;
	
	public void initialize() {
		
		view = new MediaView();
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
		// TODO - Handle play button
	}
	
	@SuppressWarnings("unused")
	private void handlePauseButton(Event event) {
		LOGGER.info("Pause button pressed");
		// TODO - Handle pause button
	}
	
	@SuppressWarnings("unused")
	private void handleStopbutton(Event event) {
		LOGGER.info("Stop button pressed");
		// TODO - Handle stop button
	}

    private void addListeners() {
    	
    	// TODO - Listen to model changes
    	//PropertyChangeDispatcher.getInstance().addListener(PhaseManager.class, "currentPhase", this, "onPhaseChanged");
    	
    }
	
	private void addHandlers() {
		
		addEventHandler(view, "playButton", "setOnAction", "handlePlayButton");
		addEventHandler(view, "pauseButton", "setOnAction", "handlePauseButton");
		addEventHandler(view, "stopButton", "setOnAction", "handleStopbutton");
		
	}
	
}
