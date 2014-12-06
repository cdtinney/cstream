package com.cstream.media;

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

public class MediaController extends Controller implements LineListener {

	private static Logger LOGGER = Logger.getLogger(MediaController.class.getName());

	// View
	private MediaView view; 

	// Model
	private TrackerClient client;
	private Song activeSong;
	private Song queuedSong;
	
	private LocalAudioPlayback audioPlayback = new LocalAudioPlayback();
	
	// Sub-controllers
	private LibraryController libraryController;

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
		
		if (activeSong != null) {

			if (song.getId().equals(activeSong.getId())) {
				pauseSong();
				
			} else {
				queuedSong = song;
				stopSong();
				
			}
			
			return;
			
		}

		activeSong = song;
		boolean isLocal = song.sharedByPeer(client.getPeer().getId());
		if (isLocal) {
			
			LOGGER.info("Play song locally: " + song);	
			
			// Start local audio play back on a new thread, so we don't block the UI thread
			new Thread(() -> {
				
				audioPlayback.play(activeSong.getPath(), this);
				
			}).start();
			
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
			
			LOGGER.info("Toggle pause song locally " + activeSong);
			
			audioPlayback.togglePause();
			
			if (audioPlayback.isPaused()) {
	            view.getButton("playButton").setText("Play");
			} else {
	            view.getButton("playButton").setText("Pause");
				
			}
        	
            //view.getButton("playButton").setDisable(false);
            //view.getButton("stopButton").setDisable(false);
			
			
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
			
			audioPlayback.stopPlayback();
			
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
		
		if (activeSong == null) {
			playSong(libraryController.getSelectedSong());
			return;
		}
		
		playSong(activeSong);
		
	}

	@SuppressWarnings("unused")
	private void handleStopbutton(Event event) {
		stopSong();		
	}

	@Override
	public void update(LineEvent event) {
		
        LineEvent.Type type = event.getType();
        
        if (type == LineEvent.Type.START) {
        	
            LOGGER.info("Playback started");            
            
            Platform.runLater(() -> {
            	
                view.getButton("playButton").setText("Pause");
                view.getButton("stopButton").setDisable(false);
                
                view.updateNowPlaying(activeSong);
            	
            });
             
        } else if (type == LineEvent.Type.STOP) {
        	
            LOGGER.info("Playback stopped");            
            
            Platform.runLater(() -> {
            	
                view.getButton("playButton").setText("Play");
                view.getButton("stopButton").setDisable(true);
                
                view.updateNowPlaying(activeSong = null);
                
                if (queuedSong != null) {
                	playSong(queuedSong);
                	queuedSong = null;
                }
            	
            });
            
        }
		
	}

}
