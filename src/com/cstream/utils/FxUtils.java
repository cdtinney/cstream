package com.cstream.utils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Font;

public class FxUtils {

	@SuppressWarnings("unused")
	public static void showDialog(AlertType type, String title, String header, String message) {
		
		Alert alert = new Alert(type);
		alert.setHeaderText(header);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.showAndWait();
		
	}
	
	public static Label buildLabel(String text, String id, int prefWidth, int minWidth, int fontSize, Pos alignment) {
	
		Label label = new Label(text);
		label.setId(id);
		label.setAlignment(alignment);
		label.setFont(new Font("Arial", fontSize));
		label.setPrefWidth(prefWidth);
		label.setMinWidth(minWidth);
		return label;
		
	}
    
	public static Button buildButton(String text, String id, int width, int height, boolean disable) {
    	
    	Button button = new Button(text); 	
    	button.setId(id);
    	button.getStyleClass().addAll("nofocus");
    	button.setPrefWidth(width);
    	button.setPrefHeight(height);
    	button.setDisable(disable);
    	return button;
    	
    }
	
	public static void addStyleSheet(Class<?> clazz, Scene scene, String resourceName) {
		
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
	
	/** 
	 * Recursive JavaFX element lookup.
	 * @param parent	The parent element.
	 * @param id		The id associated with the child element.
	 * @return			The child element, if found. Otherwise, null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T lookup(Parent parent, String id) {
		
		if (parent == null) {
			return null;
		}
		
		String nodeId;
		
		// Special case
		if (parent instanceof MenuBar) {
			
			// Search through all MenuItems in all top level menus
			for (Menu menu : ((MenuBar) parent).getMenus()) {
				for (MenuItem item : menu.getItems()) {
					nodeId = item.getId();
					
					if (nodeId != null && nodeId.equals(id)) {
						return (T) item;
					}
				}
			}
			
		} 
		
		// All Node types
		for (Node node : parent.getChildrenUnmodifiable()) {
			nodeId = node.getId();
			
			if (nodeId != null && nodeId.equals(id)) {
				return (T) node;
			} else if (node instanceof Parent) {
				
				// The node is a parent itself - do a recursive lookup
				T child = lookup((Parent) node, id);
				
				if (child != null) {
					return child;
				}
			}
			
		}
		
		return null;
	}

}
