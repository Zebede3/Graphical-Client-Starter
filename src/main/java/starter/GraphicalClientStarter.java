package starter;

import com.beust.jcommander.JCommander;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import starter.gui.ClientStarterController;
import starter.models.CommandLineConfig;

public class GraphicalClientStarter extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
		
		final CommandLineConfig config = new CommandLineConfig();
		
		final String[] args = super.getParameters().getRaw().toArray(new String[0]);
		JCommander.newBuilder().addObject(config).build().parse(args);
		
		final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gui.fxml"));
		final Parent root = (Parent) loader.load();
		final ClientStarterController controller = (ClientStarterController) loader.getController();
		controller.setStage(stage);
		stage.setTitle("Client Starter");
		stage.setScene(new Scene(root));
		
		System.out.println("Graphical client starter created");
		
		if (config.isLaunchProfile()) {
			System.out.println("Launching " + config.getLaunchProfile());
			controller.launch(config.getLaunchProfile());
		}
		
		// problem - the launch method doesn't wait or anything
//		if (config.isCloseAfterLaunch())
//			return;
		
		stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
