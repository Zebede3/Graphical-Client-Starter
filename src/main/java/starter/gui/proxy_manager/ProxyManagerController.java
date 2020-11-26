package starter.gui.proxy_manager;
	
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import starter.gui.TextFieldTableCell;
import starter.models.ApplicationConfiguration;
import starter.models.ProxyDescriptor;
import starter.models.ProxyDescriptorModel;
import starter.models.ProxyManagerColumn;
import starter.util.FXUtil;
import starter.util.FileUtil;

public class ProxyManagerController implements Initializable {
	
	@FXML
	private TableView<ProxyDescriptorModel> table;
	
	@FXML
	private CheckMenuItem includeTribotProxies;
	
	private ApplicationConfiguration config;
	private Stage stage;
	private ProxyDescriptorModel[] tribot;
	private ExecutorService exec;
	
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
				final TextFieldTableCell<ProxyDescriptorModel> cell = new TextFieldTableCell<ProxyDescriptorModel>();
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
		                if (newv.hasBeenChecked()) {
		                	if (newv.isWorking()) {
		                		return "-fx-background-color:lightgreen";
		                	}
		                	else {
		                		return "-fx-background-color:salmon";
		                	}
		                }
						if (newv.editableProperty().get()) {
							return "";
						}
						final Color color = Color.LIGHTSKYBLUE;
						return "-fx-background-color : " + FXUtil.colorToCssRgb(color);
					}, newv.editableProperty(), newv.workingProperty()));
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
	
	public void init(Stage stage, ApplicationConfiguration config, ProxyDescriptor[] tribot, ExecutorService exec) {
		this.config = config;
		this.stage = stage;
		this.table.getItems().setAll(config.proxies().stream().map(c -> new ProxyDescriptorModel(c, true)).toArray(ProxyDescriptorModel[]::new));
		this.tribot = Arrays.stream(tribot).map(c -> new ProxyDescriptorModel(c, false)).toArray(ProxyDescriptorModel[]::new);
		this.includeTribotProxies.setSelected(config.isIncludeTribotProxies());
		this.exec = exec;
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
	public void importProxies() {
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getDirectory());
		chooser.setTitle("Load Import File");
		chooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"),
				new ExtensionFilter("All Files", "*.*"));
		final File save = chooser.showOpenDialog(this.stage);
		if (save == null) {
			return;
		}
		final ProxyDescriptorModel model = this.table.getItems().get(this.table.getItems().size() - 1);
		if (isEmpty(model)) {
			this.table.getItems().remove(this.table.getItems().size() - 1);
		}
		try {
			Files.readAllLines(save.toPath())
			.stream()
			.map(s -> {
				final String[] split = s.split(":");
				try {
					switch (split.length) {
					case 2:
						return new ProxyDescriptor("", split[0], Integer.parseInt(split[1].trim()), "", "");
					case 3:
						return new ProxyDescriptor(split[0], split[1], Integer.parseInt(split[2].trim()), "", "");
					case 4:
						return new ProxyDescriptor("", split[0], Integer.parseInt(split[1].trim()), split[2], split[3]);
					case 5:
						return new ProxyDescriptor(split[0], split[1], Integer.parseInt(split[2].trim()), split[3], split[4]);
					default:
						return null;
					}
				}
				catch (NumberFormatException e) {
					e.printStackTrace();
					return null;
				}
			})
			.filter(Objects::nonNull)
			.map(p -> new ProxyDescriptorModel(p, true))
			.forEach(o -> this.table.getItems().add(o));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		checkAddNew();
	}
	
	@FXML
	public void importProxiesHelp() {
		final Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Import Proxies Information");
		alert.setHeaderText(null);
		alert.setContentText("The import file can be in any of the following forms:\n\n" + "IP:Port\n" + "Name:IP:Port\n" + "IP:Port:Username:Password\n" + "Name:IP:Port:Username:Password");
		alert.initOwner(this.stage);
		alert.showAndWait();
	}
	
	@FXML
	public void checkProxies() {
		final ProxyDescriptorModel[] models = this.table.getItems().toArray(ProxyDescriptorModel[]::new);
		this.exec.submit(() -> {
			Arrays.stream(models)
			.forEach(m -> {
				try {
					synchronized (this) {
						Authenticator.setDefault(new Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(m.getUsername(), m.getPassword().toCharArray());
							}
						});
						final HttpURLConnection con = (HttpURLConnection) new URL("https://oldschool.runescape.com/").openConnection(m.toDescriptor().toProxy());
						con.setReadTimeout(15000);
						con.setConnectTimeout(15000);
						con.setRequestMethod("GET");
						updateProxyState(m, con.getResponseCode() == HttpURLConnection.HTTP_OK);
					}
				} 
				catch (IOException e) {
					updateProxyState(m, false);
				}
				finally {
					synchronized (this) {
						Authenticator.setDefault(null);
					}
				}
			});
		});
	}
	
	@FXML
	public void resetChecked() {
		this.table.getItems().forEach(m -> {
			m.resetChecked();
		});
	}
	
	private void updateProxyState(ProxyDescriptorModel proxy, boolean working) {
		System.out.println("Tested proxy: " + proxy.toDescriptor() + "; working: " + working);
		Platform.runLater(() -> {
			proxy.setWorking(working);
		});
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
