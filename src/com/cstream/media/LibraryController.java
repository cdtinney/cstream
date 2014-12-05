package com.cstream.media;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.control.TableRow;

import com.cstream.controller.Controller;
import com.cstream.model.Song;
import com.cstream.notifier.Notifier;

public class LibraryController extends Controller {

	private static Logger LOGGER = Logger.getLogger(LibraryController.class.getName());

	// View
	private LibraryView view;
	
	//Model
	private Song selectedSong;
	private Song songToPlay;
	
	public void initialize() {
		
		view = new LibraryView();
		view.initialize();
		
		addHandlers();
		
	}
	
	public void addData(Collection<Song> songs) {
		view.addData(songs);
	}
	
	private void handleSongClick(Song song) {
		view.getTable().getSelectionModel().select(song);
		setSongSelected(song);
	}
	
	private void handleSongDblClick(Song song) {
		LOGGER.info("Double click: " + song);
		view.getTable().getSelectionModel().select(song);
		updateToPlay(song);
	}
	
	private void addHandlers() {

		view.getTable().setRowFactory(tv -> {
			
		    TableRow<Song> row = new TableRow<>();
		    
		    row.setOnMouseClicked(event -> {
		    	
		    	if(event.getClickCount() == 1 && (! row.isEmpty())) {
		    		Song rowData = row.getItem();
		    		handleSongClick(rowData);
		    	}
		    	
		        if (event.getClickCount() == 2 && (! row.isEmpty())) {
		            Song rowData = row.getItem();
		            handleSongDblClick(rowData);
		        }
		        
		    });
		    
		    return row ;
		    
		});
		
	}
	
	public Node getView() {
		return view;
	}
	
	public Song getSongToPlay() {
		return this.songToPlay;
	}
	
	private void setSongSelected(Song song) {
		this.selectedSong = song;
	}
	
	public Song getSongSelected() {
		return this.selectedSong;
	}
	
	private void updateToPlay(Song newToPlay) {

		Notifier.getInstance().notify(this, "songToPlay", songToPlay, songToPlay = newToPlay);
		
	}
	
}
