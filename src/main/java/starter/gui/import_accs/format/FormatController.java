package starter.gui.import_accs.format;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import starter.util.AccountImportParser.AccountImportField;

public class FormatController implements Initializable {

	@FXML
	private TableView<AccountImportField> fields; 
	
	@FXML
	private TextField format;
	
	private Stage stage;
	private Consumer<String> onComplete;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		final TableColumn<AccountImportField, String> type = new TableColumn<>("Type");
		type.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().toString());
		});
		type.prefWidthProperty().bind(this.fields.widthProperty().divide(2));
		final TableColumn<AccountImportField, String> symbol = new TableColumn<>("Symbol");
		symbol.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().getPattern());
		});
		symbol.prefWidthProperty().bind(this.fields.widthProperty().divide(2));
		this.fields.getColumns().addAll(type, symbol);
		for (AccountImportField field : AccountImportField.values())
			this.fields.getItems().add(field);
	}
	
	public void init(Stage stage, Consumer<String> onComplete) {
		this.stage = stage;
		this.onComplete = onComplete;
	}
	
	@FXML
	public void apply() {
		this.stage.hide();
		this.onComplete.accept(this.format.getText());
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}

}
