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
		Song song = lib.get("1234");
		if (song != null) {
			LOGGER.info("Song path is: " + song.getPath());		
			
		} else {
			LOGGER.warning("Song path not found for songId: 1234");
			
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
