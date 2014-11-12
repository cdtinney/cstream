package com.cstream;

import com.cstream.utils.FxUtils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

public class CApplicationView extends Scene {
	
	private static Label status;
	
	private BorderPane root;
	
	public CApplicationView(int width, int height) {
		super(new BorderPane(), width, height);	
		
		root = (BorderPane) getRoot();
		
		FxUtils.addStyleSheet(getClass(), this, "/css/application.css");
	}
	
	public void initialize() {
		addMenuBar();
	}
	
	public void addToBorderPane(Node node, String position) {
		
		if (position.equals("center")) {
			root.setCenter(node);
			
		} else if (position.equals("bottom")) {
			root.setBottom(node);
			
		} else if (position.equals("top")) {
			root.setTop(node);
			
		} else if (position.equals("left")) {
			root.setLeft(node);
			
		} else if (position.equals("right")) {
			root.setRight(node);
			
		}
	}
	
	private void addMenuBar() {
		
		MenuBar menuBar = new MenuBar();
		menuBar.setPrefWidth(getWidth());
		
		// Game menu
		Menu gameMenu = new Menu("cStream");
		
		MenuItem quitAppItem = new MenuItem("Quit");
		quitAppItem.setId("quitAppMenuItem");
		
		gameMenu.getItems().addAll(quitAppItem);
		
		// Help menu
		Menu helpMenu = new Menu("Help");
		
		MenuItem aboutItem = new MenuItem("About");
		aboutItem.setId("aboutMenuItem");
		
		helpMenu.getItems().addAll(aboutItem);
		
		// Add all menus to bar
		menuBar.getMenus().addAll(gameMenu, helpMenu);
		
		root.setTop(menuBar);
		
	}
}
