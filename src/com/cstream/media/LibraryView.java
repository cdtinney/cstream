package com.cstream.media;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import com.cstream.logging.LogLevel;
import com.cstream.model.Song;
import com.cstream.notifier.Notifier;
import com.cstream.tracker.TrackerClient;

public class LibraryView extends HBox {

	private static Logger LOGGER = Logger.getLogger(LibraryView.class.getName());
	
	private final int TABLE_WIDTH = 1280;
	
	// View
	private TableView<Song> libTableView = new TableView<Song>();
	
	// Model
	private ObservableList<Song> data = FXCollections.observableArrayList();

	public void initialize() {

		addColumns();

		libTableView.setPrefWidth(TABLE_WIDTH);
		getChildren().add(libTableView);
		
		addListeners();

		libTableView.setItems(data);
		
	}
	
	public void addData(Collection<Song> songs) {
		data.addAll(songs);
	}
	
	public TableView<Song> getTable() {
		return libTableView;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void onLibraryChanged(PropertyChangeEvent evt) {
		
		Song selectedSong = libTableView.getSelectionModel().getSelectedItem();

		LOGGER.log(LogLevel.DEBUG, "On library changed event");
				
		Map<String, Song> oldLib = (Map<String, Song>) evt.getOldValue();
		Map<String, Song> newLib = (Map<String, Song>) evt.getNewValue();
		
		// Clear the saved library, and create a new list
		data.clear();
		
		// Add all of the updated songs
		data.addAll(newLib.values());		
		
		// Try to re-select the previous song
		selectSong(selectedSong);
		
		// Sort the songs
	    FXCollections.sort(libTableView.getItems());

	}
	
	private void selectSong(Song song) {
		
		if (song == null) {
			return;
		}
		
		for (Song s : data) {
			
			if (s.getId().equals(song.getId())) {
				libTableView.getSelectionModel().select(s);
			}
			
		}
		
	}
	
	private void addListeners() {
		
		Notifier.getInstance().addListener(TrackerClient.class, "sharedLibrary", this, "onLibraryChanged", true);
		
	}
	
	@SuppressWarnings("unchecked")
	private void addColumns() {
		 
        TableColumn<Song, String> artistCol = new TableColumn<Song, String>("Artist");
        artistCol.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));
        
        TableColumn<Song, String> titleCol = new TableColumn<Song, String>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        
        TableColumn<Song, String> albumCol = new TableColumn<Song, String>("Album");
        albumCol.setCellValueFactory(new PropertyValueFactory<Song, String>("album"));
        
        artistCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        titleCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        albumCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        
        libTableView.getColumns().addAll(artistCol, titleCol, albumCol);
		
	}

}
