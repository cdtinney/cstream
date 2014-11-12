package com.cstream.Media;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class MediaInfoView extends VBox {

	public void initialize() {
		setPrefWidth(500);
		setAlignment(Pos.CENTER_LEFT);
		setPadding(new Insets(10, 10, 10, 10));
		
		addFileThumbNail();
		addDetailLabels();
	}
	
	private void addFileThumbNail() {
		
	}
	
	private void addDetailLabels() {
		// TODO: song, artist, album, play count, added on, produced, etc..
	}
	
	
}
