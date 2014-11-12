package ctransfer;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ctransfer.utils.FxUtils;


public class CApplication extends Application {
	
	private final int WIDTH = 1280;
	private final int HEIGHT = 720;
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, WIDTH, HEIGHT);
			
			FxUtils.addStyleSheet(getClass(), scene, "application.css");
			
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
