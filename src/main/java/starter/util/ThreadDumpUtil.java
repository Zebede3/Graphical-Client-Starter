package starter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ThreadDumpUtil {

	public static void takeThreadDump(String tribotPath, long pid, Stage stage) {
		final String path = tribotPath + File.separator + "jre" + File.separator + "bin" + File.separator + "jcmd.exe";
		try {
			final Process p = new ProcessBuilder()
			.command(path, String.valueOf(pid), "Thread.print")
			.redirectErrorStream(true)
			.redirectInput(FileUtil.NULL_FILE)
			.start();
			final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final String contents = br.lines().collect(Collectors.joining(System.lineSeparator()));
			Platform.runLater(() -> {
				final FileChooser chooser = new FileChooser();
				chooser.setInitialDirectory(FileUtil.getDirectory());
				chooser.setTitle("Save Thread Dump");
				chooser.setInitialFileName(pid + "-" + System.currentTimeMillis());
				chooser.getExtensionFilters().add(new ExtensionFilter("Thread Dump Files", "*.tdump"));
				final File save = chooser.showSaveDialog(stage);
				if (save == null) {
					return;
				}
				try {
					Files.writeString(save.toPath(), contents);
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			});
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
