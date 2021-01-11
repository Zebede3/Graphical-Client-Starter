package starter.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;
import starter.models.ProxyDescriptorModel;
import starter.models.ProxyManagerColumn;

public class ExportUtil {
	
	public static void exportAccounts(Stage owner, Consumer<Scene> bindStyle, List<AccountConfiguration> accounts, List<AccountColumn> defaultColumns, 
			Map<AccountColumn, TableColumn<AccountConfiguration, ?>> columnMap, FileFormat method) {
		
		System.out.println("Exporting accounts to " + method.extension().toUpperCase() + " file");
		
		final AccountColumn[] columns = PromptUtil.promptExportRows(owner, bindStyle,
				defaultColumns.toArray(new AccountColumn[0]));
		if (columns == null) {
			System.out.println("Columns not provided, aborting export");
			return;
		}
		System.out.println("Attempting to export: " + Arrays.toString(columns));
		
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getDirectory());
		chooser.setTitle("Save Exported Accounts");
		chooser.getExtensionFilters().add(new ExtensionFilter(method.description() + " Files", "*." + method.extension()));
		final File save = chooser.showSaveDialog(owner);
		if (save == null) {
			System.out.println("No file provided, aborting export");
			return;
		}
		
		String copy = "";
		boolean started = false;
		for (AccountConfiguration acc : accounts) {
			if (started) {
				copy += System.lineSeparator();
			}
			started = true;
			boolean hasStartedRow = false;
			for (AccountColumn column : columns) {
				if (!hasStartedRow)
					hasStartedRow = true;
				else
					copy += method.delimiter();
				copy += column.getCopyText(acc, columnMap.get(column));
			}
		}
		
		try {
			Files.write(save.toPath(), copy.getBytes());
			System.out.println("Exported " + accounts.size() + " accounts to " + method.extension().toUpperCase() + " file at " + save);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void exportAccountsTextAdvanced(Stage owner, Consumer<Scene> bindStyle, List<AccountConfiguration> accounts, 
			Map<AccountColumn, TableColumn<AccountConfiguration, ?>> columnMap) {
		final FileFormat fileFormat = new FileFormat() {
			@Override
			public String delimiter() {
				return "unknown";
			}
			@Override
			public String extension() {
				return "txt";
			}
			@Override
			public String description() {
				return "Text";
			};
		};
		final String format = PromptUtil.promptAdvancedExportFormat(owner, bindStyle);
		if (format == null) {
			return;
		}
		final String content = accounts
		.stream()
		.map(a -> {
			String s = format;
			for (AccountColumn col : AccountColumn.values()) {
				s = s.replace(col.getSymbol(), col.getCopyText(a, columnMap.get(col)));
			}
			return s;
		})
		.collect(Collectors.joining(System.lineSeparator()));
		final File f = PromptUtil.promptExportFile(owner, bindStyle, fileFormat);
		if (f == null) {
			return;
		}
		try {
			Files.writeString(f.toPath(), content);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void exportProxies(Stage owner, Consumer<Scene> bindStyle, List<ProxyDescriptorModel> proxies, FileFormat method) {
		
		System.out.println("Exporting proxies to " + method.extension().toUpperCase() + " file");
		
		final ProxyManagerColumn[] columns = PromptUtil.promptExportRows(owner, bindStyle);
		if (columns == null) {
			System.out.println("Columns not provided, aborting export");
			return;
		}
		System.out.println("Attempting to export: " + Arrays.toString(columns));
		
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getDirectory());
		chooser.setTitle("Save Exported Proxies");
		chooser.getExtensionFilters().add(new ExtensionFilter(method.description() + " Files", "*." + method.extension()));
		final File save = chooser.showSaveDialog(owner);
		if (save == null) {
			System.out.println("No file provided, aborting export");
			return;
		}
		
		String copy = "";
		boolean started = false;
		for (ProxyDescriptorModel proxy : proxies) {
			if (started) {
				copy += System.lineSeparator();
			}
			started = true;
			boolean hasStartedRow = false;
			for (ProxyManagerColumn column : columns) {
				if (!hasStartedRow)
					hasStartedRow = true;
				else
					copy += method.delimiter();
				copy += column.get(proxy);
			}
		}
		
		try {
			Files.write(save.toPath(), copy.getBytes());
			System.out.println("Exported " + proxies.size() + " proxies to " + method.extension().toUpperCase() + " file at " + save);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
