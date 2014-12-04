package com.cstream;
	
import java.util.Map;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.cstream.model.Song;
import com.cstream.tracker.TrackerClient;
import com.cstream.utils.logging.CLogHandler;

public class CApplication extends Application {
		
	private static Logger LOGGER = Logger.getLogger(CApplication.class.getName());
	
	private CApplicationController controller = new CApplicationController();
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setResizable(false);
		primaryStage.setTitle("cStream ");
		primaryStage.getIcons().add(new Image("/images/icon.png"));
		
		controller.initialize(primaryStage);
		
		primaryStage.show();
		
		// TODO: Remove. Testing only.
		Map<String, Song> lib = TrackerClient.getLibrary();
		if (lib == null) {
			LOGGER.warning("getLibrary returned null");
			return;
		}
		
		Song song = lib.get("1");
		if (song != null) {
			LOGGER.info("Retrieved: " + song);	
			
		} else {
			LOGGER.info("Did not retrieve song: 1");
			
		}
		
		Song song2 = lib.get("2");
		if (song2 != null) {
			LOGGER.info("Retrieved: " + song2);			
			
		} else {
			LOGGER.info("Did not retrieve song: 2");
			
		}
		
	}
	
	@Override
	public void stop() {
		
		controller.stop();
		LOGGER.info("Application has stopped.");
		
	}
	
	public static void main(String[] args) {
		
		CLogHandler.setHandler(LOGGER);
		launch(args);
		
	}
}
