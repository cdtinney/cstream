package com.cstream.media;

import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import com.cstream.logging.LogLevel;
import com.cstream.model.Song;
import com.cstream.torrent.TorrentClientManager;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

public class LibraryView extends HBox {

	private static Logger LOGGER = Logger.getLogger(LibraryView.class.getName());
	
	private final int TABLE_WIDTH = 1280;
	
	private TableView<SharedTorrent> table = new TableView<SharedTorrent>();
	
	// View
	private TableView<Song> libTableView = new TableView<Song>();
	
	// Model
	private ObservableList<Song> data = FXCollections.observableArrayList();

	public void initialize() {
		
		table.setPrefWidth(TABLE_WIDTH);
		
		addTableColumns();
		getChildren().add(table);
		
	}
	
	public void setItems(ObservableList<SharedTorrent> torrents) {
		table.setItems(torrents);
	}
	
	public SharedTorrent getSelected() {
		return table.getSelectionModel().getSelectedItem();
	}
	
	public TableView<SharedTorrent> getTable() {
		return table;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void onLibraryChanged(PropertyChangeEvent evt) {

		LOGGER.log(LogLevel.DEBUG, "Updating library view");
		
		Song selectedSong = libTableView.getSelectionModel().getSelectedItem();
				
		Map<String, Song> oldLib = (Map<String, Song>) evt.getOldValue();
		Map<String, Song> newLib = (Map<String, Song>) evt.getNewValue();
		
		// Clear the saved library, and create a new list
		data.clear();
		
		// Add all of the updated songs
		//addData(newLib.values());		
		
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
	
	@SuppressWarnings("unchecked")
	private void addTableColumns() {

        TableColumn<SharedTorrent, String> nameCol = new TableColumn<SharedTorrent, String>("Name");
		nameCol.setCellValueFactory(row -> {
			
			SharedTorrent t = row.getValue();
			return new SimpleStringProperty(t.getName());
			
		});

        TableColumn<SharedTorrent, String> percentCol = new TableColumn<SharedTorrent, String>("Percentage");
        percentCol.setCellValueFactory(row -> {
			
        	SharedTorrent t = row.getValue();
			return new SimpleStringProperty(Float.toString(t.getCompletion()) + "%");
			
		});

        TableColumn<SharedTorrent, String> peersCol = new TableColumn<SharedTorrent, String>("Peers");
        peersCol.setCellValueFactory(row -> {
			
        	//SharedTorrent t = row.getValue();
        	// TODO
			return new SimpleStringProperty("TODO");
			
		});

        TableColumn<SharedTorrent, String> stateCol = new TableColumn<SharedTorrent, String>("State");
        stateCol.setCellValueFactory(row -> {
        	
			SharedTorrent t = row.getValue();
        	
        	LOGGER.info("Updating state column for torrent: " + t.getName());
        	
        	TorrentClientManager manager = TorrentClientManager.getInstance();
        	Client client = manager.findClient(t);
        	
        	if (client == null) {
        		
        		// TODO - This returns incomplete if the file is locally complete but tracker is down
        		String state = t.isComplete() ? "Complete" : "Incomplete";
        		return new SimpleStringProperty(state);
        		
        	}
        	
        	return new SimpleStringProperty(client.getState().toString());
			
		});
        
        stateCol.setCellFactory(column -> {
        	
        	return new TableCell<SharedTorrent, String>() {
        		
                @Override
                public void updateItem(String item, boolean empty) {
                	super.updateItem(item, empty);
                	
                	if (item == null || empty) {
                		setText(null);
                		setStyle("");
                		return;
                	}
                	
                	SharedTorrent torrent = (SharedTorrent) this.getTableRow().getItem();
                	if (torrent.isComplete()) {
                        this.setStyle("-fx-background-color:green");
                        
                	}
                	
                	this.setTextFill(Color.WHITE);                	
                	setText(item.toString());
                	
                }
                
            };
        	
        });
		
		nameCol.prefWidthProperty().bind(table.widthProperty().divide(3));
		percentCol.prefWidthProperty().bind(table.widthProperty().divide(3));
		stateCol.prefWidthProperty().bind(table.widthProperty().divide(3));
		
		table.getColumns().addAll(nameCol, percentCol, stateCol);
		
		
	}

}
