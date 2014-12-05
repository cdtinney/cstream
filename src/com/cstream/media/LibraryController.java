package com.cstream.media;

import java.util.Collection;
import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.control.TableRow;

import com.cstream.controller.Controller;
import com.cstream.model.Song;

public class LibraryController extends Controller {

	private static Logger LOGGER = Logger.getLogger(LibraryController.class.getName());

	private LibraryView view;
	
	public void initialize() {
		
		view = new LibraryView();
		view.initialize();
		
		addHandlers();
		addListeners();
		
	}
	
	public void addData(Collection<Song> songs) {
		view.addData(songs);
	}
	
	private void handleSongDblClick(Song song) {
		LOGGER.info("Double click: " + song);
	}
	
	private void addHandlers() {

		view.getTable().setRowFactory(tv -> {
			
		    TableRow<Song> row = new TableRow<>();
		    
		    row.setOnMouseClicked(event -> {
		    	
		        if (event.getClickCount() == 2 && (! row.isEmpty())) {
		            Song rowData = row.getItem();
		            handleSongDblClick(rowData);
		        }
		        
		    });
		    
		    return row ;
		    
		});
		
	}
	
	private void addListeners() {
		// TODO 
	}
	
	public Node getView() {
		return view;
	}
	
}
