package starter.gui.java_path;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import starter.models.StarterConfiguration;

public class JavaPathController implements Initializable {

	private final SimpleObjectProperty<File> file = new SimpleObjectProperty<>();
	
	@FXML
	private CheckBox use;
	
	@FXML
	private Text text;
	
	//private SimpleObjectProperty<StarterConfiguration> settings;
	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.text.textProperty().bind(Bindings.createStringBinding(() -> {
			final File f = this.file.get();
			return f != null ? f.getAbsolutePath() : "No file selected";
		}, this.file));
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings) {
		//this.settings = settings;
		this.stage = stage;
		//this.use.setSelected(settings.get().isUseCustomJavaPath());
		//this.file.set(new File(settings.get().getCustomJavaPath()));
	}
	
	@FXML
	public void configure() {
		final DirectoryChooser fileChooser = new DirectoryChooser();
		fileChooser.setTitle("Select Java Bin Directory");
		if (this.file.get().isDirectory())
			fileChooser.setInitialDirectory(this.file.get());
		final File selectedFile = fileChooser.showDialog(this.stage);
		if (selectedFile != null)
			this.file.set(selectedFile);
	}

	@FXML
	public void apply() {
		this.stage.hide();
		//this.settings.get().setUseCustomJavaPath(this.use.isSelected());
		//this.settings.get().setCustomJavaPath(this.file.get().getAbsolutePath());
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}
	
}
