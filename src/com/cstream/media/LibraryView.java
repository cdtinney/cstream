package com.cstream.media;

import com.cstream.model.Song;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

public class LibraryView extends HBox {
	
	private final int TABLE_WIDTH = 1280;
	
	private TableView<Song> libTableView = new TableView<Song>();
	
	// TODO - Hook up library model observer
	private ObservableList<Song> data;

	public void initialize() {

		addColumns();
		
		getChildren().add(libTableView);
		
		libTableView.setPrefWidth(TABLE_WIDTH);
		
	}
	
	@SuppressWarnings("unchecked")
	private void addColumns() {
		 
        TableColumn<Song, String> artistCol = new TableColumn<Song, String>("Artist");
        TableColumn<Song, String> songCol = new TableColumn<Song, String>("Song");
        TableColumn<Song, String> emailCol = new TableColumn<Song, String>("Album");
        
        artistCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        songCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        emailCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        
        libTableView.getColumns().addAll(artistCol, songCol, emailCol);
		
	}

}
