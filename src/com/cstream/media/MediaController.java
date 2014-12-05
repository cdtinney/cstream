package com.cstream.media;

import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.media.MediaPlayer;

import com.cstream.controller.Controller;
import com.cstream.model.Song;
import com.cstream.tracker.TrackerClient;

public class MediaController extends Controller {

	private static Logger LOGGER = Logger.getLogger(MediaController.class.getName());

	// View
	private MediaView view; 

	// Model
	private TrackerClient client;
	private Song activeSong;
	
	// Sub-controllers
	private LibraryController libraryController;
	
	private MediaPlayer mp;

	public void initialize(LibraryController libraryController, TrackerClient client) {

		view = new MediaView();
		view.initialize();

		this.libraryController = libraryController;
		this.client = client;

		addHandlers();

	}

	public Node getView() {
		return view;
	}

	private void playSong(Song song) {
		
		if (song == null) {
			return;
		}
		
		if (activeSong != null && song.getId().equals(activeSong.getId())) {
			LOGGER.info("Song is already playing: " + song);
			return;
		}

		activeSong = song;

		boolean isLocal = activeSong.sharedByPeer(client.getPeer().getId());
		if (isLocal) {
			LOGGER.info("Play song locally: " + song);	

		} else {
			LOGGER.info("Play song stream: " + song);
			
		}
		
	}
	
	private void pauseSong() {
		
		if (activeSong == null) {
			LOGGER.info("No active song to pause");
			return;
		}

		boolean isLocal = activeSong.sharedByPeer(client.getPeer().getId());
		if (isLocal) {
			LOGGER.info("Pause song locally " + activeSong);
			
		} else {
			LOGGER.info("Pause song stream " + activeSong);
			
		}
		
	}
	
	private void stopSong() {
		
		if (activeSong == null) {
			LOGGER.info("No active song to stop");
			return;
		}
		
		boolean isLocal = activeSong.sharedByPeer(client.getPeer().getId());
		if (isLocal) {
			LOGGER.info("Stop song locally " + activeSong);
			
		} else {
			LOGGER.info("Stop song stream " + activeSong);
			
		}
		
		activeSong = null;
		
	}

	private void addHandlers() {

		addEventHandler(view, "playButton", "setOnAction", "handlePlayButton");
		addEventHandler(view, "pauseButton", "setOnAction", "handlePauseButton");
		addEventHandler(view, "stopButton", "setOnAction", "handleStopbutton");
		
		libraryController.getView().getTable().setRowFactory(tv -> {
			
		    TableRow<Song> row = new TableRow<>();
		    
		    row.setOnMouseClicked(event -> {
		    	
		        if (event.getClickCount() == 2 && (! row.isEmpty())) {
		            Song song = row.getItem();
		            playSong(song);
		        }
		        
		    });
		    
		    return row ;
		    
		});
		
	}

	@SuppressWarnings("unused")
	private void handlePlayButton(Event event) {
		playSong(libraryController.getSelectedSong());
	}

	@SuppressWarnings("unused")
	private void handlePauseButton(Event event) {
		pauseSong();
	}

	@SuppressWarnings("unused")
	private void handleStopbutton(Event event) {
		stopSong();		
	}

}
