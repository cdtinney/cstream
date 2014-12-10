package com.cstream.media;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
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

import com.cstream.client.TorrentClientManager;
import com.cstream.logging.LogLevel;
import com.cstream.song.Song;
import com.cstream.util.EnumUtils;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;


public class LibraryView extends HBox {

	private static Logger LOGGER = Logger.getLogger(LibraryView.class.getName());
	
	private final int TABLE_WIDTH = 1280;
	
	private TableView<SharedTorrent> table = new TableView<SharedTorrent>();
	
	// View
	private TableView<Song> libTableView = new TableView<Song>();
	
	// Model
	private ObservableList<SharedTorrent> data = FXCollections.observableArrayList();

	public void initialize() {
		
		table.setPrefWidth(TABLE_WIDTH);
		
		addTableColumns();
		getChildren().add(table);
		
		table.setItems(data);
		
	}
	
	public void addItem(SharedTorrent torrent) {
		data.add(torrent);
	}
	
	public void addItems(Collection<SharedTorrent> torrents) {
		data.addAll(torrents);		
	}
	
	public void setItems(ObservableList<SharedTorrent> torrents) {
		data.addAll(torrents);
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
		
//		for (Song s : data) {
//			
//			if (s.getId().equals(song.getId())) {
//				libTableView.getSelectionModel().select(s);
//			}
//			
//		}
		
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
        	
        	TorrentClientManager manager = TorrentClientManager.getInstance();
        	Client client = manager.findClient(t);
        	
        	if (client == null) {
        		return new SimpleStringProperty("Stopped");
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
                	if (torrent == null) {
                		setText(null);
                		setStyle("");
                		return;
                	}
                	
                	TorrentClientManager manager = TorrentClientManager.getInstance();
                	Client client = manager.findClient(torrent);
                	
                	if (client == null) {
                        this.setStyle("-fx-background-color: none"); 
                    	this.setTextFill(Color.BLACK);                  	
                    	setText(item.toString());
                    	return;
                	}
                	
                	ClientState state = EnumUtils.lookup(ClientState.class, item.toString());
                	if (state == null) {
                        this.setStyle("-fx-background-color: none"); 
                    	this.setTextFill(Color.BLACK);                 	
                    	setText(item.toString()); 
                    	return;
                	}
                	
                	switch (state) {
                	
                		case WAITING:
                		case VALIDATING:
                            this.setStyle("-fx-background-color: #FF9900");
                			break;
                			
                		case SHARING:
                            this.setStyle("-fx-background-color: #00CC00");
                			break;
                			
                		case SEEDING:
                            this.setStyle("-fx-background-color: #00CC00");
                			break;
                			
                		case DONE:
                            this.setStyle("-fx-background-color: #006600");
                			break;
                			
                		case ERROR:
                            this.setStyle("-fx-background-color: ##CC0000");
                			break; 
                			
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

	public void updateItem(SharedTorrent torrent) {

		int index = data.indexOf(torrent);
		if (index < 0) {
			
			// Search by hash
			for (Torrent t : data) {
				
				if (t.getHexInfoHash().equals(torrent.getHexInfoHash())) {
					
					LOGGER.info("Replacing torrent in library view: " + torrent.getName());
					index = data.indexOf(t);
					data.set(index, null);
					data.set(index, torrent);
					return;
					
				}
				
			}
			
			LOGGER.warning("No torrent found in library view: " + torrent.getName());
			return;
			
		}
		
		LOGGER.info("Updating torrent at index: " + index);
		data.set(index, null);
		data.set(index, torrent);
		
	}

}
