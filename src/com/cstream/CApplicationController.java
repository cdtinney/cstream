package com.cstream;

import java.util.Optional;
import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.TextInputDialog;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import com.cstream.controller.Controller;
import com.cstream.media.LibraryController;
import com.cstream.media.MediaBarController;
import com.cstream.media.MediaInfoController;
import com.cstream.tracker.TrackerPeer;
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
	private LibraryController mediaLibController = new LibraryController();
	private MediaInfoController mediaInfoController = new MediaInfoController();
	
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
		
//		String libDir = showPathDialog();
//		if (!libDir.isEmpty()) {
//			Map<String, Song> library = LibraryUtils.buildLocalLibrary(libDir);
//			
//		}
		
	}
	
	public void stop() {
		LOGGER.info("Stop");
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
	
	private void initializeStage() {
		
		stage.setScene(view);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();
		
	}
	
	private void initializeSubControllers() {
		
		// NOTE: Pass networking to each controller
		mediaBarController.initialize(mp);
		mediaLibController.initialize();
		
	}
	
	private void addSubViews() {
		
		view.addToBorderPane(mediaBarController.getView(), "bottom");
		view.addToBorderPane(mediaLibController.getView(), "center");
		
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
		// TODO 
	}
	
	@SuppressWarnings("unused")
	private void handleAboutAction(Event event) {
		LOGGER.info("About");
		// TODO
	}
	
}
