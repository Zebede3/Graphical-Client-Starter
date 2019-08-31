package starter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import starter.gui.GUIController;

public class GraphicalClientStarter extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		final FXMLLoader loader = new FXMLLoader(getClass().getResource("/starter/gui/gui.fxml"));
		final Parent root = (Parent) loader.load();
		final GUIController controller = (GUIController) loader.getController();
		controller.setStage(stage);
		stage.setTitle("Client Starter");
		stage.setScene(new Scene(root));
		stage.show();
		System.out.println("Graphical client starter created");
		if (super.getParameters().getRaw().size() > 0) {
			System.out.println("Launching " + super.getParameters().getRaw().get(0));
			controller.launch(super.getParameters().getRaw().get(0));
		}
	}
	
	public static void main(String[] args) throws Exception {
		launch(args);
	}

}
