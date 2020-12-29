package starter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class JcmdUtil {

	public static void takeThreadDump(String tribotPath, long pid, Stage stage) {
		final String path = tribotPath + File.separator + "jre" + File.separator + "bin" + File.separator + "jcmd";
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
	
	public static void takeHeapDump(String tribotPath, long pid, Stage stage) {
		
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getDirectory());
		chooser.setTitle("Save Heap Dump");
		chooser.setInitialFileName(pid + "-" + System.currentTimeMillis());
		chooser.getExtensionFilters().add(new ExtensionFilter("Heap Dump Files", "*.hprof"));
		final File save = chooser.showSaveDialog(stage);
		if (save == null) {
			return;
		}
		
		Scheduler.executor().submit(() -> {
			final String path = tribotPath + File.separator + "jre" + File.separator + "bin" + File.separator + "jcmd";
			final String heapDumpName = System.currentTimeMillis() + "temp";
			try {
				new ProcessBuilder()
				.command(path, String.valueOf(pid), "GC.heap_dump", heapDumpName)
				.redirectErrorStream(true)
				.redirectInput(FileUtil.NULL_FILE)
				.redirectOutput(FileUtil.NULL_FILE)
				.start()
				.onExit()
				.thenRun(() -> {
					final File source = new File(tribotPath + File.separator + "tribot-gradle-launcher" + File.separator + heapDumpName);
					// for some reason jcmd doesn't take spaces in the file path name so we have to move it there ourselves
					try {
						Files.move(source.toPath(), save.toPath());
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
				});
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void printJvms(String tribotPath, Consumer<List<String>> onComplete) {
		final String path = tribotPath + File.separator + "jre" + File.separator + "bin" + File.separator + "jcmd";
		try {
			final Process p = new ProcessBuilder()
			.command(path)
			.redirectErrorStream(true)
			.redirectInput(FileUtil.NULL_FILE)
			.start();
			final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final List<String> results = br.lines().map(s -> s.trim()).filter(s -> !s.isBlank()).collect(Collectors.toList());
			onComplete.accept(results);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
