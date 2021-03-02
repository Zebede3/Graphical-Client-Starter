package starter.gui.import_accs;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import starter.gui.UIBuilder;
import starter.gui.import_accs.format.FormatController;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;
import starter.models.ApplicationConfiguration;
import starter.util.AccountImportParser;
import starter.util.AccountImportParser.AccountImportField;
import starter.util.AccountImportParser.AccountImportResult;

public class ImportController implements Initializable {
	
	private static final String DEFAULT_FORMAT = AccountImportField.USERNAME.getSymbol() + ":" + AccountImportField.PASSWORD.getSymbol();

	private final SimpleObjectProperty<File> file = new SimpleObjectProperty<>(new File(""));
	private final SimpleStringProperty format = new SimpleStringProperty(DEFAULT_FORMAT);
	
	@FXML
	private Text formatText, fileText;
	
	private Stage stage;
	private BiConsumer<AccountConfiguration[], AccountColumn[]> onComplete;
	private ApplicationConfiguration config;
	
	private Map<AccountImportField, String> defaults = new HashMap<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.formatText.textProperty().bind(this.format);
		this.fileText.textProperty().bind(Bindings.createStringBinding(() -> this.file.get().getAbsolutePath(), this.file));
	}
	
	public void init(Stage stage, BiConsumer<AccountConfiguration[], AccountColumn[]> onComplete,
			ApplicationConfiguration config) {
		this.stage = stage;
		this.onComplete = onComplete;
		this.config = config;
		this.format.set(config.getLastImportPattern());
		final File file = new File(config.getLastImportFile());
		if (file.exists() && file.getParent() != null && file.getParentFile().isDirectory())
			this.file.set(file);
		if (config.getLastImportDefaults() != null) {
			this.defaults.putAll(config.getLastImportDefaults());
		}
	}
	
	@FXML
	public void configureFormat() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/format.fxml")
		.withWindowName("Import Accounts")
		.withApplicationConfig(this.config)
		.<FormatController>onCreation((stage, controller) -> {
			controller.init(stage, this.format.get(), this.defaults, (format, defaults) -> {
				this.format.set(format);
				this.defaults = defaults;
			});
		})
		.build();
	}
	
	@FXML
	public void configureFile() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Import File");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", "*.*"));
		final File parent = this.file.get().getParentFile();
		if (parent != null && parent.exists())
			fileChooser.setInitialDirectory(this.file.get().getParentFile());
		final File selectedFile = fileChooser.showOpenDialog(this.stage);
		if (selectedFile != null)
			this.file.set(selectedFile);
	}
	
	@FXML
	public void apply() {
		this.stage.hide();
		final AccountImportResult res = AccountImportParser.parse(this.format.get(), this.file.get(), this.defaults);
		if (res != null) {
			this.onComplete.accept(res.getAccounts(), res.getColumns());
		}
		this.config.setLastImportDefaults(this.defaults);
		this.config.setLastImportFile(this.file.get().getAbsolutePath());
		this.config.setLastImportPattern(this.format.get());
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}

}
