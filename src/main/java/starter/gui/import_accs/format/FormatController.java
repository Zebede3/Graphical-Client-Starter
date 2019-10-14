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
import starter.util.importing.AccountImportParser.AccountImportField;

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
		final TableColumn<AccountImportField, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().toString());
		});
		name.prefWidthProperty().bind(this.fields.widthProperty().divide(3));
		final TableColumn<AccountImportField, String> symbol = new TableColumn<>("Symbol");
		symbol.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().getSymbol());
		});
		symbol.prefWidthProperty().bind(this.fields.widthProperty().divide(3));
		final TableColumn<AccountImportField, String> type = new TableColumn<>("Type");
		type.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().getType().getSimpleName());
		});
		type.prefWidthProperty().bind(this.fields.widthProperty().divide(3));
		this.fields.getColumns().addAll(name, symbol, type);
		for (AccountImportField field : AccountImportField.values())
			this.fields.getItems().add(field);
	}
	
	public void init(Stage stage, String format, Consumer<String> onComplete) {
		this.stage = stage;
		this.onComplete = onComplete;
		this.format.setText(format);
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
