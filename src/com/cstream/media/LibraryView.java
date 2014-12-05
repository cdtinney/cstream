package com.cstream.media;

import java.util.Collection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import com.cstream.model.Song;

public class LibraryView extends HBox {
	
	private final int TABLE_WIDTH = 1280;
	
	// View
	private TableView<Song> libTableView = new TableView<Song>();
	
	// Model
	private ObservableList<Song> data = FXCollections.observableArrayList();

	public void initialize() {

		addColumns();
		
		getChildren().add(libTableView);
		
		libTableView.setPrefWidth(TABLE_WIDTH);
		
	}
	
	public void addData(Collection<Song> songs) {

		libTableView.setItems(data);
		
		for (Song s: songs) {
			data.add(s);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void addColumns() {
		 
        TableColumn<Song, String> artistCol = new TableColumn<Song, String>("Artist");
        artistCol.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));
        
        TableColumn<Song, String> titleCol = new TableColumn<Song, String>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        
        TableColumn<Song, String> albumCol = new TableColumn<Song, String>("Album");
        albumCol.setCellValueFactory(new PropertyValueFactory<Song, String>("album"));
        
        // TODO - More columns
        
        artistCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        titleCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        albumCol.prefWidthProperty().bind(libTableView.widthProperty().divide(3));
        
        libTableView.getColumns().addAll(artistCol, titleCol, albumCol);
		
	}

}
