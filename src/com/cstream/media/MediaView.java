package com.cstream.media;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.cstream.model.Song;
import com.cstream.util.FxUtils;

public class MediaView extends HBox {
	
	private Label title;
	private Label artist;
	private Label album;
	private Label remainingTimeLabel;
	
    public void initialize() {    	
    	
    	// Configure the parent HBox
    	setSpacing(5);
    	setPrefHeight(125);
    	setAlignment(Pos.CENTER);
    	setPadding(new Insets(5, 5, 5, 5));
    	getStyleClass().add("mediabar");
    	
    	// Add the UI elements
    	addControlButtons();
    	addSeekBar();
    	addNowPlayingInfo();
    	
    }
    
    public void updateNowPlaying(Song song) {
    	
    	if (song == null) {
    		title.setText(null);
    		album.setText(null);
    		artist.setText(null);
    		return;
    	}
    	
    	title.setText(song.getTitle());
    	album.setText(song.getAlbum());
    	artist.setText(song.getArtist());
    	
    	remainingTimeLabel.setText(String.format("%2d:%02d", (song.getLength()%3600)/60, (song.getLength()%60)));
    	
    }
    
    public Button getButton(String id) {
    	return FxUtils.lookup(this, id);
    }
    
    private void addControlButtons() {
    	
    	Button playButton = FxUtils.buildButton("Play", "playButton", 60, 35, false);
    	Button stopButton = FxUtils.buildButton("Stop", "stopButton", 60, 35, true);
    	
    	getChildren().addAll(playButton, stopButton);
    	
    }
    
    private void addSeekBar() {
    	
    	HBox seekBox = new HBox();
    	seekBox.setSpacing(10);
    	seekBox.setAlignment(Pos.CENTER);
    	
    	// Insets: top, right, bottom, left
    	seekBox.setPadding(new Insets(15, 25, 15, 25));
    	
    	Label currentTimeLabel = new Label("0:00");
    	
    	Slider timeSlider = new Slider();
    	timeSlider.setId("timeSlider");
    	timeSlider.setMinWidth(50);
    	timeSlider.setPrefWidth(400);
    	timeSlider.setMaxWidth(Double.MAX_VALUE);
    	timeSlider.setDisable(true);
    	
    	remainingTimeLabel = new Label("5:00");
    	
    	seekBox.getChildren().addAll(currentTimeLabel, timeSlider, remainingTimeLabel);
    	getChildren().add(seekBox);
    	
    }
    
    private void addNowPlayingInfo() {

    	VBox nowPlayingVBox = new VBox();
    	nowPlayingVBox.getStyleClass().add("nowPlaying");
    	nowPlayingVBox.setPrefWidth(400);
    	nowPlayingVBox.setSpacing(5);
    	nowPlayingVBox.setAlignment(Pos.CENTER);
    	nowPlayingVBox.setPadding(new Insets(10, 0, 10, 10));
		
    	title = FxUtils.buildLabel("title", "", 390, 50, 20, Pos.CENTER);
    	artist = FxUtils.buildLabel("artistLabel", "", 390, 50, 16, Pos.CENTER);
    	album = FxUtils.buildLabel("albumLabel", "", 390, 50, 16, Pos.CENTER);
		
    	nowPlayingVBox.getChildren().addAll(title, artist, album);
    	
    	getChildren().add(nowPlayingVBox);
    	
	}
	
}
