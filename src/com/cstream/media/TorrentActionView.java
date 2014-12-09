package com.cstream.media;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import com.cstream.util.FxUtils;

public class TorrentActionView extends HBox {
	
	private Button startButton;
	private Button stopButton;

	public void initialize() {
		
    	setSpacing(5);
    	setPrefHeight(50);
    	setAlignment(Pos.CENTER_LEFT);
    	setPadding(new Insets(5, 5, 5, 15));
    	
    	startButton = FxUtils.buildButton("Start", "startButton", 75, 20, false);
    	stopButton = FxUtils.buildButton("Stop", "stopButton", 75, 20, false);
    	
    	getChildren().addAll(startButton, stopButton);		
		
	}
	
	public Button getStartButton() {
		return startButton;
	}
	
	public Button getStopButton() {
		return stopButton;
	}

}
