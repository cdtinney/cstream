package com.cstream.media;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MediaInfoView extends VBox {

	public void initialize() {
		setSpacing(5);
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
		Label title = new Label("Title: ");
		title.setPrefWidth(130);
		title.setMinWidth(50);
    	getChildren().add(title);
    	
    	Label artist = new Label("Artist: ");
    	artist.setPrefWidth(130);
    	artist.setMinWidth(50);
    	getChildren().add(artist);
    	
    	Label album = new Label("Album: ");
    	album.setPrefWidth(130);
    	album.setMinWidth(50);
    	getChildren().add(album);
    	
    	Label playcount = new Label("Plays: ");
    	playcount.setPrefWidth(130);
    	playcount.setMinWidth(50);
    	getChildren().add(playcount);
    	
    	Label addedDate = new Label("Added: ");
    	addedDate.setPrefWidth(130);
    	addedDate.setMinWidth(50);
    	getChildren().add(addedDate);
	}
	
	
}
