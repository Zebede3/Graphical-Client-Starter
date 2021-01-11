package starter.util;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.scene.Scene;
import javafx.stage.Stage;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;

public class ImportUtil {
	
	public static AccountConfiguration[] importFiles(FileFormat format, Stage stage, Consumer<Scene> bindStyle, AccountColumn[] initial) {
		final File f = PromptUtil.promptImportFile(stage, format);
		if (f == null) {
			return null;
		}
		final AccountColumn[] columns = PromptUtil.promptImportRows(stage, bindStyle, initial);
		if (columns == null || columns.length == 0) {
			return null;
		}
		try {
			return Files.readAllLines(f.toPath())
			.stream()
			.map(s -> {
				final AccountConfiguration acc = new AccountConfiguration();
				final String[] fields = s.split(format.delimiter());
				final int len = Math.min(fields.length, columns.length);
				for (int i = 0; i < len; i++) {
					columns[i].setField(acc, fields[i]);
				}
				return acc;
			})
			.filter(Objects::nonNull)
			.toArray(AccountConfiguration[]::new);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
