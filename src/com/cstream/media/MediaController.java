package com.cstream.media;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TableRow;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.cstream.controller.Controller;
import com.cstream.model.Song;
import com.cstream.playback.LocalAudioPlayback;
import com.cstream.tracker.TrackerClient;

public class MediaController extends Controller {

	private static Logger LOGGER = Logger.getLogger(MediaController.class.getName());
	
	// View
	private MediaView view; 
	
	// Sub-controllers
	private LibraryController libraryController;

	// Model
	private TrackerClient client;
	
	private LocalAudioPlayback audioPlayback;
	private Song activeSong;
	private Song queuedSong;

	public void initialize(LibraryController libraryController, TrackerClient client) {

		view = new MediaView();
		view.initialize();

		this.libraryController = libraryController;
		this.client = client;
		this.audioPlayback = new LocalAudioPlayback();

		addHandlers();

	}

	public Node getView() {
		return view;
	}

	private void play(Song song) {
		
		if (song == null) {
			return;
		}
		
		if (activeSong != null) {

			if (song.getId().equals(activeSong.getId())) {
				togglePause();
				
			} else {
				queuedSong = song;
				stop();
				
			}
			
			return;
			
		}

		activeSong = song;
		boolean isLocal = song.sharedByPeer(client.getPeer().getId());
		if (isLocal) {
			LOGGER.info("Play song locally: " + song);	
			audioPlayback.playFile(activeSong.getPath(), new SongListener());
			
		} else {
			LOGGER.info("Play song stream: " + song);
		}
		
	}
	
	private void togglePause() {
		
		if (activeSong == null) {
			LOGGER.info("No active song to pause");
			return;
		}

		boolean isLocal = activeSong.sharedByPeer(client.getPeer().getId());
		if (isLocal) {
			audioPlayback.togglePause();
			view.togglePlayButton(audioPlayback.isPaused());
			
		}
		
	}
	
	private void stop() {
		
		if (activeSong == null) {
			LOGGER.info("No active song to stop");
			return;
		}
		
		boolean isLocal = activeSong.sharedByPeer(client.getPeer().getId());
		if (isLocal) {
			audioPlayback.stop();
			
		}
		
		activeSong = null;
		
	}

	@SuppressWarnings("unused")
	private void handlePlayButton(Event event) {
		
		if (activeSong == null) {
			play(libraryController.getSelectedSong());
			return;
		}
		
		play(activeSong);
		
	}

	@SuppressWarnings("unused")
	private void handleStopbutton(Event event) {
		stop();		
	}

	private void addHandlers() {

		addEventHandler(view, "playButton", "setOnAction", "handlePlayButton");
		addEventHandler(view, "stopButton", "setOnAction", "handleStopbutton");
		
		libraryController.getView().getTable().setRowFactory(tv -> {
			
		    TableRow<Song> row = new TableRow<>();
		    
		    row.setOnMouseClicked(event -> {
		    	
		        if (event.getClickCount() == 2 && (! row.isEmpty())) {
		            Song song = row.getItem();
		            play(song);
		        }
		        
		    });
		    
		    return row ;
		    
		});
		
	}
	
	private class SongListener implements LineListener {
		
		private Timer timer;

		@Override
		public void update(LineEvent event) {

	        LineEvent.Type type = event.getType();
	        
	        if (type == LineEvent.Type.START) {
	        	handleStartEvent();
	             
	        } else if (type == LineEvent.Type.STOP) {
	            handleStopEvent();
	        	
	        }
			
		}
		
		private void handleStartEvent() {
        	
            LOGGER.info("Playback started: " + activeSong);
            
            timer = new Timer();
            SongTimer songTimer = new SongTimer();
            timer.schedule(songTimer, 0, (long) 500);         
            
            Platform.runLater(() -> {
            	
    			view.togglePlayButton(audioPlayback.isPaused() || audioPlayback.isClosed());
                view.getButton("stopButton").setDisable(false);
                view.updateNowPlaying(activeSong);
            	
            });
			
		}
		
		private void handleStopEvent() {
        	
            LOGGER.info("Playback stopped: " + activeSong);
            
            timer.cancel();    
            
            Platform.runLater(() -> {

    			view.togglePlayButton(audioPlayback.isPaused() || audioPlayback.isClosed());
                view.getButton("stopButton").setDisable(true);
                
                view.updateNowPlaying(activeSong = null);
                view.updateTimes(0, 0, 0);
                
                if (queuedSong != null) {
                	play(queuedSong);
                	queuedSong = null;
                }
            	
            });
			
		}
		
	}
	
	private class SongTimer extends TimerTask {
		
		private volatile int time = 0;

		@Override
		public void run() {
			
			Platform.runLater(() -> {
				
				if (audioPlayback == null || activeSong == null) {
					return;
				}
				
				int current = audioPlayback.getPosition();
				if (current != time) {
					
					int remaining = activeSong.getLength() - current;
					view.updateTimes(time = current, remaining, activeSong.getLength());
				}
				
			});
			
		}
	    
	}

}
