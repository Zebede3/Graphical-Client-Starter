package starter.gui.import_accs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import starter.gui.import_accs.format.FormatController;
import starter.models.AccountConfiguration;
import starter.models.StarterConfiguration;
import starter.util.AccountImportParser;

public class ImportController implements Initializable {

	private final SimpleObjectProperty<File> file = new SimpleObjectProperty<>(new File(""));
	private final SimpleStringProperty format = new SimpleStringProperty("");
	
	@FXML
	private Text formatText, fileText;
	
	private Stage stage;
	private SimpleObjectProperty<StarterConfiguration> settings;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.formatText.textProperty().bind(this.format);
		this.fileText.textProperty().bind(Bindings.createStringBinding(() -> this.file.get().getAbsolutePath(), this.file));
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings) {
		this.stage = stage;
		this.settings = settings;
	}
	
	@FXML
	public void configureFormat() {
		final Stage stage = new Stage();
		stage.initOwner(this.stage);
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/format.fxml"));
			final Parent root = (Parent) loader.load();
			final FormatController controller = (FormatController) loader.getController();
			controller.init(stage, this.format::set);
			stage.setTitle("Import Format");
			stage.setScene(new Scene(root));
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void configureFile() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Import File");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));
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
		final AccountConfiguration[] accs = AccountImportParser.parse(this.format.get(), this.file.get());
		this.settings.get().getAccounts().addAll(accs);
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}

}
