package com.cstream.media;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

public class MediaLibraryView extends HBox {

	public void initialize() {
		
		setPrefWidth(600);

		addArtistList();
		addSongList();
		
	}

	private void addArtistList() {
		
		ObservableList<String> names = FXCollections.observableArrayList();
		ObservableList<String> data = FXCollections.observableArrayList();

		ListView<String> artists = new ListView<String>(data);
		artists.setEditable(false);

		names.addAll(
				"Adam", "Alex", "Alfred", "Albert",
				"Brenda", "Connie", "Derek", "Donny", 
				"Lynne", "Myrtle", "Rose", "Rudolph", 
				"Tony", "Trudy", "Williams", "Zach"
				);

		for (int i = 0; i < 18; i++) {
			data.add("anonym");
		}

		artists.setItems(names);
		//artists.setCellFactory(ComboBoxListCell.forListView(names));              
		getChildren().add(artists);
		
	}

	private void addSongList() {
		
		ObservableList<String> names = FXCollections.observableArrayList();
		ObservableList<String> data = FXCollections.observableArrayList();

		ListView<String> songs = new ListView<String>(data);
		songs.setEditable(false);

		names.addAll(
				"Adam", "Alex", "Alfred", "Albert",
				"Brenda", "Connie", "Derek", "Donny", 
				"Lynne", "Myrtle", "Rose", "Rudolph", 
				"Tony", "Trudy", "Williams", "Zach"
				);

		for (int i = 0; i < 18; i++) {
			data.add("anonym");
		}

		songs.setItems(names);
		//songs.setCellFactory(ComboBoxListCell.forListView(names));              
		getChildren().add(songs);
		
	}

}
