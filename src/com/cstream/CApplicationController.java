package com.cstream;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.biasedbit.efflux.participant.RtpParticipant;
import com.cstream.controller.Controller;
import com.cstream.media.LibraryController;
import com.cstream.media.MediaController;
import com.cstream.model.Song;
import com.cstream.tracker.TrackerClient;
import com.cstream.tracker.TrackerPeer;
import com.cstream.utils.FxUtils;
import com.cstream.utils.OSUtils;

public class CApplicationController extends Controller {

	private static Logger LOGGER = Logger.getLogger(CApplicationController.class.getName());

	public final static int WIDTH = 1280;
	public final static int HEIGHT = 720;

	private final static String DEFAULT_BASE_DIR = System.getProperty("user.home");

	// Primary stage
	private Stage stage;
	
	// View
	private CApplicationView view; 

	// Sub-Controllers
	private MediaController mediaController = new MediaController();
	private LibraryController libraryController = new LibraryController();

	// Model
	private TrackerPeer peer;

	public void initialize(Stage stage) {
		this.stage = stage;

		view = new CApplicationView(WIDTH, HEIGHT);
		view.initialize();

		initializeControllers();
		addViews();

		stage.setScene(view);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();

		initializePeerLib(); 
		connectToTracker();

		addEventHandlers();
	}

	public void stop() {

		LOGGER.info("Attempting to stop the application...");
		
		if (!peer.removeTracker()) {
			LOGGER.warning("Request to remove peer from tracker was not successful");
			
		} else {
			LOGGER.info("Reqeust to remove peer from tracker was successful");
			
		}
		
		// TODO - Place elsewhere - It would be ideal if classes can simply subscribe to a quit event
		TrackerClient.closeSocket();
		
	}

	// TODO: Display this after the view has been displayed
	private String showPathDialog() {

		String defaultDir = DEFAULT_BASE_DIR;
		defaultDir += OSUtils.isWindows() ? "\\cstream" : "/cstream";

		TextInputDialog dialog = new TextInputDialog(defaultDir);
		dialog.setTitle("Music Library Path");
		dialog.setHeaderText("Please enter the directory path you would like to share (default: " + defaultDir + ")");

		Optional<String> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : "";

	}

	private void initializeControllers() {

		libraryController.initialize();
		mediaController.initialize();

	}

	private void initializePeerLib() {

		String libDir = showPathDialog();
		peer = new TrackerPeer(libDir);

		Map<String, Song> library = peer.getFiles();
		if (library != null) {
			libraryController.addData(library.values());
		}

	}

	private void connectToTracker() {

		if (!peer.joinTracker()) {
			LOGGER.warning("Failed to join tracking service");
			return;
			
			//TODO: Display this dialog like the path one after the full view has opened ?
			//showWarningDialog("Network Warning","Failed to join tracking service. You are viewing your local offline library.");
		}
		
		TrackerClient.initializeSocket();
		
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
