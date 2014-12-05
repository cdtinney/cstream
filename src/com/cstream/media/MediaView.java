package com.cstream.media;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.cstream.utils.FxUtils;

public class MediaView extends HBox {
	
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
    
    private void addControlButtons() {
    	
    	Button playButton = FxUtils.buildButton("Play", "playButton", 60, 35, false);
    	Button pauseButton = FxUtils.buildButton("Pause", "pauseButton", 60, 35, false);
    	Button stopButton = FxUtils.buildButton("Stop", "stopButton", 60, 35, false);
    	
    	getChildren().addAll(playButton, pauseButton, stopButton);
    	
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
    	
    	Label remainingTimeLabel = new Label("5:00");
    	
    	seekBox.getChildren().addAll(currentTimeLabel, timeSlider, remainingTimeLabel);
    	getChildren().add(seekBox);
    	
    }
    
    private void addNowPlayingInfo() {

    	VBox nowPlayingVBox = new VBox();
    	nowPlayingVBox.getStyleClass().add("nowPlaying");
    	nowPlayingVBox.setPrefWidth(350);
    	nowPlayingVBox.setSpacing(5);
    	nowPlayingVBox.setAlignment(Pos.CENTER);
    	nowPlayingVBox.setPadding(new Insets(10, 0, 10, 10));
		
    	Label title = FxUtils.buildLabel("title", "titleLabel", 300, 50, 20, Pos.CENTER);
    	Label artist = FxUtils.buildLabel("artistLabel", "artistLabel", 300, 50, 16, Pos.CENTER);
    	Label album = FxUtils.buildLabel("albumLabel", "albumLabel", 300, 50, 16, Pos.CENTER);
		
    	nowPlayingVBox.getChildren().addAll(title, artist, album);
    	
    	getChildren().add(nowPlayingVBox);
    	
	}
	
}
