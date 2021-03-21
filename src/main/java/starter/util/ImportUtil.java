package starter.util;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;

public class ImportUtil {
	
	public static AccountConfiguration[] importFiles(FileFormat format, Stage stage, Consumer<Scene> bindStyle, AccountColumn[] columns) {
		final File f = PromptUtil.promptImportFile(stage, format);
		if (f == null) {
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
	
	public static void merge(AccountConfiguration source, AccountConfiguration add, 
			Map<AccountColumn, TableColumn<AccountConfiguration, ?>> map, AccountColumn... columns) {
		for (AccountColumn c : columns) {
			c.setField(source, c.getCopyText(add, map.get(c)));
		}
	}
	
	public static void merge(List<AccountConfiguration> source, List<AccountConfiguration> add, ImportStrategy action, 
			Map<AccountColumn, TableColumn<AccountConfiguration, ?>> map, AccountColumn... columns) {
		System.out.println("Importing " + add.size() + " accounts with import strategy: " + action);
		switch (action) {
		case CREATE_NEW:
			source.addAll(add);
			break;
		case ONLY_MERGE_LOGIN_NAME:
		case MERGE_LOGIN_NAME:
			add.forEach(a -> {
				final AccountConfiguration acc = source.stream().filter(s -> s.getUsername().equals(a.getUsername())).findFirst().orElse(null);
				if (acc != null) {
					merge(acc, a, map, columns);
				}
				else if (action != ImportStrategy.ONLY_MERGE_LOGIN_NAME) {
					source.add(a);
				}
			});
			break;
		case MERGE_ROW_INDEX_SELECTED:
		case ONLY_MERGE_ROW_INDEX_SELECTED:
			final List<AccountConfiguration> accs = source.stream().filter(AccountConfiguration::isSelected).collect(Collectors.toList());
			for (int i = 0; i < add.size(); i++) {
				if (accs.size() > i) {
					merge(accs.get(i), add.get(i), map, columns);
				}
				else if (action != ImportStrategy.ONLY_MERGE_ROW_INDEX_SELECTED) {
					source.add(add.get(i));
				}
			}
			break;
		case ONLY_MERGE_ROW_INDEX:
		case MERGE_ROW_INDEX:
			for (int i = 0; i < add.size(); i++) {
				if (source.size() > i) {
					merge(source.get(i), add.get(i), map, columns);
				}
				else if (action != ImportStrategy.ONLY_MERGE_ROW_INDEX) {
					source.add(add.get(i));
				}
			}
			break;
		}
	}

}
