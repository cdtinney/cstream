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

import com.cstream.controller.Controller;
import com.cstream.media.LibraryController;
import com.cstream.media.MediaBarController;
import com.cstream.model.Song;
import com.cstream.tracker.TrackerPeer;
import com.cstream.utils.FxUtils;
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

		initializeControllers();
		addViews();

		stage.setScene(view);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();

		initializePeerLib(); // Should be done before they can use the UI
		connectToTracker();

		addEventHandlers();
		addEventListeners();

	}

	public void stop() {

		LOGGER.info("Attempting to stop the application...");
		
		//TODO: Close all open streams and connections
		
		if (!peer.removeTracker()) {
			LOGGER.warning("Request to remove peer from tracker was not successful");
			return;
			
		}
		
		LOGGER.info("Reqeust to remove peer from tracker was successful");
		
	}

	//TODO: Display this after the view has been displayed
	private String showPathDialog() {

		String defaultDir = DEFAULT_BASE_DIR;
		defaultDir += OSUtils.isWindows() ? "\\cstream" : "/cstream";

		TextInputDialog dialog = new TextInputDialog(defaultDir);
		dialog.setTitle("Music Library Path");
		dialog.setHeaderText("Please enter the directory path you would like to share (default: " + defaultDir + ")");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : "";

	}

	private void initializeControllers() {

		// TODO - Pass networking to controllers (if necessary...)
		libraryController.initialize();
		mediaBarController.initialize(mp);

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
			//TODO: Display this dialog like the path one after the full view has opened ?
			//showWarningDialog("Network Warning","Failed to join tracking service. You are viewing your local offline library.");
		}

	}

	private void addViews() {

		view.addToBorderPane(libraryController.getView(), "center");
		view.addToBorderPane(mediaBarController.getView(), "bottom");

	}

	private void addEventHandlers() {

		Parent root = view.getRoot();

		addEventHandler(root, "quitMenuItem", "setOnAction", "handleQuitAction");
		addEventHandler(root, "aboutMenuItem", "setOnAction", "handleAboutAction");

	}

	private void addEventListeners() {

		// TODO - Listen for changes to model classes
		//PropertyChangeDispatcher.getInstance().addListener(GameClient.class, "connected", this, "onConnectionChange");

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
