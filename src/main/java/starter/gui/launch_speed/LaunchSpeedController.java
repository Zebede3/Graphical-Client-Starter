package starter.gui.launch_speed;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.stage.Stage;
import starter.models.StarterConfiguration;

public class LaunchSpeedController implements Initializable {

	@FXML
	private Spinner<Integer> timeBetweenLaunch;
	
	private SimpleObjectProperty<StarterConfiguration> settings;
	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.timeBetweenLaunch.setValueFactory(new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 30));
		this.timeBetweenLaunch.setEditable(true);
		this.timeBetweenLaunch.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue)
				this.timeBetweenLaunch.increment(0);
		});
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings) {
		this.settings = settings;
		this.stage = stage;
		this.timeBetweenLaunch.getValueFactory().setValue(settings.get().getDelayBetweenLaunch());
	}

	@FXML
	public void apply() {
		this.stage.hide();
		this.timeBetweenLaunch.increment(0);
		this.settings.get().setDelayBetweenLaunch(this.timeBetweenLaunch.getValue());
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}
	
}
