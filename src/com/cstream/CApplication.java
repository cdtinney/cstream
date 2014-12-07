package com.cstream;
	
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.cstream.logging.LogHandler;

public class CApplication extends Application {
		
	private static Logger LOGGER = Logger.getLogger(CApplication.class.getName());
	
	private CApplicationController controller = new CApplicationController();
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setResizable(false);
		primaryStage.setTitle("cStream ");
		
		try {
			primaryStage.getIcons().add(new Image("/images/icon.png"));
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		controller.initialize(primaryStage);
		
		primaryStage.show();
		
	}
	
	@Override
	public void stop() {
		
		controller.stop();
		LOGGER.info("Application has stopped.");
		
	}
	
	public static void main(String[] args) {
		
		LogHandler.setHandler(LOGGER);
		launch(args);
		
	}
}
