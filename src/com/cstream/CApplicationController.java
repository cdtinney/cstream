package com.cstream;

import java.util.Map;
import java.util.logging.Logger;

import javafx.scene.Scene;

import com.cstream.client.HttpTransferClient;
import com.cstream.controller.Controller;
import com.cstream.media.MediaController;
import com.cstream.model.Song;
import com.cstream.torrent.TorrentClientManager;
import com.cstream.torrent.TorrentManager;
import com.cstream.tracker.TrackerClient;
import com.cstream.tracker.TrackerPeer;
import com.cstream.util.LibraryUtils;

public class CApplicationController extends Controller {

	private static Logger LOGGER = Logger.getLogger(CApplicationController.class.getName());
	
	// Primary scene
	private CApplicationView scene; 

	// Sub-Controller
	private MediaController mediaController = new MediaController();

	// Model
	private TrackerClient client;
	
	// Manager classes
	private TorrentClientManager clientManager;
	private TorrentManager torrentManager;

	public void initialize() {

		scene = new CApplicationView(CApplication.WIDTH, CApplication.HEIGHT);
		scene.initialize();
		
		client = new TrackerClient(new TrackerPeer());
		mediaController.initialize(client);
		
		addViews();
		
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public void start() {
		
		initLocalLibrary(); 
		
		torrentManager = TorrentManager.getInstance();
		torrentManager.addTorrentsFromLibrary(client.getPeer().getFiles(), client.getPeer().getId());
		
		clientManager = TorrentClientManager.getInstance();
		clientManager.shareAll(mediaController.getLibraryView());
		HttpTransferClient.downloadTorrents("192.168.1.109", "6970");
		
	}

	public void stop() {

		LOGGER.info("Attempting to stop the application...");
		
	}

	private void initLocalLibrary() {
		
		Map<String, Song> files = LibraryUtils.buildLocalLibrary(TorrentManager.FILE_DIR, client.getPeer().getId());
		client.setFiles(files);

	}

	private void addViews() {

		scene.addToBorderPane(mediaController.getActionView(), "top");
		scene.addToBorderPane(mediaController.getLibraryView(), "center");
		scene.addToBorderPane(mediaController.getMediaView(), "bottom");

	}

}
