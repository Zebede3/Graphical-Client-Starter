package starter.gui.tribot.jar_path;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import starter.models.StarterConfiguration;

public class CustomJarController implements Initializable {

	private final SimpleObjectProperty<File> file = new SimpleObjectProperty<>();
	
	@FXML
	private CheckBox use;
	
	@FXML
	private Text text;
	
	private SimpleObjectProperty<StarterConfiguration> settings;
	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.text.textProperty().bind(Bindings.createStringBinding(() -> {
			final File f = this.file.get();
			return f != null ? f.getAbsolutePath() : "No file selected";
		}, this.file));
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings) {
		this.settings = settings;
		this.stage = stage;
		//this.use.setSelected(settings.get().isUseCustomTribotPath());
		this.file.set(new File(settings.get().getCustomTribotPath()));
	}
	
	@FXML
	public void configure() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select TRiBot Jar File");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Executable Jar Files", "*.jar"));
		final File dir = this.file.get().isDirectory() ? this.file.get() : this.file.get().getParentFile();
		if (dir != null && dir.exists())
			fileChooser.setInitialDirectory(dir);
		final File selectedFile = fileChooser.showOpenDialog(this.stage);
		if (selectedFile != null)
			this.file.set(selectedFile);
	}

	@FXML
	public void apply() {
		this.stage.hide();
		//this.settings.get().setUseCustomTribotPath(this.use.isSelected());
		this.settings.get().setCustomTribotPath(this.file.get().getAbsolutePath());
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}
	
}
