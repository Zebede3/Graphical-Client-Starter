package starter.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;

public class ExportUtil {

	public static final ExportMethod CSV = new ExportMethod() {
		@Override
		public String delimiter() {
			return ",";
		}

		@Override
		public String extension() {
			return "csv";
		};
	};
	
	public static final ExportMethod TSV = new ExportMethod() {
		@Override
		public String delimiter() {
			return "\t";
		}
		@Override
		public String extension() {
			return "tsv";
		}
	};
	
	public static void exportAccounts(Stage owner, List<AccountConfiguration> accounts, List<AccountColumn> columns, 
			Map<AccountColumn, TableColumn<AccountConfiguration, ?>> columnMap, ExportMethod method) {
		
		System.out.println("Exporting accounts to " + method.extension().toUpperCase() + " file");
		
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getDirectory());
		chooser.setTitle("Save Exported Accounts");
		chooser.getExtensionFilters().add(new ExtensionFilter(method.extension().toUpperCase() + " Files", "*." + method.extension()));
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
			System.out.println("Exported " + accounts.size() + " to " + method.extension().toUpperCase() + " file");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public interface ExportMethod {
		
		String delimiter();
		
		String extension();
		
	}
	
}
