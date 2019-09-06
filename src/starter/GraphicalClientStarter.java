package starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import starter.gui.ClientStarterController;
import starter.util.FileUtil;

public class GraphicalClientStarter extends Application {

	private static final String RELAUNCH = "relaunch";
	
	@Override
	public void start(Stage stage) throws Exception {
		final FXMLLoader loader = new FXMLLoader(getClass().getResource("/starter/gui/gui.fxml"));
		final Parent root = (Parent) loader.load();
		final ClientStarterController controller = (ClientStarterController) loader.getController();
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
	
	public static void main(String[] args) {
		if (args.length != 0 && args[0].equals(RELAUNCH)) {
			onRelaunch(args);
			System.exit(0);
		}
		launch(args);
	}
	
	// this is a pretty unusual workaround that fixes an issue with the unofficial CLI not redirecting/consuming process io streams
	// this will likely exist until tribot releases their official CLI so that I can directly control the processes created
	// essentially this creates a sub process to relaunch this jar that will then use the unofficial CLI
	
	// this could be changed with writing the client starter jar to the users file system then launching it, but I find this better for now
	
	public static boolean launchClient(String[] args) {
		final List<String> launchCommand = generateLaunchCommand(args);
		System.out.println("Sub-process workaround launching: " + launchCommand);
		try {
			final InputStream is = new ProcessBuilder()
									.redirectInput(FileUtil.NULL_FILE)
									.redirectErrorStream(true)
									.redirectOutput(Redirect.PIPE)
									.command(launchCommand)
									.start()
									.getInputStream();
			new Thread(new StreamGobbler(is, System.out::println)).start();
			return true;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static void onRelaunch(String[] args) {
		args = Arrays.copyOfRange(args, 1, args.length);
		try {
			Class.forName("StarterNew")
			.getDeclaredMethod("main", String[].class)
			.invoke(null, new Object[] { args });
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Failed to run command, args: " + Arrays.toString(args));
		}
	}
	
	private static List<String> generateLaunchCommand(String[] args) {
		
		try {
		
			final List<String> argList = new ArrayList<>();
			
			argList.add("java");
			
			final File source = new File(GraphicalClientStarter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (source.getPath().endsWith(".jar")) {
				argList.add("-jar");
				argList.add(source.getAbsolutePath());
			}
			else 
				throw new IllegalStateException("Must be launched from jar");
			
			argList.add(RELAUNCH);
			
			for (String s : args)
				argList.add(s);
			
			return argList;
		
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static class StreamGobbler implements Runnable {

		private InputStream inputStream;
		private Consumer<String> consumeInputLine;

		public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
			this.inputStream = inputStream;
			this.consumeInputLine = consumeInputLine;
		}

		@Override
		public void run() {
			new BufferedReader(new InputStreamReader(this.inputStream)).lines().forEach(this.consumeInputLine);
			System.out.println("Sub process ended");
		}
	}

}
