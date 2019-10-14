package starter.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
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
import javafx.scene.control.TableRow;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import starter.GraphicalClientStarter;
import starter.gson.GsonFactory;
import starter.gui.import_accs.ImportController;
import starter.gui.java_path.JavaPathController;
import starter.gui.lg.LookingGlassController;
import starter.gui.tribot.jar_path.CustomJarController;
import starter.gui.tribot.signin.TRiBotSignInController;
import starter.models.AccountColumn;
import starter.models.AccountColumnData;
import starter.models.AccountConfiguration;
import starter.models.ApplicationConfiguration;
import starter.models.CommandLineConfig;
import starter.models.ObservableStack;
import starter.models.PendingLaunch;
import starter.models.ProxyColumnData;
import starter.models.ProxyDescriptor;
import starter.models.StarterConfiguration;
import starter.util.FileUtil;
import starter.util.NodeUtil;
import starter.util.ReflectionUtil;
import starter.util.TribotProxyGrabber;

public class ClientStarterController implements Initializable {

	private static final String LAST = "last.json";
	
	private static final String SOURCE_REPO_PATH = "https://github.com/Naton1/Graphical-Client-Starter/";
	
	private static final ProxyDescriptor BASE_PROXY = new ProxyDescriptor("", "", 0, "", "");

	// TODO maybe put into enum
	private static final AccountColumnData[] STRING_ACCOUNT_COLUMN_DATA = {
			new AccountColumnData("Account Name", "username", AccountColumn.NAME),
			new AccountColumnData("Password", "password", AccountColumn.PASSWORD),
			new AccountColumnData("Bank Pin", "pin", AccountColumn.PIN),
			new AccountColumnData("Script", "script", AccountColumn.SCRIPT),
			new AccountColumnData("Script Arguments", "args", AccountColumn.ARGS),
			new AccountColumnData("World", "world", AccountColumn.WORLD),
			new AccountColumnData("Break Profile", "breakProfile", AccountColumn.BREAK_PROFILE),
			new AccountColumnData("Heap Size", "heapSize", AccountColumn.HEAP_SIZE)
	};
	
	// TODO put into enum
	private static final ProxyColumnData[] PROXY_COLUMN_DATA = {
			new ProxyColumnData("Proxy IP",
				ProxyDescriptor::getIp,
				(newIp, oldProxy) -> {
					// let the user delete the proxy
					if (newIp.isEmpty()
							&& oldProxy.getPort() == 0
							&& oldProxy.getUsername().isEmpty()
							&& oldProxy.getPassword().isEmpty())
						return null;
					return new ProxyDescriptor(oldProxy.getName(), newIp, oldProxy.getPort(), oldProxy.getUsername(), oldProxy.getPassword());
				},
				AccountColumn.PROXY_IP),
			new ProxyColumnData("Proxy Port",
				p -> Integer.toString(p.getPort()),
				(newPort, oldProxy) -> {
					// let the user delete the proxy
					if (newPort.isEmpty()
							&& oldProxy.getIp().isEmpty()
							&& oldProxy.getUsername().isEmpty()
							&& oldProxy.getPassword().isEmpty())
						return null;
					final int port = newPort.isEmpty() ? 0 : Integer.parseInt(newPort.trim());
					return new ProxyDescriptor(oldProxy.getName(), oldProxy.getIp(), port, oldProxy.getUsername(), oldProxy.getPassword());
				},
				AccountColumn.PROXY_PORT),
			new ProxyColumnData("Proxy Username",
				ProxyDescriptor::getUsername,
				(newUser, oldProxy) -> {
					// let the user delete the proxy
					if (newUser.isEmpty()
							&& oldProxy.getPort() == 0
							&& oldProxy.getIp().isEmpty()
							&& oldProxy.getPassword().isEmpty())
						return null;
					return new ProxyDescriptor(oldProxy.getName(), oldProxy.getIp(), oldProxy.getPort(), newUser, oldProxy.getPassword());
				},
				AccountColumn.PROXY_USER),
			new ProxyColumnData("Proxy Password",
				ProxyDescriptor::getPassword,
				(newPass, oldProxy) -> {
					// let the user delete the proxy
					if (newPass.isEmpty()
							&& oldProxy.getPort() == 0
							&& oldProxy.getIp().isEmpty()
							&& oldProxy.getUsername().isEmpty())
						return null;
					return new ProxyDescriptor(oldProxy.getName(), oldProxy.getIp(), oldProxy.getPort(), oldProxy.getUsername(), newPass);
				},
				AccountColumn.PROXY_PASS)
	};
	
	private static final KeyCodeCombination COPY_ALL_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	private static final KeyCodeCombination COPY_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	private static final KeyCodeCombination PASTE_KEY_COMBO = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
	private static final KeyCodeCombination DUPLICATE_KEY_COMBO = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
	
	private final SimpleObjectProperty<ApplicationConfiguration> config = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<StarterConfiguration> model = new SimpleObjectProperty<>();
	
	private final ObservableStack<StarterConfiguration> undo = new ObservableStack<>();
	private final ObservableStack<StarterConfiguration> redo = new ObservableStack<>();
	
	private final SimpleStringProperty lastSaveName = new SimpleStringProperty(null);
	private final SimpleBooleanProperty outdated = new SimpleBooleanProperty(false);
	
	private final ProxyDescriptor[] proxies = TribotProxyGrabber.getProxies();
	
	private final Map<AccountColumn, TableColumn<AccountConfiguration, ?>> columns = new HashMap<>();
	private final Map<AccountColumn, CheckMenuItem> columnItems = new HashMap<>();
	
	private final ChangeListener<Object> updateListener = (obs, old, newv) -> {
		this.updated();
	};
	
	private LongBinding columnCount; // set after initializing column menu items
	
	private LaunchProcessor launcher;
	
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
	
	@FXML
	private ListView<PendingLaunch> launchQueue;
	
	@FXML
	private Menu columnSelection;
	
	private Stage stage;
	
	private ObjectProperty<Integer> delayBetweenLaunchProperty; // have to store a reference so we can unbind the bidirectional binding
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		overrideDefaultFont();
		setupConsole();
		this.config.set(getApplicationConfig());
		this.config.get().runOnChange(() -> saveApplicationConfig());
		this.autoSaveLast.selectedProperty().bindBidirectional(this.config.get().autoSaveLastProperty());
		this.save.disableProperty().bind(this.outdated.not());
		setupColumnSelection();
		setupAccountTable();
		setupSpinner();
		this.model.addListener((obs, old, newv) -> {
			if (old != null) {
				this.accounts.setItems(FXCollections.observableArrayList());
				for (AccountColumn col : AccountColumn.values()) {
					final SimpleBooleanProperty prop = newv.displayColumnProperty(col);
					final CheckMenuItem item = this.columnItems.get(col);
					item.selectedProperty().unbindBidirectional(prop);
				}
				removeMiscUpdateListeners(old);
			}
			if (this.delayBetweenLaunchProperty != null) {
				this.timeBetweenLaunch.getValueFactory().valueProperty().unbindBidirectional(this.delayBetweenLaunchProperty);
				this.delayBetweenLaunchProperty = null;
			}
			if (newv != null) {
				this.delayBetweenLaunchProperty = newv.delayBetweenLaunchProperty().asObject();
				this.timeBetweenLaunch.getValueFactory().valueProperty().bindBidirectional(this.delayBetweenLaunchProperty);
				this.accounts.setItems(newv.getAccounts());
				for (AccountColumn col : AccountColumn.values()) {
					final SimpleBooleanProperty prop = newv.displayColumnProperty(col);
					final CheckMenuItem item = this.columnItems.get(col);
					item.selectedProperty().bindBidirectional(prop);
				}
				addMiscUpdateListeners(newv);
			}
		});
		this.model.set(new StarterConfiguration());
		setupLaunchQueue();
		if (this.config.get().isAutoSaveLast())
			load(LAST);
	}
	
	public void init(Stage stage) {
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
		this.launcher.launchClients(config);
	}
	
	public void launch(String save, boolean closeAfter) {
		load(save);
		launch();
		if (closeAfter) {
			new Thread(() -> {
				while (this.launcher.hasRemainingLaunches()) {
					try {
						Thread.sleep(1000);
					} 
					catch (InterruptedException e) {}
				}
				System.exit(0);
			}).start();
		}
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
		load(open.getAbsolutePath());
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
		if (this.timeBetweenLaunch.isFocused())
			this.timeBetweenLaunch.getValueFactory().increment(0); // commit change
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
	
	@FXML
	public void colorSelectedAccounts() {
		final Dialog<Color> dialog = new Dialog<>();
		dialog.setTitle("Set Row Color");
		dialog.setHeaderText("Set 'Row Color' for selected accounts");
		dialog.setContentText("Select Color");
		final ColorPicker color = new ColorPicker(Color.WHITE);
		final HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(color);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(this.stage);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK)
		        return color.getValue();
		    return null;
		});
		dialog.showAndWait().ifPresent(c -> {
			this.cacheAccounts();
			this.accounts.getItems().filtered(AccountConfiguration::isSelected).forEach(a -> {
				a.setColor(c);
			});
			this.updated();
		});
	}
	
	@FXML
	public void importFromSettingsFile() {
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getSettingsDirectory());
		chooser.setTitle("Import Accounts - Settings File");
		chooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
		final File open = chooser.showOpenDialog(this.stage);
		if (open == null)
			return;
		final StarterConfiguration settings = readSettingsFile(open.getAbsolutePath());
		if (settings == null)
			return;
		this.cacheAccounts();
		this.accounts.getItems().addAll(settings.getAccounts());
		this.updated();
	}
	
	@FXML
	public void importFromTextFile() {
		final Stage stage = new Stage();
		stage.initOwner(this.stage);
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/import.fxml"));
			final Parent root = (Parent) loader.load();
			final ImportController controller = (ImportController) loader.getController();
			controller.init(stage, accs -> {
				this.cacheAccounts();
				this.accounts.getItems().addAll(accs);
				this.updated();
			});
			stage.setTitle("Import Accounts");
			stage.setScene(new Scene(root));
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void resetSelectedColors() {
		this.cacheAccounts();
		this.accounts.getItems().filtered(AccountConfiguration::isSelected).forEach(a -> {
			a.setColor(null);
		});
		this.updated();
	}
	
	@FXML
	public void configureLookingGlass() {
		final Stage stage = new Stage();
		stage.initOwner(this.stage);
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lg.fxml"));
			final Parent root = (Parent) loader.load();
			final LookingGlassController controller = (LookingGlassController) loader.getController();
			controller.init(stage, this.model);
			stage.setTitle("Looking Glass Configuration");
			stage.setScene(new Scene(root));
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void configureJavaPath() {
		final Stage stage = new Stage();
		stage.initOwner(this.stage);
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/java_path.fxml"));
			final Parent root = (Parent) loader.load();
			final JavaPathController controller = (JavaPathController) loader.getController();
			controller.init(stage, this.model);
			stage.setTitle("Custom Java Path");
			stage.setScene(new Scene(root));
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void displayTribotJar() {
		final Stage stage = new Stage();
		stage.initOwner(this.stage);
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/custom_jar.fxml"));
			final Parent root = (Parent) loader.load();
			final CustomJarController controller = (CustomJarController) loader.getController();
			controller.init(stage, this.model);
			stage.setTitle("Custom TRiBot Jar Configuration");
			stage.setScene(new Scene(root));
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void displayTribotSignin() {
		final Stage stage = new Stage();
		stage.initOwner(this.stage);
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signin.fxml"));
			final Parent root = (Parent) loader.load();
			final TRiBotSignInController controller = (TRiBotSignInController) loader.getController();
			controller.init(stage, this.model);
			stage.setTitle("TRiBot Sign-in Configuration");
			stage.setScene(new Scene(root));
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final Comparator<AccountConfiguration> COLOR_COMPARATOR = 
			Comparator.<AccountConfiguration>comparingInt(c -> {
				final Color col = c.getColor();
				if (col == null)
					return Integer.MIN_VALUE;
				int r = ((int) col.getRed() * 255);
				int g = ((int) col.getGreen() * 255);
				int b = ((int) col.getBlue() * 255);
				return (r << 16) + (g << 8) + b;
			});
	
	@FXML
	public void sortByColorAsc() {
		this.cacheAccounts();
		this.accounts.getItems().sort(COLOR_COMPARATOR);
		this.updated();
	}
	
	@FXML
	public void sortByColorDesc() {
		this.cacheAccounts();
		this.accounts.getItems().sort(COLOR_COMPARATOR.reversed());
		this.updated();
	}
	
	private void addMiscUpdateListeners(StarterConfiguration config) {
		for (ObservableValue<?> obs : extractMiscObservables(config))
			obs.addListener(this.updateListener);
	}
	
	private void removeMiscUpdateListeners(StarterConfiguration config) {
		for (ObservableValue<?> obs : extractMiscObservables(config))
			obs.removeListener(this.updateListener);
	}
	
	private List<ObservableValue<?>> extractMiscObservables(StarterConfiguration config) {
		final List<ObservableValue<?>> obs = new ArrayList<>();
		for (AccountColumn c : AccountColumn.values())
			obs.add(config.displayColumnProperty(c));
		obs.add(config.customJavaPathProperty());
		obs.add(config.useCustomJavaPathProperty());
		obs.add(config.lookingGlassPathProperty());
		obs.add(config.lookingGlassProperty());
		obs.add(config.loginProperty());
		obs.add(config.tribotUsernameProperty());
		obs.add(config.tribotPasswordProperty());
		obs.add(config.supplySidProperty());
		obs.add(config.sidProperty());
		obs.add(config.customTribotPathProperty());
		obs.add(config.useCustomTribotPathProperty());
		return obs;
	}
	
	private void setupLaunchQueue() {
		
		this.launcher = new LaunchProcessor();
		
		this.launchQueue.setItems(this.launcher.getBacklog());
		
		this.launchQueue.setPlaceholder(new Text("No launches in progress"));
		
		final ContextMenu cm = new ContextMenu();
		
		final MenuItem remove = new MenuItem("Remove Selected");
		remove.setOnAction(e -> {
			this.launchQueue.getItems().removeAll(this.launchQueue.getSelectionModel().getSelectedItems());
		});
		
		final MenuItem removeAll = new MenuItem("Remove All");
		removeAll.setOnAction(e -> {
			this.launchQueue.getItems().clear();
		});
		
		cm.getItems().addAll(remove, removeAll);
		
		this.launchQueue.setContextMenu(cm);
	}
	
	private void setupColumnSelection() {
		for (AccountColumn col : AccountColumn.values()) {
			final CheckMenuItem item = new CheckMenuItem(col.toString());
			this.columnItems.put(col, item);
			item.selectedProperty().addListener((obs, old, newv) -> {
				if (newv)
					addColumn(col);
				else
					removeColumn(col);
			});
			this.columnSelection.getItems().add(item);
		}
		this.columnCount = Bindings.createLongBinding(() -> {
			return this.columnItems.values().stream().filter(CheckMenuItem::isSelected).count();
		}, this.columnItems.values().stream().map(CheckMenuItem::selectedProperty).toArray(BooleanProperty[]::new));
	}
	
	private void addColumn(AccountColumn col) {
		final TableColumn<AccountConfiguration, ?> column = this.columns.get(col);
		if (this.accounts.getColumns().contains(column))
			return;
		final int index = (int) (Arrays.stream(AccountColumn.values())
				.filter(c -> c.ordinal() < col.ordinal())
				.filter(c -> this.columnItems.get(c).isSelected())
				.count() + 1);
		if (index <= this.accounts.getColumns().size())
			this.accounts.getColumns().add(index, column);
		else
			this.accounts.getColumns().add(column);
	}
	
	private void removeColumn(AccountColumn col) {
		final TableColumn<AccountConfiguration, ?> column = this.columns.get(col);
		if (!this.accounts.getColumns().contains(column))
			return;
		this.accounts.getColumns().remove(column);
	}
	
	private void setupConsole() {
		this.console.setPlaceholder(new Text("No messages to display"));
		final PrintStream ps = new PrintStream(new Console(this.console), false);
		final CommandLineConfig clConfig = GraphicalClientStarter.getConfig();
		if (!clConfig.isCloseAfterLaunch()) {
			System.setOut(ps);
			System.setErr(ps);
		}
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
		this.timeBetweenLaunch.getValueFactory().valueProperty().addListener((obs, old, newv) -> {
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
			else if (DUPLICATE_KEY_COMBO.match(e)) {
				e.consume();
				final AccountConfiguration acc = this.accounts.getSelectionModel().getSelectedItem();
				if (acc == null)
					return;
				this.cacheAccounts();
				this.accounts.getItems().add(this.accounts.getItems().indexOf(acc), acc.copy());
				this.updated();
			}
		});
		
		this.accounts.setRowFactory(t -> {
			final TableRow<AccountConfiguration> row = new TableRow<>();
			row.itemProperty().addListener((obs, old, newv) -> {
				row.styleProperty().unbind();
				if (newv != null) {
					row.styleProperty().bind(Bindings.createStringBinding(() -> {
								final Color color = newv.getColor();
								if (color == null)
									return "";
								if (newv.equals(this.accounts.getSelectionModel().getSelectedItem())) {
									return "-fx-selection-bar: " + colorToCssRgb(color.brighter())
										+ "-fx-selection-bar-non-focused: " + colorToCssRgb(color.darker());
								}
								return "-fx-background-color : " + colorToCssRgb(color);
							}, newv.colorProperty(), this.accounts.getSelectionModel().selectedItemProperty()));
				}
				else
					row.setStyle("");
			});
			return row;
		});
		
		this.accounts.setEditable(true);

		this.accounts.setContextMenu(createDefaultTableContextMenu());

		this.accounts.getColumns().add(createSelectAccountTableColumn());

		for (AccountColumnData data : STRING_ACCOUNT_COLUMN_DATA) {
			final TableColumn<AccountConfiguration, ?> col = createAccountTableColumn(data);
			this.columns.put(data.getCorresponding(), col);
		}

		final TableColumn<AccountConfiguration, ?> useProxy = createUseProxyTableColumn();
		this.columns.put(AccountColumn.USE_PROXY, useProxy);
		
		final TableColumn<AccountConfiguration, ?> proxy = createProxyTableColumn();
		this.columns.put(AccountColumn.PROXY, proxy);
		
		for (ProxyColumnData data : PROXY_COLUMN_DATA)
			this.columns.put(data.getCorresponding(), createProxyComponentColumn(data));
		
		NodeUtil.setDragAndDroppable(this.accounts);
	}
	
	private TableColumn<AccountConfiguration, ?> createProxyComponentColumn(ProxyColumnData data) {
		final TableColumn<AccountConfiguration, String> col = this.getBaseColumn(data.getLabel());
		col.setCellValueFactory(f -> {
			final AccountConfiguration config = f.getValue();
			if (config == null)
				return new SimpleStringProperty("");
			final ProxyDescriptor proxy = config.getProxy();
			if (proxy == null)
				return new SimpleStringProperty("");
			return new SimpleStringProperty(data.getDisplayFunction().apply(proxy));
		});
		col.setCellFactory(s -> {
			final TableCell<AccountConfiguration, String> cell = new TextFieldTableCell<>();
			cell.setContextMenu(createDefaultTableCellContextMenu(cell, col));
			return cell;
		});
		col.setOnEditCommit(e -> {
			cacheAccounts();
			final ProxyDescriptor proxy = e.getRowValue().getProxy();
			if (proxy == null)
				e.getRowValue().setProxy(data.getEditFunction().apply(e.getNewValue(), BASE_PROXY));
			else
				e.getRowValue().setProxy(data.getEditFunction().apply(e.getNewValue(), proxy));
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
    				.forEach(a -> {
    					final ProxyDescriptor proxy = a.getProxy();
    					if (proxy == null)
    						a.setProxy(data.getEditFunction().apply(value, BASE_PROXY));
    					else
    						a.setProxy(data.getEditFunction().apply(value, proxy));
    				});
    			updated();
    			this.accounts.refresh();
    		});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private String colorToCssRgb(Color color) {
		return "rgb(" + color.getRed() * 255 + "," + color.getGreen() * 255 + "," + color.getBlue() * 255 + ");";
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
				final String val = promptRowsToSelect(data.getLabel());
				if (val == null)
					return;
				this.cacheAccounts();
				this.accounts.getItems().forEach(a -> {
					if (val.equals(ReflectionUtil.getValue(a, data.getFieldName())))
						a.setSelected(true);
				});
				this.updated();
			});
			selectRows.getItems().add(item);
		}
		for (ProxyColumnData data : PROXY_COLUMN_DATA) {
			final MenuItem item = new MenuItem("By " + data.getLabel());
			item.setOnAction(e -> {
				final String val = promptRowsToSelect(data.getLabel());
				if (val == null)
					return;
				this.cacheAccounts();
				this.accounts.getItems().forEach(a -> {
					final ProxyDescriptor proxy = a.getProxy();
					if (proxy == null)
						return;
					if (val.equals(data.getDisplayFunction().apply(proxy)))
						a.setSelected(true);
				});
				this.updated();
			});
			selectRows.getItems().add(item);
		}
		final MenuItem selectRowsByColor = new MenuItem("By Color");
		selectRowsByColor.setOnAction(e -> {
			final Color c = this.promptRowsToSelectByColor();
			if (c == null)
				return;
			this.accounts.getItems().stream().filter(acc -> c.equals(acc.getColor())).forEach(acc -> acc.setSelected(true));
		});
		selectRows.getItems().add(selectRowsByColor);
		
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
				final String val = promptRowsToSelect(data.getLabel());
				if (val == null)
					return;
				this.cacheAccounts();
				this.accounts.getItems().forEach(a -> {
					a.setSelected(val.equals(ReflectionUtil.getValue(a, data.getFieldName())));
				});
				this.updated();
			});
			selectRowsOnly.getItems().add(item);
		}
		for (ProxyColumnData data : PROXY_COLUMN_DATA) {
			final MenuItem item = new MenuItem("By " + data.getLabel());
			item.setOnAction(e -> {
				final String val = promptRowsToSelect(data.getLabel());
				if (val == null)
					return;
				this.cacheAccounts();
				this.accounts.getItems().forEach(a -> {
					final ProxyDescriptor proxy = a.getProxy();
					a.setSelected(proxy != null && val.equals(data.getDisplayFunction().apply(proxy)));
				});
				this.updated();
			});
			selectRows.getItems().add(item);
		}
		final MenuItem selectRowsOnlyByColor = new MenuItem("By Color");
		selectRowsOnlyByColor.setOnAction(e -> {
			final Color c = this.promptRowsToSelectByColor();
			if (c == null)
				return;
			this.accounts.getItems().stream().forEach(acc -> acc.setSelected(c.equals(acc.getColor())));
		});
		selectRowsOnly.getItems().add(selectRowsOnlyByColor);
		
		cm.getItems().addAll(selectRows, selectRowsOnly);
		
		selected.setContextMenu(cm);
		
		return selected;
	}
	
	private Set<Integer> promptRowsToSelectByIndex() {
		final TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Select rows by 'Row Index'");
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
	
	private String promptRowsToSelect(String label) {
		final TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Select rows by '" + label + "'");
		dialog.setHeaderText("Select rows by " + label);
		dialog.setContentText("Enter '" + label + "'");
		dialog.initOwner(this.stage);
		return dialog.showAndWait().orElse(null);
	}
	
	private Color promptRowsToSelectByColor() {
		final Dialog<Color> dialog = new Dialog<>();
		dialog.setTitle("Select rows by 'Row Color'");
		dialog.setHeaderText("Select 'Row Color'");
		dialog.setContentText("Select Color");
		final ColorPicker color = new ColorPicker(Color.WHITE);
		final HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(color);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(this.stage);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK)
		        return color.getValue();
		    return null;
		});
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
			this.cacheAccounts();
			this.accounts.getItems().add(cell.getIndex() + 1, acc.copy());
			this.updated();
		});
		duplicate.setAccelerator(DUPLICATE_KEY_COMBO);
		duplicate.disableProperty().bind(cell.itemProperty().isNotNull().not());
		
		final MenuItem delete = new MenuItem("Delete Row");
		delete.setOnAction(e -> {
			if (!this.confirmRemoval(1))
				return;
			this.cacheAccounts();
			this.accounts.getItems().remove(cell.getIndex());
			this.updated();
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
		final TableColumn<AccountConfiguration, Boolean> col = getBasePropertyColumn("Use Proxy", "useProy");
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
		final TableColumn<AccountConfiguration, ProxyDescriptor> col = getBasePropertyColumn("Proxy", "proxy");
		col.setCellFactory(lv -> {
			final ObservableList<ProxyDescriptor> proxies = FXCollections.observableArrayList(this.proxies);
			proxies.add(ProxyDescriptor.NO_PROXY);
			final ComboBoxTableCell<AccountConfiguration, ProxyDescriptor> cell = new ComboBoxTableCell<>(proxies.toArray(new ProxyDescriptor[0]));
			cell.setContextMenu(createDefaultTableCellContextMenu(cell, col));
			return cell;
		});
		col.setOnEditCommit(e -> {
			this.cacheAccounts();
			final ProxyDescriptor actual = e.getNewValue() == ProxyDescriptor.NO_PROXY
					? null
					: e.getNewValue();
			e.getRowValue().setProxy(actual);
			this.updated();
			this.accounts.refresh();
		});
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set 'Proxy' for selected accounts");
		set.setOnAction(e -> {
			final ObservableList<ProxyDescriptor> proxies = FXCollections.observableArrayList(this.proxies);
			proxies.add(ProxyDescriptor.NO_PROXY);
    		final ChoiceDialog<ProxyDescriptor> dialog = new ChoiceDialog<>(null, proxies);
    		dialog.setTitle("Set Proxy");
    		dialog.setHeaderText("Set 'Proxy' for selected accounts");
    		dialog.setContentText("Select Proxy");
    		dialog.initOwner(this.stage);
    		dialog.showAndWait().ifPresent(value -> {
    			final ProxyDescriptor actual = value == ProxyDescriptor.NO_PROXY
						    					? null
						    					: value;
    			cacheAccounts();
    			this.accounts.getItems().stream()
    				.filter(AccountConfiguration::isSelected)
    				.forEach(a -> ReflectionUtil.setValue(a, "proxy", actual));
    			updated();
    			this.accounts.refresh();
    		});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private <T> TableColumn<AccountConfiguration, T> getBaseColumn(String label) {
		final TableColumn<AccountConfiguration, T> col = new TableColumn<>(label);
		col.prefWidthProperty().bind(this.accounts.widthProperty().subtract(30).divide(this.columnCount));
		col.setMinWidth(110);
		col.setEditable(true);
		return col;
	}
	
	private <T> TableColumn<AccountConfiguration, T> getBasePropertyColumn(String label, String fieldName) {
		final TableColumn<AccountConfiguration, T> col = getBaseColumn(label);
		col.setCellValueFactory(new PropertyValueFactory<AccountConfiguration, T>(fieldName));
		return col;
	}

	private TableColumn<AccountConfiguration, String> createAccountTableColumn(AccountColumnData data) {
		final TableColumn<AccountConfiguration, String> col = getBasePropertyColumn(data.getLabel(), data.getFieldName());
		col.setCellFactory(s -> {
			final TableCell<AccountConfiguration, String> cell = new TextFieldTableCell<>();
			cell.setContextMenu(createDefaultTableCellContextMenu(cell, col));
			return cell;
		});
		col.setOnEditCommit(e -> {
			cacheAccounts();
			ReflectionUtil.setValue(e.getRowValue(), data.getFieldName(), e.getNewValue());
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
    				.forEach(a -> ReflectionUtil.setValue(a, data.getFieldName(), value));
    			updated();
    			this.accounts.refresh();
    		});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	// Note that currently undo/redo do not take into account selecting checkbox table cells manually
	
	private void cacheAccounts() {
		this.undo.push(this.model.get().copy());
		this.redo.clear();
	}
	
	private void undoAccounts() {
		if (this.undo.isEmpty())
			return;
		this.redo.push(this.model.get().copy());
		this.model.get().getAccounts().setAll(this.undo.pop().getAccounts());
		updated();
	}
	
	private void redoAccounts() {
		if (this.redo.isEmpty())
			return;
		this.undo.push(this.model.get().copy());
		this.model.get().getAccounts().setAll(this.redo.pop().getAccounts());
		updated();
	}
	
	private void updated() {
		if (this.config.get().isAutoSaveLast())
			save(LAST);
		this.outdated.set(true);
	}
	
	private void load(String name) {
		final StarterConfiguration config = readSettingsFile(name);
		if (config == null)
			return;
		cacheAccounts();
		this.model.set(config);
		updated();
		this.outdated.set(false);
		if (!name.equals(LAST))
			this.lastSaveName.set(name);
	}
	
	private StarterConfiguration readSettingsFile(String name) {
		if (name == null || name.isEmpty())
			return null;
		if (!new File(name).isAbsolute())
			name = FileUtil.getSettingsDirectory().getAbsolutePath() + File.separator + name;
		if (!name.endsWith(".json"))
			name += ".json";
		final File file = new File(name);
		if (!file.exists()) {
			System.out.println("Failed to open '" + name + "', does not exist");
			return null;
		}
		try {
			final byte[] contents = Files.readAllBytes(file.toPath());
			final StarterConfiguration config = GsonFactory.buildGson().fromJson(new String(contents), StarterConfiguration.class);
			return config;
		}
		catch (IOException | JsonSyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void save(String name) {
		if (name.isEmpty())
			return;
		if (!new File(name).isAbsolute())
			name = FileUtil.getSettingsDirectory().getAbsolutePath() + File.separator + name;
		if (!name.endsWith(".json"))
			name += ".json";
		final String save = GsonFactory.buildGson().toJson(this.model.get());
		try {
			Files.write(new File(name).toPath(), save.getBytes());
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
