package com.cstream;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import com.cstream.util.FxUtils;

public class CApplicationView extends Scene {
	
	private BorderPane root;
	
	public CApplicationView(int width, int height) {
		super(new BorderPane(), width, height);	
		
		root = (BorderPane) getRoot();
		
		FxUtils.addStyleSheet(getClass(), this, "/css/application.css");
		
	}
	
	public void initialize() {
		// Nothing
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
	
}
