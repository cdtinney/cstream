package com.cstream;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.cstream.controller.Controller;
import com.cstream.media.LibraryController;
import com.cstream.media.MediaController;
import com.cstream.model.Song;
import com.cstream.server.HttpServer;
import com.cstream.tracker.TrackerClient;
import com.cstream.tracker.TrackerPeer;
import com.cstream.util.FxUtils;
import com.cstream.util.LibraryUtils;
import com.cstream.util.OSUtils;

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
	private TrackerClient client;
	
	// Networking
	private HttpServer server;

	public void initialize(Stage stage) {
		
		this.stage = stage;

		view = new CApplicationView(WIDTH, HEIGHT);
		view.initialize();

		stage.setScene(view);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();

		client = new TrackerClient(new TrackerPeer());
		server = new HttpServer();

		libraryController.initialize();
		mediaController.initialize(libraryController, client);
		
		addViews();
		
		initLocalLibrary(); 
		client.start();

		addEventHandlers();
		
	}

	public void stop() {

		LOGGER.info("Attempting to stop the application...");
		client.stop();
		
	}

	private String showPathDialog() {

		String defaultDir = DEFAULT_BASE_DIR;
		defaultDir += OSUtils.isWindows() ? "\\cstream" : "/cstream";

		TextInputDialog dialog = new TextInputDialog(defaultDir);
		dialog.setTitle("Music Library Path");
		dialog.setHeaderText("Please enter the directory path you would like to share (default: " + defaultDir + ")");

		Optional<String> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : "";

	}

	private void initLocalLibrary() {
		
		String directory = showPathDialog();
		Map<String, Song> files = LibraryUtils.buildLocalLibrary(directory, client.getPeer().getId());
		
		client.getPeer().setFiles(files);
		
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
