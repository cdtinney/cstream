package ctransfer.utils;

import javafx.scene.Scene;

public class FxUtils {
	
	public static void addStyleSheet(Class clazz, Scene scene, String resourceName) {
		
		if (clazz == null || scene == null || resourceName == null || resourceName.isEmpty()) {
			// TODO - Proper logging
			return;
		}
		
		try {
			scene.getStylesheets().add(clazz.getResource(resourceName).toExternalForm());
			
		} catch (NullPointerException e) {
			System.err.println("CSS resource not found: " + resourceName);
			
		}
		
	}

}
