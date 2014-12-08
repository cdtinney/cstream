package com.cstream;

import java.util.Map;
import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.cstream.controller.Controller;
import com.cstream.media.LibraryController;
import com.cstream.media.MediaController;
import com.cstream.model.Song;
import com.cstream.server.HttpServer;
import com.cstream.torrent.TorrentClientManager;
import com.cstream.torrent.TorrentManager;
import com.cstream.tracker.TrackerClient;
import com.cstream.tracker.TrackerPeer;
import com.cstream.util.FxUtils;
import com.cstream.util.LibraryUtils;

public class CApplicationController extends Controller {

	private static Logger LOGGER = Logger.getLogger(CApplicationController.class.getName());

	public final static int WIDTH = 1280;
	public final static int HEIGHT = 720;

	// Primary stage
	private Stage stage;
	
	// View
	private CApplicationView view; 

	// Sub-Controllers
	private MediaController mediaController = new MediaController();
	private LibraryController libraryController = new LibraryController();

	// Model
	private TrackerClient client;
	
	// Networking
	private HttpServer server;
	
	// Manager classes
	private TorrentClientManager clientManager;
	private TorrentManager torrentManager;

	public void initialize(Stage stage) {
		
		this.stage = stage;

		view = new CApplicationView(WIDTH, HEIGHT);
		view.initialize();

		stage.setScene(view);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();
		
		client = new TrackerClient(new TrackerPeer());
		//server = new HttpServer(client);

		libraryController.initialize();
		mediaController.initialize(libraryController, client);
		
		addViews();
		
		initLocalLibrary(); 
		
		torrentManager = TorrentManager.getInstance();
		torrentManager.addTorrentsFromLibrary(client.getPeer().getFiles(), client.getPeer().getId());
		
		clientManager = TorrentClientManager.getInstance();
		clientManager.shareAll();
		
		client.start();

		addEventHandlers();
		
	}

	public void stop() {

		LOGGER.info("Attempting to stop the application...");
		client.stop();
		
	}

	private void initLocalLibrary() {
		
		Map<String, Song> files = LibraryUtils.buildLocalLibrary(TorrentManager.FILE_DIR, client.getPeer().getId());
		
		client.setFiles(files);
		
		if (files != null) {
			libraryController.addData(files.values());
		}

	}

	private void addViews() {

		view.addToBorderPane(libraryController.getView(), "center");
		view.addToBorderPane(mediaController.getView(), "bottom");

	}

	private void addEventHandlers() {

		Parent root = view.getRoot();

		addEventHandler(root, "quitMenuItem", "setOnAction", "handleQuitAction");
		addEventHandler(root, "aboutMenuItem", "setOnAction", "handleAboutAction");

	}

	@SuppressWarnings("unused")
	private void handleQuitAction(Event event) {
		stage.fireEvent( new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST) );
	}

	@SuppressWarnings("unused")
	private void handleAboutAction(Event event) {
		FxUtils.showDialog(AlertType.INFORMATION, "About", "cStream", "cStream is a peer-to-peer audo streaming application by Benjamin Sweett & Colin Tinney");
	}

}
