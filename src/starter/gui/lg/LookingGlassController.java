package starter.gui.lg;

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
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import starter.models.StarterConfiguration;

public class LookingGlassController implements Initializable {
	
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
		this.use.setSelected(settings.get().isLookingGlass());
		this.file.set(new File(settings.get().getLookingGlassPath()));
	}
	
	@FXML
	public void configure() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Looking Glass Client Path");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Executable Jar Files", "*.jar"));
		final File selectedFile = fileChooser.showOpenDialog(this.stage);
		if (selectedFile != null)
			this.file.set(selectedFile);
	}

	@FXML
	public void apply() {
		this.stage.hide();
		this.settings.get().setLookingGlass(this.use.isSelected());
		this.settings.get().setLookingGlassPath(this.file.get().getAbsolutePath());
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}

}
