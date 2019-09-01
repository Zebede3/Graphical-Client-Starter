package starter.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import starter.gson.GsonFactory;
import starter.models.AccountColumnData;
import starter.models.AccountConfiguration;
import starter.models.ApplicationConfiguration;
import starter.models.ObservableStack;
import starter.models.ProxyDescriptor;
import starter.models.StarterConfiguration;
import starter.util.FileUtil;
import starter.util.TribotProxyGrabber;

public class GUIController implements Initializable {
	
	private static final String LAST = "last.json";
	
	private static final String SOURCE_REPO_PATH = "https://github.com/Naton1/Graphical-Client-Starter/";

	private static final AccountColumnData[] STRING_ACCOUNT_COLUMN_DATA = {
			new AccountColumnData("Account Name", "username"),
			new AccountColumnData("Script", "script"),
			new AccountColumnData("Script Arguments", "args"),
			new AccountColumnData("World", "world"),
			new AccountColumnData("Break Profile", "breakProfile"),
			new AccountColumnData("Heap Size", "heapSize")
	};
	
	private static final int NUM_EDITABLE_ACCOUNT_COLUMNS = STRING_ACCOUNT_COLUMN_DATA.length + 2;
	
	private static final KeyCodeCombination COPY_ALL_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	private static final KeyCodeCombination COPY_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	private static final KeyCodeCombination PASTE_KEY_COMBO = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
	
	private final SimpleObjectProperty<ApplicationConfiguration> config = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<StarterConfiguration> model = new SimpleObjectProperty<>();
	
	private final ObservableStack<StarterConfiguration> undo = new ObservableStack<>();
	private final ObservableStack<StarterConfiguration> redo = new ObservableStack<>();
	
	private final SimpleStringProperty lastSaveName = new SimpleStringProperty(null);
	private final SimpleBooleanProperty outdated = new SimpleBooleanProperty(false);
	
	private final ProxyDescriptor[] proxies = TribotProxyGrabber.getProxies();
	
	@FXML
	private TableView<AccountConfiguration> accounts;

	@FXML
	private Spinner<Integer> timeBetweenLaunch;
	
	@FXML
	private MenuItem save;
	
	@FXML
	private CheckMenuItem autoSaveLast;
	
	@FXML
	private ListView<String> console;
	
	private Stage stage;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		overrideDefaultFont();
		setupConsole();
		this.config.set(getApplicationConfig());
		this.config.get().runOnChange(() -> saveApplicationConfig());
		this.autoSaveLast.selectedProperty().bindBidirectional(this.config.get().autoSaveLastProperty());
		this.model.addListener((obs, old, newv) -> {
			if (old != null) {
				old.delayBetweenLaunchProperty().unbind();
				this.accounts.setItems(FXCollections.observableArrayList());
			}
			if (newv != null) {
				newv.delayBetweenLaunchProperty().bind(this.timeBetweenLaunch.getValueFactory().valueProperty());
				this.accounts.setItems(newv.getAccounts());
			}
		});
		this.save.disableProperty().bind(this.outdated.not());
		setupAccountTable();
		setupSpinner();
		this.model.set(new StarterConfiguration());
		if (this.config.get().isAutoSaveLast())
			load(LAST);
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
			this.quit();
			e.consume();
		});
	}
	
	@FXML
	public void addNewAccount() {
		cacheAccounts();
		final AccountConfiguration acc = new AccountConfiguration();
		acc.setUsername("Enter account");
		acc.setScript("Enter script");
		this.accounts.getItems().add(acc);
		updated();
	}
	
	@FXML
	public void removeSelectedAccounts() {
		final AccountConfiguration[] selected = this.accounts.getItems().stream().filter(a -> a.isSelected()).toArray(AccountConfiguration[]::new);
		if (!confirmRemoval(selected.length))
			return;
		cacheAccounts();
		this.accounts.getItems().removeAll(selected);
		updated();
	}
	
	@FXML
	public void clearAccountTable() {
		if (!confirmRemoval(this.accounts.getItems().size()))
			return;
		cacheAccounts();
		this.accounts.getItems().clear();
		updated();
	}
	
	@FXML
	public void launch() {
		final StarterConfiguration config = this.model.get().copy();
		new Thread(() -> launchClients(config)).start();
	}
	
	public void launch(String save) {
		load(save);
		launch();
	}
	
	@FXML
	public void load() {
		checkSave();
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getSettingsDirectory());
		chooser.setTitle("Load Client Starter Settings");
		chooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
		final File open = chooser.showOpenDialog(this.stage);
		if (open == null)
			return;
		load(open.getName());
	}
	
	@FXML
	public void save() {
		if (this.lastSaveName.get() == null) {
			saveAs();
			return;
		}
		save(this.lastSaveName.get());
		this.outdated.set(false);
	}
	
	@FXML
	public void saveAs() {
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getSettingsDirectory());
		chooser.setTitle("Save Client Starter Settings");
		chooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
		final File save = chooser.showSaveDialog(this.stage);
		if (save == null)
			return;
		this.lastSaveName.set(save.getName());
		save(save.getName());
		this.outdated.set(false);
	}
	
	@FXML
	public void quit() {
		if (!confirmExit())
			return;
		checkSave();
		shutdown();
	}
	
	@FXML
	public void newConfiguration() {
		checkSave();
		cacheAccounts();
		this.model.set(new StarterConfiguration());
		updated();
		this.outdated.set(false);
		this.lastSaveName.set(null);
	}
	
	@FXML
	public void viewSource() {
		final Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("View Source");
		alert.setHeaderText("Source Repository");
		Node node;
		if (Desktop.isDesktopSupported()) {
			final Hyperlink link = new Hyperlink(SOURCE_REPO_PATH);
			link.setOnAction(e -> {
				try {
					Desktop.getDesktop().browse(new URL(SOURCE_REPO_PATH).toURI());
				} 
				catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
			});
			node = link;
		}
		else {
			final TextField text = new TextField();
			text.setEditable(false);
			text.setText(SOURCE_REPO_PATH);
			node = text;
		}
		alert.getDialogPane().setContent(node);
		alert.initOwner(this.stage);
		alert.showAndWait();
	}
	
	private void setupConsole() {
		final PrintStream ps = new PrintStream(new Console(this.console), false);
		System.setOut(ps);
		System.setErr(ps);
		this.console.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		final ContextMenu cm = new ContextMenu();
		final MenuItem copy = new MenuItem("Copy Selected");
		copy.setOnAction(e -> {
			final String s = this.console.getSelectionModel().getSelectedItems().stream().collect(Collectors.joining(System.lineSeparator()));
			final ClipboardContent content = new ClipboardContent();
			content.put(DataFormat.PLAIN_TEXT, s);
			Clipboard.getSystemClipboard().setContent(content);
		});
		copy.setAccelerator(COPY_KEY_COMBO);
		final MenuItem copyAll = new MenuItem("Copy All");
		copyAll.setOnAction(e -> {
			final String s = this.console.getItems().stream().collect(Collectors.joining(System.lineSeparator()));
			final ClipboardContent content = new ClipboardContent();
			content.put(DataFormat.PLAIN_TEXT, s);
			Clipboard.getSystemClipboard().setContent(content);
		});
		copyAll.setAccelerator(COPY_ALL_KEY_COMBO);
		final MenuItem clear = new MenuItem("Clear");
		clear.setOnAction(e -> {
			this.console.getItems().clear();
		});
		cm.getItems().addAll(copy, copyAll, new SeparatorMenuItem(), clear);
		this.console.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (copy.getAccelerator().match(e)) {
				copy.fire();
				e.consume();
			}
			else if (copyAll.getAccelerator().match(e)) {
				copyAll.fire();
				e.consume();
			}
		});
		this.console.setContextMenu(cm);
	}
	
	private void shutdown() {
		this.stage.hide();
		Platform.exit();
		System.exit(0);
	}
	
	private void setupSpinner() {
		this.timeBetweenLaunch.setValueFactory(new IntegerSpinnerValueFactory(1, 1000, 30));
		this.timeBetweenLaunch.setEditable(true);
		this.timeBetweenLaunch.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue)
				this.timeBetweenLaunch.increment(0); // won't change value, but will commit editor
		});
		this.timeBetweenLaunch.valueProperty().addListener((obs, old, newv) -> {
			this.updated();
		});
	}
	
	private void setupAccountTable() {
		
		this.accounts.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (COPY_KEY_COMBO.match(e)) {
				final AccountConfiguration acc = this.accounts.getSelectionModel().getSelectedItem();
				if (acc != null)
					copyAccountToClipboard(acc);
				e.consume();
			}
			else if (PASTE_KEY_COMBO.match(e)) {
				final AccountConfiguration acc = this.accounts.getSelectionModel().getSelectedItem();
				final int index = acc != null ? this.accounts.getItems().indexOf(acc) : this.accounts.getItems().size() - 1;
				pasteAccountFromClipboard(index);
				e.consume();
			}
		});
		
		this.accounts.setEditable(true);

		this.accounts.setContextMenu(createDefaultTableContextMenu());

		this.accounts.getColumns().add(createSelectAccountTableColumn());

		for (AccountColumnData data : STRING_ACCOUNT_COLUMN_DATA)
			this.accounts.getColumns().add(createAccountTableColumn(data));

		this.accounts.getColumns().add(createUseProxyTableColumn());
		this.accounts.getColumns().add(createProxyTableColumn());
	}
	
	private TableColumn<AccountConfiguration, Boolean> createSelectAccountTableColumn() {
		
		final CheckBox selectAll = new CheckBox();
		selectAll.selectedProperty().addListener((observable, old, newValue) -> {
			cacheAccounts();
			this.accounts.getItems().forEach(acc -> acc.setSelected(newValue));
			updated();
		});

		final TableColumn<AccountConfiguration, Boolean> selected = new TableColumn<>();
		selected.setGraphic(selectAll);
		selected.setPrefWidth(30);

		selected.setSortable(false);
		selected.setEditable(true);
		selected.setCellFactory(lv -> new CheckBoxTableCell<>(index -> this.accounts.getItems().get(index).selectedProperty()));
		
		final ContextMenu cm = new ContextMenu();
		
		final Menu selectRows = new Menu("Select Rows");
		
		final MenuItem selectRowsByIndex = new MenuItem("By Index");
		selectRowsByIndex.setOnAction(e -> {
			final Set<Integer> select = promptRowsToSelectByIndex();
			if (select == null)
				return;
			this.cacheAccounts();
			select.forEach(index -> this.accounts.getItems().get(index).setSelected(true));
			this.updated();
		});
		selectRows.getItems().add(selectRowsByIndex);
		for (AccountColumnData data : STRING_ACCOUNT_COLUMN_DATA) {
			final MenuItem item = new MenuItem("By " + data.getLabel());
			item.setOnAction(e -> {
				final String val = promptRowsToSelect(data);
				if (val == null)
					return;
				this.cacheAccounts();
				this.accounts.getItems().forEach(a -> {
					if (val.equals(this.getValue(a, data.getFieldName())))
						a.setSelected(true);
				});
				this.updated();
			});
			selectRows.getItems().add(item);
		}
		
		final Menu selectRowsOnly = new Menu("Select Only Rows");
		
		final MenuItem selectRowsOnlyByIndex = new MenuItem("By Index");
		selectRowsOnlyByIndex.setOnAction(e -> {
			final Set<Integer> select = promptRowsToSelectByIndex();
			if (select == null)
				return;
			this.cacheAccounts();
			for (int i = 0; i < this.accounts.getItems().size(); i++)
				this.accounts.getItems().get(i).setSelected(select.contains(i));
			this.updated();
		});
		selectRowsOnly.getItems().add(selectRowsOnlyByIndex);
		
		for (AccountColumnData data : STRING_ACCOUNT_COLUMN_DATA) {
			final MenuItem item = new MenuItem("By " + data.getLabel());
			item.setOnAction(e -> {
				final String val = promptRowsToSelect(data);
				if (val == null)
					return;
				this.cacheAccounts();
				this.accounts.getItems().forEach(a -> {
					a.setSelected(val.equals(this.getValue(a, data.getFieldName())));
				});
				this.updated();
			});
			selectRowsOnly.getItems().add(item);
		}
		
		cm.getItems().addAll(selectRows, selectRowsOnly);
		
		selected.setContextMenu(cm);
		
		return selected;
	}
	
	private Set<Integer> promptRowsToSelectByIndex() {
		final TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Select row indexes");
		dialog.setHeaderText("Row indexes start at 0, use a comma to separate");
		dialog.setContentText("Enter row indexes (ex. 0, 2)");
		dialog.initOwner(this.stage);
		return dialog.showAndWait().map(text -> {
			return Arrays.stream(text.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(Integer::parseInt)
					.collect(Collectors.toSet());
		}).orElse(null);
	}
	
	private String promptRowsToSelect(AccountColumnData data) {
		final TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Select rows by '" + data.getLabel() + "'");
		dialog.setHeaderText("Select rows by " + data.getLabel());
		dialog.setContentText("Enter '" + data.getLabel() + "'");
		dialog.initOwner(this.stage);
		return dialog.showAndWait().orElse(null);
	}

	private <T> ContextMenu createDefaultTableContextMenu() {

		final ContextMenu cm = new ContextMenu();

		final MenuItem undo = new MenuItem("Undo");
		undo.setOnAction(e -> {
			this.undoAccounts();
		});
		undo.disableProperty().bind(Bindings.createBooleanBinding(() -> this.undo.isEmpty(), this.undo));
		undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));

		final MenuItem redo = new MenuItem("Redo");
		redo.setOnAction(e -> {
			this.redoAccounts();
		});
		redo.disableProperty().bind(Bindings.createBooleanBinding(() -> this.redo.isEmpty(), this.redo));
		redo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));

		cm.getItems().addAll(undo, redo);

		return cm;
	}
	
	private <T> ContextMenu createDefaultTableCellContextMenu(TableCell<AccountConfiguration, T> cell, TableColumn<AccountConfiguration, T> col) {
		
		final ContextMenu cm = createDefaultTableContextMenu();
		
		final MenuItem duplicate = new MenuItem("Duplicate Row");
		duplicate.setOnAction(e -> {
			final AccountConfiguration acc = this.accounts.getItems().get(cell.getIndex());
			if (acc == null)
				return;
			this.accounts.getItems().add(cell.getIndex() + 1, acc.copy());
		});
		
		final MenuItem delete = new MenuItem("Delete Row");
		delete.setOnAction(e -> {
			if (!this.confirmRemoval(1))
				return;
			this.accounts.getItems().remove(cell.getIndex());
		});
		
		delete.disableProperty().bind(cell.itemProperty().isNotNull().not());
		
		final MenuItem edit = new MenuItem("Edit Cell");
		edit.setOnAction(e -> {
			this.accounts.edit(cell.getIndex(), col);
		});
		edit.disableProperty().bind(cell.itemProperty().isNotNull().not());
		
		final MenuItem copy = new MenuItem("Copy Row");
		copy.setOnAction(e -> {
			final AccountConfiguration acc = this.accounts.getItems().get(cell.getIndex());
			if (acc == null)
				return;
			copyAccountToClipboard(acc);
		});
		copy.disableProperty().bind(cell.itemProperty().isNotNull().not());
		copy.setAccelerator(COPY_KEY_COMBO);
		
		final MenuItem paste =  new MenuItem("Paste Row");
		paste.setOnAction(e -> {
			final int index = cell.getIndex() >= this.accounts.getItems().size()
							? this.accounts.getItems().size() - 1
							: cell.getIndex();
			pasteAccountFromClipboard(index);
		});
		paste.setAccelerator(PASTE_KEY_COMBO);
		// these accelerators don't directly get triggered but the table has event handlers to handle them,
		// so they exist to notify the user of the shortcuts
		
		cm.getItems().addAll(0, Arrays.asList(edit, new SeparatorMenuItem(), copy, paste, new SeparatorMenuItem(), duplicate, delete, new SeparatorMenuItem()));

		return cm;
	}
	
	private void pasteAccountFromClipboard(int index) {
		
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		
		if (!clipboard.hasContent(DataFormat.PLAIN_TEXT))
			return;
		
		final String content = (String) clipboard.getContent(DataFormat.PLAIN_TEXT);
		if (content == null)
			return;
		
		final AccountConfiguration acc = GsonFactory.buildGson().fromJson(content, AccountConfiguration.class);
		if (acc == null)
			return;
		
		this.cacheAccounts();
		this.accounts.getItems().add(index, acc);
		this.updated();
		
	}
	
	private void copyAccountToClipboard(AccountConfiguration acc) {
		final String s = GsonFactory.buildGson().toJson(acc);
		final ClipboardContent content = new ClipboardContent();
		content.put(DataFormat.PLAIN_TEXT, s);
		Clipboard.getSystemClipboard().setContent(content);
	}
	
	private TableColumn<AccountConfiguration, Boolean> createUseProxyTableColumn() {
		final TableColumn<AccountConfiguration, Boolean> col = getBaseColumn("Use Proxy", "useProy");
		col.setCellFactory(lv -> new CheckBoxTableCell<>(index -> this.accounts.getItems().get(index).useProxyProperty()));
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set 'Use Proxy' for selected accounts");
		set.setOnAction(e -> {
			final Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle("Set Use Proxy");
			dialog.setHeaderText("Set 'Use Proxy' for selected accounts");
			final ButtonType enable = new ButtonType("Use Proxy");
			final ButtonType disable = new ButtonType("Don't Use Proxy");
			final ButtonType cancel = ButtonType.CLOSE;
			dialog.getButtonTypes().setAll(enable, disable, cancel);
			dialog.initOwner(this.stage);
			dialog.showAndWait().filter(t -> t != cancel).ifPresent(type -> {
				cacheAccounts();
				this.accounts.getItems().stream()
						.filter(AccountConfiguration::isSelected)
						.forEach(a -> a.setUseProxy(type == enable));
				updated();
				this.accounts.refresh();
			});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private TableColumn<AccountConfiguration, ProxyDescriptor> createProxyTableColumn() {
		final TableColumn<AccountConfiguration, ProxyDescriptor> col = getBaseColumn("Proxy", "proxy");
		col.setCellFactory(lv -> {
			final TableCell<AccountConfiguration, ProxyDescriptor> cell = new ComboBoxTableCell<>(this.proxies);
			cell.setContextMenu(createDefaultTableCellContextMenu(cell, col));
			return cell;
		});
		col.setOnEditCommit(e -> {
			this.cacheAccounts();
			e.getRowValue().setProxy(e.getNewValue());
			this.updated();
			this.accounts.refresh();
		});
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set 'Proxy' for selected accounts");
		set.setOnAction(e -> {
    		final ChoiceDialog<ProxyDescriptor> dialog = new ChoiceDialog<>(null, this.proxies);
    		dialog.setTitle("Set Proxy");
    		dialog.setHeaderText("Set 'Proxy' for selected accounts");
    		dialog.setContentText("Select Proxy");
    		dialog.initOwner(this.stage);
    		dialog.showAndWait().ifPresent(value -> {
    			cacheAccounts();
    			this.accounts.getItems().stream()
    				.filter(AccountConfiguration::isSelected)
    				.forEach(a -> setValue(a, "proxy", value));
    			updated();
    			this.accounts.refresh();
    		});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private <T> TableColumn<AccountConfiguration, T> getBaseColumn(String label, String fieldName) {
		final TableColumn<AccountConfiguration, T> col = new TableColumn<>(label);
		col.prefWidthProperty().bind(this.accounts.widthProperty().subtract(30).divide(NUM_EDITABLE_ACCOUNT_COLUMNS));
		col.setMinWidth(110);
		col.setEditable(true);
		col.setCellValueFactory(new PropertyValueFactory<AccountConfiguration, T>(fieldName));
		return col;
	}

	private TableColumn<AccountConfiguration, String> createAccountTableColumn(AccountColumnData data) {
		final TableColumn<AccountConfiguration, String> col = getBaseColumn(data.getLabel(), data.getFieldName());
		col.setCellFactory(s -> {
			final TableCell<AccountConfiguration, String> cell = new TextFieldTableCell<>();
			cell.setContextMenu(createDefaultTableCellContextMenu(cell, col));
			return cell;
		});
		col.setOnEditCommit(e -> {
			cacheAccounts();
			setValue(e.getRowValue(), data.getFieldName(), e.getNewValue());
			updated();
			this.accounts.refresh();
		});
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set '" + data.getLabel() + "' for selected accounts");
		set.setOnAction(e -> {
    		final TextInputDialog dialog = new TextInputDialog();
    		dialog.setTitle("Set " + data.getLabel());
    		dialog.setHeaderText("Set '" + data.getLabel() + "' for selected accounts");
    		dialog.setContentText("Enter " + data.getLabel());
    		dialog.initOwner(this.stage);
    		dialog.showAndWait().ifPresent(value -> {
    			cacheAccounts();
    			this.accounts.getItems().stream()
    				.filter(AccountConfiguration::isSelected)
    				.forEach(a -> setValue(a, data.getFieldName(), value));
    			updated();
    			this.accounts.refresh();
    		});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private void setValue(Object obj, String fieldName, Object value) {
		final String methodName = "set" + (fieldName.length() > 1
								? Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)
								: fieldName.toUpperCase());
		try {
			obj.getClass().getMethod(methodName, value.getClass()).invoke(obj, value);
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	private <T> T getValue(Object obj, String fieldName) {
		final String methodName = "get" + (fieldName.length() > 1
								? Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)
								: fieldName.toUpperCase());
		try {
			return (T) obj.getClass().getMethod(methodName).invoke(obj);
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	// Note that currently undo/redo do not take into account selecting checkbox table cells manually
	
	private void cacheAccounts() {
		synchronized (this.undo) {
			this.undo.push(this.model.get().copy());
			this.redo.clear();
		}
	}
	
	private void undoAccounts() {
		synchronized (this.undo) {
			if (this.undo.isEmpty())
				return;
			this.redo.push(this.model.get().copy());
			this.model.get().getAccounts().setAll(this.undo.pop().getAccounts());
			updated();
		}
	}
	
	private void redoAccounts() {
		synchronized (this.undo) {
			if (this.redo.isEmpty())
				return;
			this.undo.push(this.model.get().copy());
			this.model.get().getAccounts().setAll(this.redo.pop().getAccounts());
			updated();
		}
	}
	
	private void updated() {
		if (this.config.get().isAutoSaveLast())
			save(LAST);
		this.outdated.set(true);
	}
	
	private void load(String name) {
		if (name.isEmpty())
			return;
		if (!name.endsWith(".json"))
			name += ".json";
		final File file = new File(FileUtil.getSettingsDirectory().getAbsolutePath() + File.separator + name);
		if (!file.exists()) {
			System.out.println("Failed to open '" + name + "', does not exist");
			return;
		}
		try {
			final byte[] contents = Files.readAllBytes(file.toPath());
			final StarterConfiguration config = GsonFactory.buildGson().fromJson(new String(contents), StarterConfiguration.class);
			cacheAccounts();
			this.model.set(config);
			updated();
			this.outdated.set(false);
			if (!name.equals(LAST))
				this.lastSaveName.set(name);
		}
		catch (IOException | JsonSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void save(String name) {
		if (name.isEmpty())
			return;
		if (!name.endsWith(".json"))
			name += ".json";
		final String save = GsonFactory.buildGson().toJson(this.model.get());
		try {
			Files.write(new File(FileUtil.getSettingsDirectory().getAbsolutePath() + File.separator + name).toPath(), save.getBytes());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean confirmRemoval(int amount) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm Changes");
		alert.setHeaderText("This will delete " + amount + " account configuration" + (amount != 1 ? "s" : ""));
		alert.setContentText("Are you sure you want to do this?");
		alert.initOwner(this.stage);
		alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
		return alert.showAndWait().get() == ButtonType.YES;
	}
	
	private void checkSave() {
		if (this.config.get().isDontShowSaveConfirm())
			return;
		if (!this.outdated.get())
			return;
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Save Changes");
		alert.setHeaderText("Would you like to save your changes?");
		final CheckBox dontAskAgain = new CheckBox("Don't ask again");
		alert.getDialogPane().setContent(dontAskAgain);
		alert.initOwner(this.stage);
		alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
		if (alert.showAndWait().get() == ButtonType.YES)
			save();
		this.config.get().setDontShowSaveConfirm(dontAskAgain.isSelected());
	}
	
	private boolean confirmExit() {
		if (this.config.get().isDontShowExitConfirm())
			return true;
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm Exit");
		alert.setHeaderText("Do you want to exit the application?");
		alert.initOwner(this.stage);
		final CheckBox dontAskAgain = new CheckBox("Don't ask again");
		alert.getDialogPane().setContent(dontAskAgain);
		alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
		final boolean result = alert.showAndWait().get() == ButtonType.YES;
		this.config.get().setDontShowExitConfirm(dontAskAgain.isSelected());
		return result;
	}
	
	private ApplicationConfiguration getApplicationConfig() {
		final File config = FileUtil.getApplicationConfig();
		if (config.exists()) {
			try {
				final byte[] contents = Files.readAllBytes(config.toPath());
				return GsonFactory.buildGson().fromJson(new String(contents), ApplicationConfiguration.class);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new ApplicationConfiguration();
	}
	
	private void saveApplicationConfig() {
		final byte[] bytes = GsonFactory.buildGson().toJson(this.config.get()).getBytes();
		try {
			Files.write(FileUtil.getApplicationConfig().toPath(), bytes);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void launchClients(StarterConfiguration config) {
		
		final List<AccountConfiguration> selected = config.getAccounts().stream()
													.filter(AccountConfiguration::isSelected)
													.collect(Collectors.toList());
		
		System.out.println("Attempting to launch " + selected.size() + " clients");
		
		for (int i = 0; i < selected.size(); i++) {
			
			final AccountConfiguration account = selected.get(i);
			
			if (!account.isSelected())
				continue;
			
			final Map<String, String> args = new LinkedHashMap<>(); // preserve order for printing args
			
			args.put("accountName", account.getUsername());
			args.put("scriptName", account.getScript());
			
			if (!account.getWorld().isEmpty())
				args.put("world", account.getWorld());
			
			if (!account.getBreakProfile().isEmpty())
				args.put("breakProfile", account.getBreakProfile());
			
			if (!account.getArgs().isEmpty())
				args.put("scriptCommand", account.getArgs());
			
			if (account.isUseProxy() && account.getProxy() != null) {
				
				args.put("proxyIP", account.getProxy().getIp());
				args.put("proxyPort", account.getProxy().getPort() + "");

				if (account.getProxy().getUsername() != null && !account.getProxy().getUsername().isEmpty())
					args.put("proxyUsername", account.getProxy().getUsername());

				if (account.getProxy().getPassword() != null && !account.getProxy().getPassword().isEmpty())
					args.put("proxyPassword", account.getProxy().getPassword());
				
			}
			
			if (!account.getHeapSize().isEmpty())
				args.put("heapSize", account.getHeapSize());
			
			final String[] argsArray = args.entrySet().stream()
										.map(e -> e.getKey() + "~" + e.getValue())
										.toArray(String[]::new);
			
			System.out.println("Launching client: " + Arrays.toString(argsArray));
			
			try {
				Class.forName("StarterNew")
				.getDeclaredMethod("main", String[].class)
				.invoke(null, new Object[] { argsArray });
			} 
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				e.printStackTrace();
				System.out.println("Failed to run command, args: " + Arrays.toString(argsArray));
			}
			
			if (i != selected.size() - 1) {
				System.out.println("Waiting " + config.getDelayBetweenLaunch() + " seconds");
				try {
					TimeUnit.SECONDS.sleep(config.getDelayBetweenLaunch());
				} 
				catch (InterruptedException e) {}
			}
			
		}
		
		System.out.println("Launch complete");
	}
	
	// lazy workaround so nodes don't display unusually on different operating systems/graphic settings
	// without explicitly setting a font for each node
	private void overrideDefaultFont() {
		try {
			final Field field = javafx.scene.text.Font.class.getDeclaredField("defaultSystemFontSize");
			field.setAccessible(true);
			field.set(null, 12f);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

	}

}
