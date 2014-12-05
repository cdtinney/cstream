package com.cstream.media;

import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;

import com.cstream.controller.Controller;
import com.cstream.model.Song;
import com.cstream.notifier.Notifier;
import com.cstream.tracker.TrackerClient;

public class MediaController extends Controller {

	private static Logger LOGGER = Logger.getLogger(MediaController.class.getName());

	// View
	private MediaView view; 

	//Network
	private TrackerClient client;

	// Model
	private Song playingSong;
	private MediaPlayer mp;
	private LibraryController subController;

	public void initialize(LibraryController subController, TrackerClient client) {

		view = new MediaView();
		view.initialize();

		this.subController = subController;
		this.client = client;

		addHandlers();
		addListeners();

	}

	public Node getView() {
		return view;
	}

	@SuppressWarnings("unused")
	private void handlePlayButton(Event event) {
		LOGGER.info("Play button pressed");

		Song selected = subController.getSongSelected();
		this.playingSong = subController.getSongToPlay();

		/*
		// TODO: selected song is not playing .. play it
		if(selected.compareTo(playingSong) != 0) {

			if(selected.isLocal(client.getPeer().getId())) {
				// Play locally

			} else {
				// Stream from peers

			}

		}*/
	}

	@SuppressWarnings("unused")
	private void handlePauseButton(Event event) {
		LOGGER.info("Pause button pressed");

		// Need a way to check if playing song is playing
		if(this.playingSong != null) {

			if(playingSong.isLocal(client.getPeer().getId())) {
				// Pause locally

			} else {
				// Pause stream

			}

		}
	}

	@SuppressWarnings("unused")
	private void handleStopbutton(Event event) {
		LOGGER.info("Stop button pressed");

		if(this.playingSong != null) {

			if(playingSong.isLocal(client.getPeer().getId())) {
				// Stop locally

			} else {
				// Stop stream

			}

		}
	}

	private void addListeners() {

		Notifier.getInstance().addListener(LibraryController.class, "songToPlay", this, "handlePlayButton", true);

	}

	private void addHandlers() {

		addEventHandler(view, "playButton", "setOnAction", "handlePlayButton");
		addEventHandler(view, "pauseButton", "setOnAction", "handlePauseButton");
		addEventHandler(view, "stopButton", "setOnAction", "handleStopbutton");

	}

}
