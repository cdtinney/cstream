package com.cstream.media;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.TableRow;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.cstream.client.TorrentActivityListener;
import com.cstream.client.TorrentClientManager;
import com.cstream.client.TorrentManager;
import com.cstream.controller.Controller;
import com.cstream.song.LocalAudioPlayback;
import com.cstream.song.Song;
import com.turn.ttorrent.client.SharedTorrent;

public class MediaController extends Controller implements TorrentActivityListener {

	private static Logger LOGGER = Logger.getLogger(MediaController.class.getName());
	
	// Views
	private MediaView mediaView; 
	private LibraryView libraryView;
	private TorrentActionView actionView;
	
	private LocalAudioPlayback audioPlayback;
	private Song activeSong;
	private Song queuedSong;

	public void initialize() {
		
		audioPlayback = new LocalAudioPlayback();
		
		actionView = new TorrentActionView();
		actionView.initialize();

		mediaView = new MediaView();
		mediaView.initialize();
		
		libraryView = new LibraryView();
		libraryView.initialize();

		addHandlers();

	}

	public TorrentActionView getActionView() {
		return actionView;
	}
	
	public LibraryView getLibraryView() {
		return libraryView;
	}

	public MediaView getMediaView() {
		return mediaView;
	}
	
	private void playTorrent(SharedTorrent torrent) {
		
		LOGGER.info("Playing: " + torrent.getName());
		
		Song song = TorrentManager.getInstance().getSongs().get(torrent.getName());
		if (song == null) {
			LOGGER.warning("Client is not storing song: " + torrent.getName());
			return;
		}
		
		play(song);		
		
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
		LOGGER.info("Play song locally: " + song);	
		audioPlayback.playFile(activeSong.getPath(), new SongListener());
		
	}
	
	private void togglePause() {
		
		if (activeSong == null) {
			LOGGER.info("No active song to pause");
			return;
		}
		
		audioPlayback.togglePause();
		mediaView.togglePlayButton(audioPlayback.isPaused());
		
	}
	
	private void stop() {
		
		if (activeSong == null) {
			LOGGER.info("No active song to stop");
			return;
		}

		audioPlayback.stop();
		activeSong = null;
		
	}

	@SuppressWarnings("unused")
	private void handlePlayButton(Event event) {
		
		if (activeSong == null) {
			SharedTorrent selected = libraryView.getSelected();
			playTorrent(selected);
			return;
		}
		
		play(activeSong);
		
	}

	@SuppressWarnings("unused")
	private void handleStopbutton(Event event) {
		stop();		
	}

	@SuppressWarnings("unused")
	private void handleStartTorrentButton(Event event) {
		
		SharedTorrent selected = libraryView.getSelected();	
		if (selected != null) {
			TorrentClientManager.getInstance().share(selected);	
		}
		
	}

	@SuppressWarnings("unused")
	private void handleStopTorrentButton(Event event) {
		
		SharedTorrent selected = libraryView.getSelected();
		if (selected != null) {
			TorrentClientManager.getInstance().stopShare(selected);	
		}
		
	}

	private void addHandlers() {

		addEventHandler(mediaView, "playButton", "setOnAction", "handlePlayButton");
		addEventHandler(mediaView, "stopButton", "setOnAction", "handleStopbutton");
		
		libraryView.getTable().setRowFactory(tv -> {
			
		    TableRow<SharedTorrent> row = new TableRow<>();
		    
		    row.setOnMouseClicked(event -> {
		    	
		        if (event.getClickCount() == 2 && (! row.isEmpty())) {
		            SharedTorrent torrent = row.getItem();
		            playTorrent(torrent);
		        }
		        
		    });
		    
		    return row ;
		    
		});

		addEventHandler(actionView, "startButton", "setOnAction", "handleStartTorrentButton");
		addEventHandler(actionView, "stopButton", "setOnAction", "handleStopTorrentButton");
		
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
            	
    			mediaView.togglePlayButton(audioPlayback.isPaused() || audioPlayback.isClosed());
                mediaView.getButton("stopButton").setDisable(false);
                mediaView.updateNowPlaying(activeSong);
            	
            });
			
		}
		
		private void handleStopEvent() {
        	
            LOGGER.info("Playback stopped: " + activeSong);
            
            timer.cancel();    
            
            Platform.runLater(() -> {

    			mediaView.togglePlayButton(audioPlayback.isPaused() || audioPlayback.isClosed());
                mediaView.getButton("stopButton").setDisable(true);
                
                mediaView.updateNowPlaying(activeSong = null);
                mediaView.updateTimes(0, 0, 0);
                
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
					mediaView.updateTimes(time = current, remaining, activeSong.getLength());
				}
				
			});
			
		}
	    
	}

	@Override
	public void handleTorrentAdded(SharedTorrent torrent) {
		
		Platform.runLater(() -> {
			libraryView.addItem(torrent);
		});
		
	}

	@Override
	public void handleTorrentChanged(SharedTorrent torrent) {
		
		Platform.runLater(() -> {
			libraryView.updateItem(torrent);
		});
		
	}

}
