package starter.gui.schedule;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import starter.models.StarterConfiguration;
import starter.util.FXUtil;

public class ScheduleController implements Initializable {

    @FXML
    private CheckBox scheduleLaunches;

    @FXML
    private CheckBox useCustomDate;

    @FXML
    private DatePicker customDate;

    @FXML
    private Spinner<Integer> hours;

    @FXML
    private Spinner<Integer> minutes;

    @FXML
    private Spinner<Integer> seconds;
    
	private SimpleObjectProperty<StarterConfiguration> settings;
	private Stage stage;
	
	@Override
	public void initialize(URL var1, ResourceBundle var2) {
		final LocalTime time = LocalTime.now();
		FXUtil.initSpinner(this.hours, 0, 23, time.getHour());
		FXUtil.initSpinner(this.minutes, 0, 59, time.getMinute());
		FXUtil.initSpinner(this.seconds, 0, 59, time.getSecond());
		this.customDate.setValue(LocalDate.now());
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings) {
		this.settings = settings;
		this.stage = stage;
		this.customDate.setValue(settings.get().getLaunchDate());
		this.useCustomDate.setSelected(settings.get().isUseCustomLaunchDate());
		this.scheduleLaunches.setSelected(settings.get().isScheduleLaunch());
		this.hours.getValueFactory().setValue(settings.get().getLaunchTime().getHour());
		this.minutes.getValueFactory().setValue(settings.get().getLaunchTime().getMinute());
		this.seconds.getValueFactory().setValue(settings.get().getLaunchTime().getSecond());
	}

	@FXML
	public void apply() {
		this.stage.hide();
		this.settings.get().setUseCustomLaunchDate(this.useCustomDate.isSelected());
		this.settings.get().setScheduleLaunch(this.scheduleLaunches.isSelected());
		this.settings.get().setLaunchDate(this.customDate.getValue());
		this.settings.get().setLaunchTime(LocalTime.of(this.hours.getValue(), this.minutes.getValue(), this.seconds.getValue()));
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}

}
