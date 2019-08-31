package starter.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.scene.control.ListView;

// Based off of https://stackoverflow.com/questions/48589410/replicating-console-functionality-with-a-listview/48589707#48589707
public class Console extends OutputStream {
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final ListView<String> output;

	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	public Console(ListView<String> output) {
		this.output = output;
	}

	private void addText() throws IOException {
		final String text = this.buffer.toString("UTF-8");
		this.buffer.reset();
		final String time = getTimeStamp();
		Platform.runLater(() -> this.output.getItems().add(time + text));
	}

	@Override
	public void write(int b) throws IOException {
		this.buffer.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int bound = off + len;
		final String lineSeparator = System.lineSeparator();
		for (int i = off; i < bound; i++) {
			if (i + lineSeparator.length() <= bound) {
				final String s = new String(Arrays.copyOfRange(b, i, i + lineSeparator.length()));
				if (s.equals(lineSeparator)) {
					this.buffer.write(b, off, i - off);
					addText();
					i += lineSeparator.length() - 1; // this assumes lineSeparator will never be empty, which should never be possible
					off = i + 1;
				}
			}
		}
		assert (off <= bound);
		this.buffer.write(b, off, bound - off);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void flush() throws IOException {
		addText();
	}

	@Override
	public void close() throws IOException {
		try {
			flush();
		}
		finally {
			this.buffer.close();
		}
	}
	
	private String getTimeStamp() {
		return "[" + formatter.format(LocalDateTime.now()) + "] ";
	}
	
}