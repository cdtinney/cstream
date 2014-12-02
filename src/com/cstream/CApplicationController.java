package com.cstream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.TextInputDialog;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import com.cstream.controller.Controller;
import com.cstream.media.MediaBarController;
import com.cstream.media.MediaInfoController;
import com.cstream.media.MediaLibraryController;
import com.cstream.model.Song;
import com.cstream.tracker.TrackerClient;
import com.cstream.tracker.TrackerPeer;
import com.cstream.utils.LibraryUtils;
import com.cstream.utils.OSUtils;
import com.sun.org.apache.bcel.internal.generic.ISUB;

public class CApplicationController extends Controller {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(CApplicationController.class.getName());
	
	public final static int WIDTH = 1280;
	public final static int HEIGHT = 720;
	
	private Stage stage;
	
	private CApplicationView view; 
	
	// Sub-Controllers
	private MediaBarController mediaBarController = new MediaBarController();
	private MediaLibraryController mediaLibController = new MediaLibraryController();
	private MediaInfoController mediaInfoController = new MediaInfoController();
	
	// TODO: MediaPlayer needs a source of Media to be initialized
	private MediaPlayer mp;
	
	// TODO: RTP Instance
	
	//This Peer
	private TrackerPeer peer;
	
	public void initialize(Stage stage) {
		this.stage = stage;
		
		view = new CApplicationView(WIDTH, HEIGHT);
		view.initialize();
		
		initializeSubControllers();
		addSubViews();
		
		stage.setScene(view);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();
		
		initializeSubControllers();
		addEventHandlers();
		addEventListeners();
		
		//TODO: Check that library was returned correctly and isnt empty
		// Log warning or display info to user?
		Map<String, Song> library = buildLibrary();
		
		//TODO: Get port? and build peer object for this client
		//peer = new TrackerPeer(port, library);
	}
	
	public void stop() {
		//TODO: Stop networking
	}
	
	private void initializeSubControllers() {
		
		// NOTE: Pass networking to each controller
		mediaBarController.initialize(mp);
		mediaLibController.initialize();
		
	}
	
	private void addSubViews() {
		
		view.addToBorderPane(mediaBarController.getView(), "bottom");
		view.addToBorderPane(mediaLibController.getView(), "right");
		view.addToBorderPane(mediaInfoController.getView(), "left");

	}
	
	protected void handleQuitAppMenuItemAction(Event event) {
		// TODO
	}
	
	protected void handleAboutMenuItemAction(Event event) {
		// TODO
	}
		
	private void addEventHandlers() {
		
		Parent root = view.getRoot();
		
		addEventHandler(root, "quitAppMenuItem", "setOnAction", "handleQuitAppMenuItemAction");
		addEventHandler(root, "aboutMenuItem", "setOnAction", "handleAboutMenuItemAction");
		
	}
	
	private void addEventListeners() {
		
		//PropertyChangeDispatcher.getInstance().addListener(GameClient.class, "connected", this, "onConnectionChange");
		
	}
	
	private Map<String, Song> buildLibrary() {
		
		Map<String, Song> library = new HashMap<String, Song>();
		String path = "";
		String folder = showPathDialog();
		String slash = "/";
		
		if (OSUtils.isWindows()) {
			slash = "\\";
		}
		
		if(folder != null) {
			path = System.getProperty("user.home") + slash + folder;
		}
		
		if(path != null) {
			library = LibraryUtils.buildLocalLibrary(path);
		}
		
		return library;
	}
	
	
	protected String showPathDialog() {
		
		String defaultV = "/Music";
		
		TextInputDialog dialog = new TextInputDialog(defaultV);
		dialog.setTitle("Music Library Path");
		dialog.setHeaderText("To share your music with others enter" +
		"a path to your music folder. Defaults from : " + System.getProperty("user.home") + defaultV);
		dialog.setContentText("Path to Music: ");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		if (result.isPresent()){
		    return result.get();
		}

		return "";
	}
	
}
