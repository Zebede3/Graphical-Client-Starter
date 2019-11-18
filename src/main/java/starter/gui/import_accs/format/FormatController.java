package starter.gui.import_accs.format;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import starter.gui.TextFieldTableCell;
import starter.util.importing.AccountImportParser.AccountImportField;

public class FormatController implements Initializable {

	private final Map<AccountImportField, SimpleStringProperty> defaults = Arrays.stream(AccountImportField.values())
			.collect(Collectors.toMap(Function.identity(), v -> new SimpleStringProperty("")));
	
	@FXML
	private TableView<AccountImportField> fields; 
	
	@FXML
	private TextField format;
	
	private Stage stage;
	private BiConsumer<String, Map<AccountImportField, String>> onComplete;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.fields.setEditable(true);
		final TableColumn<AccountImportField, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().toString());
		});
		name.prefWidthProperty().bind(this.fields.widthProperty().divide(4));
		final TableColumn<AccountImportField, String> symbol = new TableColumn<>("Symbol");
		symbol.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().getSymbol());
		});
		symbol.prefWidthProperty().bind(this.fields.widthProperty().divide(4));
		final TableColumn<AccountImportField, String> type = new TableColumn<>("Type");
		type.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().getType().getSimpleName());
		});
		type.prefWidthProperty().bind(this.fields.widthProperty().divide(4));
		final TableColumn<AccountImportField, String> def = new TableColumn<>("Default");
		def.setCellValueFactory(s -> {
			return this.defaults.get(s.getValue());
		});
		def.setCellFactory(t -> {
			return new TextFieldTableCell<>();
		});
		def.setOnEditCommit(e -> this.defaults.get(e.getRowValue()).set(e.getNewValue()));
		def.prefWidthProperty().bind(this.fields.widthProperty().divide(4));
		def.setEditable(true);
		this.fields.getColumns().addAll(name, symbol, type, def);
		for (AccountImportField field : AccountImportField.values())
			this.fields.getItems().add(field);
	}
	
	public void init(Stage stage, String format, Map<AccountImportField, String> defaults,
			BiConsumer<String, Map<AccountImportField, String>> onComplete) {
		this.stage = stage;
		this.onComplete = onComplete;
		defaults.forEach((field, val) -> this.defaults.get(field).set(val));
		this.format.setText(format);
	}
	
	@FXML
	public void apply() {
		this.stage.hide();
		this.onComplete.accept(this.format.getText(), this.defaults.entrySet().stream()
				.filter(e -> !e.getValue().get().isEmpty())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get())));
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}

}
