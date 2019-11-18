package starter;

import java.lang.reflect.Field;

import com.beust.jcommander.JCommander;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import starter.gui.ClientStarterController;
import starter.models.CommandLineConfig;
import starter.util.WorldUtil;

public class GraphicalClientStarter extends Application {
	
	private static final CommandLineConfig config = new CommandLineConfig();
	
	@Override
	public void start(Stage stage) throws Exception {
		
		final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gui.fxml"));
		final Parent root = (Parent) loader.load();
		final ClientStarterController controller = (ClientStarterController) loader.getController();
		stage.setTitle("Graphical Client Starter");
		final Scene scene = new Scene(root);
		stage.setScene(scene);
		controller.init(stage);
		
		final String version = this.getClass().getPackage().getImplementationVersion();
		System.out.println("Graphical client starter" + (version != null ? " " + version : "") + " created");
		
		if (config.isLaunchProfile()) {
			System.out.println("Launching " + config.getLaunchProfile());
			controller.launch(config.getLaunchProfile(), config.isCloseAfterLaunch());
		}
		
		if (!config.isCloseAfterLaunch())
			stage.show();
	}
	
	public static void main(String[] args) {
		overrideDefaultFont();
		JCommander.newBuilder().addObject(config).build().parse(args);
		launch(args);
	}
	
	public static CommandLineConfig getConfig() {
		return config;
	}
	
	// lazy workaround so nodes don't display unusually on different operating systems/graphic settings
	// without explicitly setting a font for each node
	private static void overrideDefaultFont() {
		try {
			final Field field = javafx.scene.text.Font.class.getDeclaredField("defaultSystemFontSize");
			field.setAccessible(true);
			field.set(null, 12f);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

	}

}
