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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import starter.models.StarterConfiguration;
import starter.util.FXUtil;

public class ScheduleShutdownController implements Initializable {

    @FXML
    private CheckBox scheduleClientShutdown;

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
    
    @FXML
    private CheckBox applyToActiveClients;
    
    @FXML
    private CheckBox rescheduleShutdownClients;
    
    @FXML
    private TextField rescheduleShutdownClientsMinutes;
    
    private Runnable applyToActive;
    
	private SimpleObjectProperty<StarterConfiguration> settings;
	private Stage stage;
	
	@Override
	public void initialize(URL var1, ResourceBundle var2) {
		final LocalTime time = LocalTime.now();
		FXUtil.initSpinner(this.hours, 0, 23, time.getHour());
		FXUtil.initSpinner(this.minutes, 0, 59, time.getMinute());
		FXUtil.initSpinner(this.seconds, 0, 59, time.getSecond());
		this.customDate.setValue(LocalDate.now());
		this.customDate.disableProperty().bind(this.useCustomDate.selectedProperty().not().or(this.scheduleClientShutdown.selectedProperty().not()));
		this.hours.disableProperty().bind(this.scheduleClientShutdown.selectedProperty().not());
		this.minutes.disableProperty().bind(this.scheduleClientShutdown.selectedProperty().not());
		this.seconds.disableProperty().bind(this.scheduleClientShutdown.selectedProperty().not());
		this.useCustomDate.disableProperty().bind(this.scheduleClientShutdown.selectedProperty().not());
		this.rescheduleShutdownClients.disableProperty().bind(this.scheduleClientShutdown.selectedProperty().not());
		this.rescheduleShutdownClientsMinutes.disableProperty().bind(this.scheduleClientShutdown.selectedProperty().not().or(this.rescheduleShutdownClients.selectedProperty().not()));
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings, Runnable applyToActive) {
		this.settings = settings;
		this.stage = stage;
		this.customDate.setValue(settings.get().getCustomClientShutdownDate());
		this.useCustomDate.setSelected(settings.get().isUseCustomClientShutdownDate());
		this.scheduleClientShutdown.setSelected(settings.get().isScheduleClientShutdown());
		this.hours.getValueFactory().setValue(settings.get().getClientShutdownTime().getHour());
		this.minutes.getValueFactory().setValue(settings.get().getClientShutdownTime().getMinute());
		this.seconds.getValueFactory().setValue(settings.get().getClientShutdownTime().getSecond());
		this.rescheduleShutdownClients.setSelected(settings.get().isRescheduleShutdownClients());
		this.rescheduleShutdownClientsMinutes.setText(settings.get().getRescheduleShutdownClientsMinutes() + "");
		this.applyToActive = applyToActive;
	}

	@FXML
	public void apply() {
		this.stage.hide();
		this.settings.get().setUseCustomClientShutdownDate(this.useCustomDate.isSelected());
		this.settings.get().setScheduleClientShutdown(this.scheduleClientShutdown.isSelected());
		this.settings.get().setCustomClientShutdownDate(this.customDate.getValue());
		this.settings.get().setClientShutdownTime(LocalTime.of(this.hours.getValue(), this.minutes.getValue(), this.seconds.getValue()));
		this.settings.get().setRescheduleShutdownClients(this.rescheduleShutdownClients.isSelected());
		try {
			this.settings.get().setRescheduleShutdownClientsMinutes(Integer.parseInt(this.rescheduleShutdownClientsMinutes.getText().trim()));
		}
		catch (NumberFormatException e) {}
		if (this.applyToActive != null && this.applyToActiveClients.isSelected()) {
			this.applyToActive.run();
		}
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}

}
