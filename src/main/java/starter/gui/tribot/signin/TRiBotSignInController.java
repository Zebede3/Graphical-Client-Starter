package starter.gui.tribot.signin;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import starter.models.StarterConfiguration;

public class TRiBotSignInController implements Initializable {

	@FXML
	private CheckBox login, useSid;
	
	@FXML
	private TextField username, password, sid;
	
	private SimpleObjectProperty<StarterConfiguration> settings;
	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.username.disableProperty().bind(this.login.selectedProperty().not());
		this.password.disableProperty().bind(this.login.selectedProperty().not());
		this.sid.disableProperty().bind(this.useSid.selectedProperty().not());
	}
	
	public void init(Stage stage, SimpleObjectProperty<StarterConfiguration> settings) {
		this.settings = settings;
		this.stage = stage;
		this.login.setSelected(settings.get().isLogin());
		this.useSid.setSelected(settings.get().isSupplySid());
		this.username.setText(settings.get().getTribotUsername());
		this.password.setText(settings.get().getTribotPassword());
		this.sid.setText(settings.get().getSid());
	}

	@FXML
	public void apply() {
		this.stage.hide();
		this.settings.get().setLogin(this.login.isSelected());
		this.settings.get().setTribotUsername(this.username.getText());
		this.settings.get().setTribotPassword(this.password.getText());
		this.settings.get().setSupplySid(this.useSid.isSelected());
		this.settings.get().setSid(this.sid.getText());
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}
	
}
