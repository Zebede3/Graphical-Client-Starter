package starter.gui.worlds.blacklist;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import starter.models.StarterConfiguration;

public class WorldBlacklistController implements Initializable {

	@FXML
	private ListView<Integer> worlds;
	
	@FXML
	private TextField world;
	
	private SimpleObjectProperty<StarterConfiguration> settings;
	
	private Stage stage;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {}
	
	@FXML
	void addWorld() {
		this.worlds.getItems().addAll(Arrays.stream(this.world.getText().split(",")).map(s -> s.trim()).filter(s -> !s.isEmpty()).map(Integer::parseInt).toArray(Integer[]::new));
		this.world.clear();
	}
	
	@FXML
	void removeSelected() {
		this.worlds.getItems().removeAll(this.worlds.getSelectionModel().getSelectedItems());
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings) {
		this.settings = settings;
		this.worlds.getItems().setAll(settings.get().worldBlacklist());
		this.stage = stage;
	}
	
	@FXML
	void apply() {
		this.stage.hide();
		this.settings.get().worldBlacklist().setAll(this.worlds.getItems());
	}
	
	@FXML
	void cancel() {
		this.stage.hide();
	}

}
