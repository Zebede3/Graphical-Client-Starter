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
	
	private static final CommandLineConfig config = new CommandLineConfig();
	
	@Override
	public void start(Stage stage) throws Exception {
		
		final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gui.fxml"));
		final Parent root = (Parent) loader.load();
		final ClientStarterController controller = (ClientStarterController) loader.getController();
		controller.init(stage);
		stage.setTitle("Graphical Client Starter");
		stage.setScene(new Scene(root));
		
		System.out.println("Graphical client starter created");
		
		if (config.isLaunchProfile()) {
			System.out.println("Launching " + config.getLaunchProfile());
			controller.launch(config.getLaunchProfile(), config.isCloseAfterLaunch());
		}
		
		stage.show();
	}
	
	public static void main(String[] args) {
		JCommander.newBuilder().addObject(config).build().parse(args);
		launch(args);
	}
	
	public static CommandLineConfig getConfig() {
		return config;
	}

}
