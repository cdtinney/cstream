package com.cstream;
	
import java.util.logging.Logger;

import com.cstream.utils.logging.CLogHandler;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class CApplication extends Application {
		
	private static Logger LOGGER = Logger.getLogger(CApplication.class.getName());
	
	private CApplicationController controller = new CApplicationController();
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setResizable(false);
		primaryStage.setTitle(" cStream ");
		primaryStage.getIcons().add(new Image("/images/icon.png"));
		
		controller.initialize(primaryStage);
		
		primaryStage.show();
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
