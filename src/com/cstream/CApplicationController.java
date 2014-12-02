package com.cstream;

import java.util.logging.Logger;

import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import com.cstream.controller.Controller;
import com.cstream.media.MediaBarController;
import com.cstream.media.MediaInfoController;
import com.cstream.media.MediaLibraryController;
import com.cstream.tracker.TrackerClient;

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
	
	// TODO: Networking Instance
	private TrackerClient tracker;
	
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
	
}
