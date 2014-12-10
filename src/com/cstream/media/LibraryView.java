package com.cstream.media;

import java.text.DecimalFormat;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import com.cstream.client.TorrentClientManager;
import com.cstream.util.EnumUtils;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

public class LibraryView extends HBox {

	private static Logger LOGGER = Logger.getLogger(LibraryView.class.getName());
	
	private final int TABLE_WIDTH = 1280;
	
	private TableView<SharedTorrent> table = new TableView<SharedTorrent>();
	private ObservableList<SharedTorrent> data = FXCollections.observableArrayList();

	public void initialize() {
		
		table.setPrefWidth(TABLE_WIDTH);
		
		addTableColumns();
		getChildren().add(table);
		
		table.setItems(data);
		table.setPlaceholder(new Label("No shared songs found."));
		
	}
	
	public void addItem(SharedTorrent torrent) {
		
		data.add(torrent);
		
		// Re-sort it
		FXCollections.sort(table.getItems(), (t1, t2) -> t1.getName().compareTo(t2.getName()));
		
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
		
		data.set(index, null);
		data.set(index, torrent);
		
	}
	
	public SharedTorrent getSelected() {
		return table.getSelectionModel().getSelectedItem();
	}
	
	public TableView<SharedTorrent> getTable() {
		return table;
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
        	DecimalFormat df = new DecimalFormat("###.##");        	
			return new SimpleStringProperty(df.format(t.getCompletion()) + "%");
			
		});

        TableColumn<SharedTorrent, String> dlCol = new TableColumn<SharedTorrent, String>("DL");
        dlCol.setCellValueFactory(row -> {
			
        	SharedTorrent t = row.getValue();
        	
        	Client client = TorrentClientManager.getInstance().findClient(t);        	
        	if (client == null) {
        		return new SimpleStringProperty("");
        	}

        	DecimalFormat formatter = new DecimalFormat("###,###,###");
        	float kbPerSec = client.getDownloadRate() / 1000;
			return new SimpleStringProperty(kbPerSec == 0 ? "" : formatter.format(kbPerSec) + " KB/s");
			
		});

        TableColumn<SharedTorrent, String> ulCol = new TableColumn<SharedTorrent, String>("UL");
        ulCol.setCellValueFactory(row -> {
			
        	SharedTorrent t = row.getValue();
        	
        	Client client = TorrentClientManager.getInstance().findClient(t);        	
        	if (client == null) {
        		return new SimpleStringProperty("");
        	}

        	DecimalFormat formatter = new DecimalFormat("###,###,###");
        	float kbPerSec = client.getUploadRate() / 1000;
			return new SimpleStringProperty(kbPerSec == 0 ? "" : formatter.format(kbPerSec) + " KB/s");
			
		});

        TableColumn<SharedTorrent, String> sizeCol = new TableColumn<SharedTorrent, String>("Size");
        sizeCol.setCellValueFactory(row -> {
			
        	SharedTorrent t = row.getValue();        	
        	DecimalFormat formatter = new DecimalFormat("###,###,###");        	
			return new SimpleStringProperty(formatter.format(t.getSize() / 1000) + " KB");
			
		});

        TableColumn<SharedTorrent, String> stateCol = new TableColumn<SharedTorrent, String>("State");
        stateCol.setCellValueFactory(row -> {
        	
			SharedTorrent t = row.getValue();
        	
        	Client client = TorrentClientManager.getInstance().findClient(t);
        	if (client == null) {
        		return new SimpleStringProperty("Inactive");
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
		
		nameCol.prefWidthProperty().bind(table.widthProperty().divide(2.5));
		percentCol.prefWidthProperty().bind(table.widthProperty().divide(10));
		dlCol.prefWidthProperty().bind(table.widthProperty().divide(10));;
		ulCol.prefWidthProperty().bind(table.widthProperty().divide(10));
		sizeCol.prefWidthProperty().bind(table.widthProperty().divide(10));
		stateCol.prefWidthProperty().bind(table.widthProperty().divide(5));
		
		table.getColumns().addAll(nameCol, percentCol, dlCol, ulCol, sizeCol, stateCol);
		
	}

}
