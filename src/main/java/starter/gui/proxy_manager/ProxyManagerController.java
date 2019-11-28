package starter.gui.proxy_manager;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import starter.gui.TextFieldTableCell;
import starter.models.ApplicationConfiguration;
import starter.models.ProxyDescriptor;
import starter.models.ProxyDescriptorModel;
import starter.models.ProxyManagerColumn;
import starter.util.FXUtil;

public class ProxyManagerController implements Initializable {

	@FXML
	private TableView<ProxyDescriptorModel> table;
	
	@FXML
	private CheckMenuItem includeTribotProxies;
	
	private ApplicationConfiguration config;
	private Stage stage;
	private ProxyDescriptorModel[] tribot;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		createTable();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createTable() {
		
		this.table.setEditable(true);
		
		final List<TableColumn<ProxyDescriptorModel, String>> columns = new ArrayList<>();
		
		for (ProxyManagerColumn col : ProxyManagerColumn.values()) {
			final TableColumn<ProxyDescriptorModel, String> c = new TableColumn<>(col.getLabel());
			c.setCellValueFactory(s -> {
				return new SimpleStringProperty(col.get(s.getValue()));
			});
			c.setCellFactory(t -> {
				final TextFieldTableCell<ProxyDescriptorModel> cell = new TextFieldTableCell<>();
				cell.getTextField().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
					if (e.getCode() == KeyCode.TAB) {
						e.consume();
						if (cell.isEditing()) {
							cell.commitEdit(cell.getTextField().getText());
							this.selectNextCell(false);
						}
					}
				});
				return cell;
			});
			c.prefWidthProperty().bind(this.table.widthProperty().divide(5));
			c.setEditable(true);
			c.setOnEditCommit(e -> {
				col.set(e.getRowValue(), e.getNewValue());
				checkAddNew();
			});
			columns.add(c);
		}
		
		this.table.setRowFactory(t -> {
			final TableRow<ProxyDescriptorModel> row = new TableRow<>();
			row.editableProperty().bind(Bindings.createBooleanBinding(() -> {
				final ProxyDescriptorModel model = row.getItem();
				return model != null && model.editableProperty().get();
			}, row.itemProperty()));
			row.itemProperty().addListener((obs, old, newv) -> {
				row.styleProperty().unbind();
				if (newv != null) {
					row.styleProperty().bind(Bindings.createStringBinding(() -> {
								if (newv.editableProperty().get())
									return "";
								final Color color = Color.LIGHTSKYBLUE;
								return "-fx-background-color : " + FXUtil.colorToCssRgb(color);
							}, newv.editableProperty()));
				}
				else
					row.setStyle("");
			});
			return row;
		});
		
		this.table.getColumns().addAll(columns);
		
		this.includeTribotProxies.selectedProperty().addListener((obs, old, newv) -> {
			if (this.tribot == null)
				return;
			removeEmpty();
			if (newv)
				this.table.getItems().addAll(0, Arrays.asList(this.tribot));
			else
				this.table.getItems().removeAll(this.tribot);
			checkAddNew();
		});
		
		this.table.getSelectionModel().setCellSelectionEnabled(true);
		
		this.table.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
		    switch (keyEvent.getCode()){
			case TAB: {
				keyEvent.consume();
				this.table.requestFocus();
				selectNextCell(keyEvent.isShiftDown());
				break;
			}
			default:
				break;
		    }
		});
		
		this.table.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode().isDigitKey() || e.getCode().isLetterKey()) {
				final TablePosition pos = this.table.getFocusModel().getFocusedCell();
				this.table.edit(pos.getRow(), pos.getTableColumn());
			}
		});
		
		final ContextMenu cm = new ContextMenu();
		final MenuItem add = new MenuItem("Add new row");
		add.setOnAction(e -> this.table.getItems().add(new ProxyDescriptorModel()));
		cm.getItems().add(add);
		final MenuItem del = new MenuItem("Remove row");
		del.setOnAction(e -> {
			final ProxyDescriptorModel model = this.table.getSelectionModel().getSelectedItem();
			if (model == null)
				return;
			if (!model.editableProperty().get())
				return;
			this.table.getItems().remove(model);
		});
		cm.getItems().add(del);
		final MenuItem clear = new MenuItem("Clear table");
		clear.setOnAction(e -> {
			this.table.getItems().removeIf(p -> p.editableProperty().get());
		});
		cm.getItems().add(clear);
		this.table.setContextMenu(cm);
		
		checkAddNew();
	}
	
	public void init(Stage stage, ApplicationConfiguration config, ProxyDescriptor[] tribot) {
		this.config = config;
		this.stage = stage;
		this.table.getItems().setAll(config.proxies().stream().map(c -> new ProxyDescriptorModel(c, true)).toArray(ProxyDescriptorModel[]::new));
		this.tribot = Arrays.stream(tribot).map(c -> new ProxyDescriptorModel(c, false)).toArray(ProxyDescriptorModel[]::new);
		this.includeTribotProxies.setSelected(config.isIncludeTribotProxies());
		checkAddNew();
	}

	@FXML
	public void apply() {
		this.stage.hide();
		this.config.setIncludeTribotProxies(this.includeTribotProxies.isSelected());
		// only take editable ones - tribot proxies are added in otherwise
		this.config.proxies().setAll(this.table.getItems().stream().filter(p -> p.editableProperty().get()).filter(p -> !isEmpty(p)).map(ProxyDescriptorModel::toDescriptor).toArray(ProxyDescriptor[]::new));	
	}
	
	@FXML
	public void cancel() {
		this.stage.hide();
	}
	
	@FXML
	public void importAccounts() {
		
	}
	
	private void checkAddNew() {
		if (this.table.getItems().size() == 0) {
			addNew();
			return;
		}
		final ProxyDescriptorModel model = this.table.getItems().get(this.table.getItems().size() - 1);
		if (!isEmpty(model))
			addNew();
	}
	
	private void removeEmpty() {
		this.table.getItems().removeIf(this::isEmpty);
	}
	
	private void addNew() {
		this.table.getItems().add(new ProxyDescriptorModel());
	}
	
	private boolean isEmpty(ProxyDescriptorModel model) {
		return model.getIp().trim().isEmpty()
				&& model.getName().trim().isEmpty()
				&& model.getPassword().trim().isEmpty()
				&& model.getPort().trim().isEmpty()
				&& model.getUsername().trim().isEmpty();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void selectNextCell(boolean backwards) {
		final List<TablePosition> selected = this.table.getSelectionModel().getSelectedCells();
		if (backwards) {
			this.table.getSelectionModel().selectPrevious();
		} 
		else {
			this.table.getSelectionModel().selectNext();
		}
		for (TablePosition pos : selected)
			this.table.getSelectionModel().clearSelection(pos.getRow(), pos.getTableColumn());
	}
	
}
