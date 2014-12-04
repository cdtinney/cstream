package com.cstream;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import com.cstream.controller.Controller;
import com.cstream.media.LibraryController;
import com.cstream.media.MediaBarController;
import com.cstream.media.MediaInfoController;
import com.cstream.model.Song;
import com.cstream.tracker.TrackerClient;
import com.cstream.tracker.TrackerPeer;
import com.cstream.utils.LibraryUtils;
import com.cstream.utils.OSUtils;

public class CApplicationController extends Controller {

	private static Logger LOGGER = Logger.getLogger(CApplicationController.class.getName());
	
	public final static int WIDTH = 1280;
	public final static int HEIGHT = 720;
	
	private final static String DEFAULT_BASE_DIR = System.getProperty("user.home");
	
	private Stage stage;
	
	private CApplicationView view; 
	
	// Sub-Controllers
	private MediaBarController mediaBarController = new MediaBarController();
	private MediaInfoController mediaInfoController = new MediaInfoController();
	
	private LibraryController libraryController = new LibraryController();
	
	// TODO: MediaPlayer needs a source of Media to be initialized
	private MediaPlayer mp;
	
	// TODO: RTP Instance
	
	// Model
	private TrackerPeer peer;
	
	public void initialize(Stage stage) {
		this.stage = stage;
		
		view = new CApplicationView(WIDTH, HEIGHT);
		view.initialize();
		
		initializeSubControllers();
		addSubViews();
		
		initializeStage();

		initializeSubControllers();
		addEventHandlers();
		addEventListeners();
		
		initializePeerLib();
	
	}
	
	public void stop() {
		LOGGER.info("Stop");
		//TODO: Close all open streams and connections
		
		//Send a remove request to tracker and wait for response before exiting
		if(!peer.removeTracker()) {
			LOGGER.warning("Tracker failed to approve disconnect");
		}
		
		Platform.exit();
	}
	
	private String showPathDialog() {
		
		String defaultDir = DEFAULT_BASE_DIR;
		if (OSUtils.isWindows()) {
			defaultDir += "\\cstream";
			
		} else {
			defaultDir += "/cstream";
			
		}
		
		TextInputDialog dialog = new TextInputDialog(defaultDir);
		dialog.setTitle("Music Library Path");
		dialog.setHeaderText("Please enter the directory path you would like to share (default: " + defaultDir + ")");
		
		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		    return result.get();
		}

		return "";
		
	}
	
	private void showAboutDialog() {
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About cStream");
		alert.setContentText("cStream is a P2P Audio Streaming Application by Benjamin Sweett & Colin Tinney.");
		alert.showAndWait();
		
	}
	
	private void initializeStage() {
		
		stage.setScene(view);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();
		
	}
	
	private void initializeSubControllers() {
		
		// NOTE: Pass networking to each controller
		mediaBarController.initialize(mp);
		libraryController.initialize();
		
	}
	
	private void initializePeerLib() {
		
		String libDir = showPathDialog();
		peer = new TrackerPeer(libDir);
		
		Map<String, Song> library = peer.getFiles();
		if(library != null) {
			libraryController.addData(library.values());
		}
		
	}
	
	private void addSubViews() {
		
		view.addToBorderPane(mediaBarController.getView(), "bottom");
		view.addToBorderPane(libraryController.getView(), "center");
		
		//view.addToBorderPane(mediaInfoController.getView(), "right");

	}
		
	private void addEventHandlers() {
		
		Parent root = view.getRoot();
		
		addEventHandler(root, "quitMenuItem", "setOnAction", "handleQuitAction");
		addEventHandler(root, "aboutMenuItem", "setOnAction", "handleAboutAction");
		
	}
	
	private void addEventListeners() {
		
		//PropertyChangeDispatcher.getInstance().addListener(GameClient.class, "connected", this, "onConnectionChange");
		
	}
	
	@SuppressWarnings("unused")
	private void handleQuitAction(Event event) {
		LOGGER.info("Quit");
		stop();
	}
	
	@SuppressWarnings("unused")
	private void handleAboutAction(Event event) {
		LOGGER.info("About");
		showAboutDialog();
	}
	
}
