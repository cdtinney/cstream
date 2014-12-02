package com.cstream.media;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class MediaBarView extends HBox {

	private Label status;
	
    public void initialize() {    	
    	setSpacing(5);
    	setPrefHeight(150);
    	setAlignment(Pos.CENTER);
    	getStyleClass().add("mediabar");
    	
    	// TODO: Find icon pack for controls
    	addPlayButton();
    	addTimeSlider();
    	addVolumeSlider();
    	addStatusText();
    	
    }
    
    public void setStatusText(String message) {

		if (status == null) {
			return;
		}
		
		status.setText("STATUS - " + (message == null? "" : message));	
		
	}

    private void addPlayButton() {
    	
    	Button playButton = new Button("Play"); 	
    	playButton.setId("playButton");
    	getStyleClass().add("playbutton");
    	playButton.getStyleClass().addAll("nofocus");
    	playButton.setPrefWidth(100);
    	playButton.setPrefHeight(100);
    	playButton.setDisable(true);
		getChildren().add(playButton);
		
    }
    
    private void addTimeSlider() {
    	
    	Label timeLabel = new Label("Time: ");
    	getChildren().add(timeLabel);
    	
    	Slider timeSlider = new Slider();
    	timeSlider.setId("timeSlider");
    	timeSlider.setMinWidth(50);
    	timeSlider.setPrefWidth(400);
    	timeSlider.setMaxWidth(Double.MAX_VALUE);
    	timeSlider.setDisable(true);
    	getChildren().add(timeSlider);
    	
    	
    	Label playTime = new Label();
    	playTime.setPrefWidth(130);
    	playTime.setMinWidth(50);
    	getChildren().add(playTime);
    	
    }
    
    private void addVolumeSlider() {
    	
    	Slider volumeSlider = new Slider();
    	volumeSlider.setPrefWidth(100);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setMinWidth(30);
        getChildren().add(volumeSlider);
        
    }	
	
	private void addStatusText() {
		
		status = new Label("STATUS - ");
		status.setFont(new Font("Lucida Console", 12));
		status.getStyleClass().add("statusText");
		status.setPrefWidth(200);
		
		getChildren().add(status);
		
	}
	
}
