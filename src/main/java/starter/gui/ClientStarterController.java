package starter.gui;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
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
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
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
import starter.models.AccountConfiguration;
import starter.models.ApplicationConfiguration;
import starter.models.CommandLineConfig;
import starter.models.ObservableStack;
import starter.models.PendingLaunch;
import starter.models.ProxyColumnData;
import starter.models.ProxyDescriptor;
import starter.models.StarterConfiguration;
import starter.models.Theme;
import starter.util.FileUtil;
import starter.util.ReflectionUtil;
import starter.util.TribotProxyGrabber;

public class ClientStarterController implements Initializable {

	private static final String LAST = "last.json";
	
	private static final String SOURCE_REPO_PATH = "https://github.com/Naton1/Graphical-Client-Starter/";
	private static final String THREAD_PATH = "https://tribot.org/forums/topic/80538-graphical-client-starter/";
	private static final String DOWNLOAD_PATH = "https://github.com/Naton1/Download-Graphical-Client-Starter";
	
	private static final ProxyDescriptor BASE_PROXY = new ProxyDescriptor("", "", 0, "", "");

	private static final AccountColumn[] STRING_ACCOUNT_COLUMN_DATA = {
			AccountColumn.NAME,
			AccountColumn.PASSWORD,
			AccountColumn.PIN,
			AccountColumn.SCRIPT,
			AccountColumn.ARGS,
			AccountColumn.WORLD,
			AccountColumn.BREAK_PROFILE, 
			AccountColumn.HEAP_SIZE
	};
	
	// TODO put into enum
	private static final ProxyColumnData[] PROXY_COLUMN_DATA = {
			new ProxyColumnData(
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
			new ProxyColumnData(
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
			new ProxyColumnData(
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
			new ProxyColumnData(
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
	
	private static final KeyCodeCombination COPY_ALL_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	private static final KeyCodeCombination COPY_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	private static final KeyCodeCombination PASTE_KEY_COMBO = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
	private static final KeyCodeCombination DUPLICATE_KEY_COMBO = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
	
	// should remove the appl config from being a property; its not really meant to be a property/change
	private final SimpleObjectProperty<ApplicationConfiguration> config = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<StarterConfiguration> model = new SimpleObjectProperty<>();
	
	private final ObservableStack<StarterConfiguration> undo = new ObservableStack<>();
	private final ObservableStack<StarterConfiguration> redo = new ObservableStack<>();
	
	private final SimpleStringProperty lastSaveName = new SimpleStringProperty(null);
	private final SimpleBooleanProperty outdated = new SimpleBooleanProperty(false);
	
	private final ProxyDescriptor[] proxies = TribotProxyGrabber.getProxies();
	
	private final Map<AccountColumn, TableColumn<AccountConfiguration, ?>> columns = new HashMap<>();
	private final Map<AccountColumn, CheckMenuItem> columnItems = new HashMap<>();
	
	private static final String TYPE = "application/x-java-serialized-object";
	private static final DataFormat SERIALIZED_MIME_TYPE;

	static {
		final DataFormat lookup = DataFormat.lookupMimeType(TYPE);
		SERIALIZED_MIME_TYPE = lookup != null ? lookup : new DataFormat(TYPE);
	}
	
	private static final int CHECK_COL_WIDTH = 30;
	
	private final ChangeListener<Object> updateListener = (obs, old, newv) -> {
		this.updated();
	};
	
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
	
	@FXML
	private Menu theme;
	
	@FXML
	private Menu selectionMode;
	
	private LongBinding columnCount; // set after initializing column menu items
	
	private LaunchProcessor launcher;
	
	private Stage stage;
	
	private ObjectProperty<Integer> delayBetweenLaunchProperty; // have to store a reference so we can unbind the bidirectional binding
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		setupConsole();
		this.config.set(getApplicationConfig());
		this.config.get().runOnChange(() -> saveApplicationConfig());
		this.autoSaveLast.selectedProperty().bindBidirectional(this.config.get().autoSaveLastProperty());
		this.save.disableProperty().bind(this.outdated.not());
		setupColumnSelection();
		setupAccountTable();
		setupSpinner();
		setupTheme();
		setupSelectionMode();
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
		this.bindStyle(stage.getScene());
		
		final EventHandler<WindowEvent> onFirstShow = e -> {
			
			if (this.config.get().getWidth() == Double.NaN)
				this.config.get().setWidth(this.stage.getWidth());
			if (this.config.get().getHeight() == Double.NaN)
				this.config.get().setHeight(this.stage.getHeight());
			
			if (this.config.get().getWidth() < 200)
				this.config.get().setWidth(200);
			if (this.config.get().getHeight() < 200)
				this.config.get().setHeight(200);
			
			this.stage.setWidth(this.config.get().getWidth());
			this.stage.setHeight(this.config.get().getHeight());
			
			this.config.get().widthProperty().addListener((obs, old, newv) -> {
				this.stage.setWidth(newv.doubleValue());
			});
			
			this.config.get().heightProperty().addListener((obs, old, newv) -> {
				this.stage.setHeight(newv.doubleValue());
			});
			
			this.config.get().heightProperty().bind(this.stage.heightProperty());
			this.config.get().widthProperty().bind(this.stage.widthProperty());
			
			if (this.config.get().getX() == Double.NaN)
				this.config.get().setX(this.stage.getX());
			if (this.config.get().getY() == Double.NaN)
				this.config.get().setY(this.stage.getY());
			
			final Rectangle base = new Rectangle((int)this.config.get().getX(), (int)this.config.get().getY(), (int)this.config.get().getWidth(), (int)this.config.get().getHeight());
			
			if (Screen.getScreens()
					.stream()
					.noneMatch(s -> {
						final Rectangle2D visual2d = s.getVisualBounds();
						final Rectangle visual = new Rectangle((int)visual2d.getMinX(),
								(int)visual2d.getMinY(),
								(int)(visual2d.getMaxX() - visual2d.getMinX()),
								(int)(visual2d.getMaxY() - visual2d.getMinY()));
						if (!visual.intersects(base))
							return false;
						final Rectangle intersection = visual.intersection(base);
						final int intersectionArea = intersection.width * intersection.height;
						final int windowArea = base.width * base.height;
						return intersectionArea >= windowArea * 0.20;
					})) {
				this.stage.centerOnScreen();
			}
			else {
				this.stage.setX(this.config.get().getX());
				this.stage.setY(this.config.get().getY());
			}
			
			this.config.get().xProperty().addListener((obs, old, newv) -> {
				this.stage.setX(newv.doubleValue());
			});
			
			this.config.get().yProperty().addListener((obs, old, newv) -> {
				this.stage.setY(newv.doubleValue());
			});
			
			this.config.get().xProperty().bind(this.stage.xProperty());
			this.config.get().yProperty().bind(this.stage.yProperty());
		};
		
		this.stage.addEventHandler(WindowEvent.WINDOW_SHOWN, onFirstShow);
		this.stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
			this.stage.removeEventHandler(WindowEvent.WINDOW_SHOWN, onFirstShow);
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
		showLink("View Source", "Source Repository", SOURCE_REPO_PATH);
	}
	
	@FXML
	public void viewThread() {
		showLink("View Thread", "TRiBot Thread", THREAD_PATH);
	}
	
	@FXML
	public void viewDownload() {
		showLink("View Download", "Download", DOWNLOAD_PATH);
	}
	
	@FXML
	public void colorSelectedAccounts() {
		final Dialog<Color> dialog = new Dialog<>();
		this.bindStyle(dialog.getDialogPane().getScene());
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
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/import.fxml")
		.withWindowName("Import Accounts")
		.withApplicationConfig(this.config.get())
		.<ImportController>onCreation((stage, controller) -> {
			controller.init(stage, accs -> {
				this.cacheAccounts();
				this.accounts.getItems().addAll(accs);
				this.updated();
			}, this.config);
		})
		.build();
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
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/lg.fxml")
		.withWindowName("Looking Glass Configuration")
		.withApplicationConfig(this.config.get())
		.<LookingGlassController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
	@FXML
	public void configureJavaPath() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/java_path.fxml")
		.withWindowName("Custom Java Path")
		.withApplicationConfig(this.config.get())
		.<JavaPathController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
	@FXML
	public void displayTribotJar() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/custom_jar.fxml")
		.withWindowName("Custom TRiBot Jar Configuration")
		.withApplicationConfig(this.config.get())
		.<CustomJarController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
	@FXML
	public void displayTribotSignin() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/signin.fxml")
		.withWindowName("TRiBot Sign-in Configuration")
		.withApplicationConfig(this.config.get())
		.<TRiBotSignInController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
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
	
	private void setupTheme() {
		final ToggleGroup group = new ToggleGroup();
		for (Theme theme : Theme.values()) {
			final RadioMenuItem item = new RadioMenuItem(theme.toString());
			item.setToggleGroup(group);
			item.selectedProperty().addListener((obs, old, newv) -> {
				if (newv)
					this.config.get().setTheme(theme);
			});
			this.theme.getItems().add(item);
			item.setSelected(this.config.get().getTheme() == theme);
		}
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
	
	private void bindStyle(Scene scene) {
		final ChangeListener<Theme> themeListener = (obs, old, newv) -> {
			scene.getStylesheets().setAll(newv.getCss());
		};
		themeListener.changed(this.config.get().themeProperty(), null, this.config.get().getTheme());
		this.config.get().themeProperty().addListener(themeListener);
		scene.addEventHandler(WindowEvent.WINDOW_HIDDEN, e -> this.config.get().themeProperty().removeListener(themeListener));
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
			//System.setOut(ps);
			//System.setErr(ps);
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
		this.timeBetweenLaunch.setValueFactory(new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 30));
		this.timeBetweenLaunch.setEditable(true);
		this.timeBetweenLaunch.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue)
				this.timeBetweenLaunch.increment(0); // won't change value, but will commit editor
		});
		this.timeBetweenLaunch.getValueFactory().valueProperty().addListener((obs, old, newv) -> {
			this.updated();
		});
	}
	
	private void setupSelectionMode() {
		final ToggleGroup group = new ToggleGroup();
		final Map<starter.models.SelectionMode, RadioMenuItem> map = new HashMap<>();
		for (starter.models.SelectionMode mode : starter.models.SelectionMode.values()) {
			final RadioMenuItem item = new RadioMenuItem(mode.toString());
			item.setToggleGroup(group);
			map.put(mode, item);
			item.setSelected(this.config.get().getSelectionMode() == mode);
			this.selectionMode.getItems().add(item);
		}
		this.config.get().selectionModeProperty().bind(Bindings.createObjectBinding(() -> {
			return map.entrySet().stream()
					.filter(entry -> entry.getValue().isSelected()).findFirst()
					.map(Map.Entry::getKey)
					.orElseThrow(() -> new IllegalStateException("No selection mode selected"));
		}, map.values().stream().map(RadioMenuItem::selectedProperty).toArray(BooleanProperty[]::new)));
		final ChangeListener<starter.models.SelectionMode> listener = (obs, old, newv) -> {
			switch (newv) {
			case CELL:
				this.accounts.getSelectionModel().setCellSelectionEnabled(true);
				break;
			case ROW:
				this.accounts.getSelectionModel().setCellSelectionEnabled(false);
				break;
			}
		};
		listener.changed(this.config.get().selectionModeProperty(), null, this.config.get().getSelectionMode());
		this.config.get().selectionModeProperty().addListener(listener);
	}
	
	private void showLink(String title, String header, String path) {
		final Alert alert = new Alert(AlertType.INFORMATION);
		this.bindStyle(alert.getDialogPane().getScene());
		alert.setTitle(title);
		alert.setHeaderText(header);
		Node node;
		if (Desktop.isDesktopSupported()) {
			final Hyperlink link = new Hyperlink(path);
			link.setOnAction(e -> {
				try {
					Desktop.getDesktop().browse(new URL(path).toURI());
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
			text.setText(path);
			node = text;
		}
		alert.getDialogPane().setContent(node);
		alert.initOwner(this.stage);
		alert.showAndWait();
	}
	
	private void setupAccountTable() {
		
		this.accounts.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (COPY_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.get().getSelectionMode()) {
				case CELL:
					copyCellsToClipboard();
					break;
				case ROW:
					copyAccountsToClipboard(this.accounts.getSelectionModel().getSelectedItems());
					break;
				}
			}
			else if (PASTE_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.get().getSelectionMode()) {
				case CELL:
					pasteCellsFromClipboard();
					break;
				case ROW:
					pasteAccountFromClipboard();
					break;
				}
			}
			else if (DUPLICATE_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.get().getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					duplicateSelectedAccounts();
					break;
				}

			}
		});
		
		this.accounts.setEditable(true);

		this.accounts.setContextMenu(createDefaultTableContextMenu());

		final TableColumn<AccountConfiguration, ?> sel = createSelectAccountTableColumn();
		this.accounts.getColumns().add(sel);
		this.columns.put(AccountColumn.SELECTED, sel);

		for (AccountColumn data : STRING_ACCOUNT_COLUMN_DATA) {
			final TableColumn<AccountConfiguration, ?> col = createAccountTableColumn(data);
			this.columns.put(data, col);
		}

		final TableColumn<AccountConfiguration, ?> useProxy = createUseProxyTableColumn();
		this.columns.put(AccountColumn.USE_PROXY, useProxy);
		
		final TableColumn<AccountConfiguration, ?> proxy = createProxyTableColumn();
		this.columns.put(AccountColumn.PROXY, proxy);
		
		for (ProxyColumnData data : PROXY_COLUMN_DATA)
			this.columns.put(data.getCorresponding(), createProxyComponentColumn(data));
		
		this.setAccountRowFactory();
		
		this.accounts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
			final TableCell<AccountConfiguration, String> cell = new TextFieldTableCell<>(this.config.get());
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
    		this.bindStyle(dialog.getDialogPane().getScene());
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
		selected.setPrefWidth(CHECK_COL_WIDTH);

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
		for (AccountColumn data : STRING_ACCOUNT_COLUMN_DATA) {
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
		
		for (AccountColumn data : STRING_ACCOUNT_COLUMN_DATA) {
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
			selectRowsOnly.getItems().add(item);
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
		this.bindStyle(dialog.getDialogPane().getScene());
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
		this.bindStyle(dialog.getDialogPane().getScene());
		dialog.setTitle("Select rows by '" + label + "'");
		dialog.setHeaderText("Select rows by " + label);
		dialog.setContentText("Enter '" + label + "'");
		dialog.initOwner(this.stage);
		return dialog.showAndWait().orElse(null);
	}
	
	private Color promptRowsToSelectByColor() {
		final Dialog<Color> dialog = new Dialog<>();
		this.bindStyle(dialog.getDialogPane().getScene());
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
		
		final List<MenuItem> defaultItems = new ArrayList<>(cm.getItems());
		
		final MenuItem duplicate = new MenuItem();
		duplicate.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.accounts.getSelectionModel().getSelectedItems().size() > 1
					? "Duplicate Rows"
					: "Duplicate Row";
		}, this.accounts.getSelectionModel().selectedItemProperty()));
		duplicate.setOnAction(e -> {
			duplicateSelectedAccounts();
		});
		duplicate.setAccelerator(DUPLICATE_KEY_COMBO);
		duplicate.disableProperty().bind(cell.itemProperty().isNotNull().not());
		
		final MenuItem delete = new MenuItem();
		delete.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.accounts.getSelectionModel().getSelectedItems().size() > 1
					? "Delete Rows"
					: "Delete Row";
		}, this.accounts.getSelectionModel().selectedItemProperty()));
		delete.setOnAction(e -> {
			final List<AccountConfiguration> accs = this.accounts.getSelectionModel().getSelectedItems();
			if (accs.size() == 0)
				return;
			if (!this.confirmRemoval(accs.size()))
				return;
			this.cacheAccounts();
			this.accounts.getItems().removeAll(accs);
			this.updated();
		});
		
		delete.disableProperty().bind(cell.itemProperty().isNotNull().not());
		
		final MenuItem edit = new MenuItem("Edit Cell");
		edit.setOnAction(e -> {
			this.accounts.edit(cell.getIndex(), col);
		});
		edit.disableProperty().bind(cell.itemProperty().isNotNull().not());
		
		final MenuItem copyRows = new MenuItem();
		copyRows.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.accounts.getSelectionModel().getSelectedItems().size() > 1
					? "Copy Rows"
					: "Copy Row";
		}, this.accounts.getSelectionModel().selectedItemProperty()));
		copyRows.setOnAction(e -> {
			copyAccountsToClipboard(this.accounts.getSelectionModel().getSelectedItems());
		});
		copyRows.disableProperty().bind(cell.itemProperty().isNotNull().not());
		copyRows.setAccelerator(COPY_KEY_COMBO);
		
		final MenuItem pasteRows =  new MenuItem();
		pasteRows.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.accounts.getSelectionModel().getSelectedItems().size() > 1
					? "Paste Rows"
					: "Paste Row";
		}, this.accounts.getSelectionModel().selectedItemProperty()));
		pasteRows.setOnAction(e -> {
			pasteAccountFromClipboard();
		});
		pasteRows.setAccelerator(PASTE_KEY_COMBO);
		// these accelerators don't directly get triggered but the table has event handlers to handle them,
		// so they exist to notify the user of the shortcuts
		
		final MenuItem copyCells = new MenuItem();
		copyCells.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.accounts.getSelectionModel().getSelectedItems().size() > 1
					? "Copy Cells"
					: "Copy Cell";
		}, this.accounts.getSelectionModel().selectedItemProperty()));
		copyCells.setOnAction(e -> {
			copyCellsToClipboard();
		});
		copyCells.disableProperty().bind(cell.itemProperty().isNotNull().not());
		copyCells.setAccelerator(COPY_KEY_COMBO);
		
		final MenuItem pasteCells =  new MenuItem();
		pasteCells.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.accounts.getSelectionModel().getSelectedItems().size() > 1
					? "Paste Cells"
					: "Paste Cell";
		}, this.accounts.getSelectionModel().selectedItemProperty()));
		pasteCells.setOnAction(e -> {
			pasteCellsFromClipboard();
		});
		pasteCells.setAccelerator(PASTE_KEY_COMBO);
		// these accelerators don't directly get triggered but the table has event handlers to handle them,
		// so they exist to notify the user of the shortcuts
		
		final ChangeListener<starter.models.SelectionMode> listener = (obs, old, newv) -> {
			switch (newv) {
			case ROW:
				cm.getItems().clear();
				cm.getItems().addAll(edit, new SeparatorMenuItem(), copyRows, pasteRows, new SeparatorMenuItem(), duplicate, delete, new SeparatorMenuItem());
				cm.getItems().addAll(defaultItems);
				break;
			case CELL:
				cm.getItems().clear();
				cm.getItems().addAll(edit, new SeparatorMenuItem(), copyCells, pasteCells, new SeparatorMenuItem());
				cm.getItems().addAll(defaultItems);
				break;
			}
		};
		listener.changed(this.config.get().selectionModeProperty(), null, this.config.get().getSelectionMode());
		this.config.get().selectionModeProperty().addListener(listener);
		
		return cm;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private void copyCellsToClipboard() {
		final TablePosition[] selected = this.accounts.getSelectionModel().getSelectedCells()
											.stream()
											.sorted(Comparator.<TablePosition>comparingInt(TablePosition::getRow)
													.thenComparingInt(TablePosition::getColumn))
											.toArray(TablePosition[]::new);
		int lastRow = -1;
		boolean hasStartedRow = false;
		String copy = "";
		for (TablePosition pos : selected) {
			if (lastRow != -1 && lastRow != pos.getRow()) {
				copy += System.lineSeparator();
				hasStartedRow = false;
			}
			lastRow = pos.getRow();
			if (!hasStartedRow)
				hasStartedRow = true;
			else
				copy += "\t";
			copy += String.valueOf(pos.getTableColumn().getCellData(pos.getRow()));
		}
		final ClipboardContent contents = new ClipboardContent();
		contents.put(DataFormat.PLAIN_TEXT, copy);
		Clipboard.getSystemClipboard().setContent(contents);
	}
	
	@SuppressWarnings("rawtypes")
	private void pasteCellsFromClipboard() {
		final List<TablePosition> selected = this.accounts.getSelectionModel().getSelectedCells();
		if (selected.size() == 0)
			return;
		final TablePosition last = selected.get(0);
		final int row = last.getRow();
		final int col = last.getColumn();
		final String copied = (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
		if (copied == null)
			return;
		final String[] lines = copied.split(Pattern.quote(System.lineSeparator()), -1);
		System.out.println(lines.length);
		int rows = 0;
		int cols = 0;
		final AccountColumn[] sorted = this.columns.entrySet()
				.stream()
				.filter(e -> this.accounts.getColumns().contains(e.getValue()))
				.sorted(Comparator.comparingInt(e -> this.accounts.getColumns().indexOf(e.getValue())))
				.map(Map.Entry::getKey)
				.toArray(AccountColumn[]::new);
		for (String line : lines) {
			final String[] cells = line.split(Pattern.quote("\t"), -1);
			for (String cell : cells) {
				final int targetColumn = cols + col;
				final int targetRow = rows + row;
				if (targetColumn >= sorted.length)
					break;
				while (targetRow >= this.accounts.getItems().size())
					this.accounts.getItems().add(new AccountConfiguration());
				sorted[targetColumn].setField(this.accounts.getItems().get(targetRow), cell);
				cols++;
			}
			cols = 0;
			rows++;
		}
		this.accounts.refresh();
	}
	
	private void pasteAccountFromClipboard() {
		
		final AccountConfiguration acc = this.accounts.getSelectionModel().getSelectedItem();
		final int index = acc != null ? this.accounts.getItems().indexOf(acc) : this.accounts.getItems().size() - 1;
		
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		
		if (!clipboard.hasContent(DataFormat.PLAIN_TEXT))
			return;
		
		final String content = (String) clipboard.getContent(DataFormat.PLAIN_TEXT);
		if (content == null)
			return;
		
		final AccountConfiguration[] accs = GsonFactory.buildGson().fromJson(content, AccountConfiguration[].class);
		if (accs == null)
			return;
		
		this.cacheAccounts();
		this.accounts.getItems().addAll(index, Arrays.asList(accs));
		this.updated();
		
	}
	
	private void duplicateSelectedAccounts() {
		final List<AccountConfiguration> sorted = this.accounts.getSelectionModel().getSelectedItems()
													.stream()
													.sorted(Comparator.comparingInt(this.accounts.getItems()::indexOf))
													.collect(Collectors.toList());
		final List<AccountConfiguration> accs = sorted
												.stream()
												.map(AccountConfiguration::copy)
												.collect(Collectors.toList());
		if (accs.size() == 0)
			return;
		final int index = this.accounts.getItems().indexOf(sorted.get(0));
		this.cacheAccounts();
		this.accounts.getItems().addAll(index + 1, accs);
		this.updated();
		this.accounts.getSelectionModel().clearSelection();
		this.accounts.getSelectionModel().selectRange(index + 1, index + 1 + accs.size());
	}
	
	private void copyAccountsToClipboard(Collection<AccountConfiguration> accs) {
		final String s = GsonFactory.buildGson().toJson(accs.toArray(new AccountConfiguration[0]));
		final ClipboardContent content = new ClipboardContent();
		content.put(DataFormat.PLAIN_TEXT, s);
		Clipboard.getSystemClipboard().setContent(content);
	}
	
	private void setAccountRowFactory() {
		
		this.accounts.setRowFactory(t -> {
			
			final TableRow<AccountConfiguration> row = new TableRow<>();
			
			row.itemProperty().addListener((obs, old, newv) -> {
				row.styleProperty().unbind();
				if (newv != null) {
					row.styleProperty().bind(Bindings.createStringBinding(() -> {
								final Color color = newv.getColor();
								if (color == null)
									return "";
								if (this.accounts.getSelectionModel().getSelectedItems().contains(newv)) {
									return "-fx-selection-bar: " + colorToCssRgb(color.brighter())
										+ "-fx-selection-bar-non-focused: " + colorToCssRgb(color.darker());
								}
								return "-fx-background-color : " + colorToCssRgb(color);
							}, newv.colorProperty(), this.accounts.getSelectionModel().selectedItemProperty()));
				}
				else
					row.setStyle("");
			});

			row.setOnDragDetected(e -> {
				switch (this.config.get().getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					if (row.isEmpty())
						return;
					if (e.getButton() != MouseButton.PRIMARY)
						return;
					final Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					final ClipboardContent cc = new ClipboardContent();
					cc.put(SERIALIZED_MIME_TYPE, row.getIndex());
					db.setContent(cc);
					e.consume();
					break;
				}
			});

			row.setOnDragOver(event -> {
				switch (this.config.get().getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					final Dragboard db = event.getDragboard();
					if (db.hasContent(SERIALIZED_MIME_TYPE)) {
						if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
							event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
							event.consume();
						}
					}
					break;
				}
			});

			row.setOnDragDropped(event -> {
				switch (this.config.get().getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					final Dragboard db = event.getDragboard();
					if (db.hasContent(SERIALIZED_MIME_TYPE)) {

						final int dropIndex = row.getIndex();
						if (dropIndex >= this.accounts.getItems().size())
							return;
						
						final int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
						final AccountConfiguration draggedItem = this.accounts.getItems().remove(draggedIndex);

						this.accounts.getItems().add(dropIndex, draggedItem);

						event.setDropCompleted(true);
						this.accounts.getSelectionModel().clearAndSelect(dropIndex);
						event.consume();
					}
					break;
				}
			});

			return row;
		});
	}
	
	private TableColumn<AccountConfiguration, Boolean> createUseProxyTableColumn() {
		final TableColumn<AccountConfiguration, Boolean> col = getBasePropertyColumn("Use Proxy", "useProxy");
		col.setCellFactory(lv -> new CheckBoxTableCell<>(index -> this.accounts.getItems().get(index).useProxyProperty()));
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set 'Use Proxy' for selected accounts");
		set.setOnAction(e -> {
			final Alert dialog = new Alert(AlertType.CONFIRMATION);
			this.bindStyle(dialog.getDialogPane().getScene());
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
    		this.bindStyle(dialog.getDialogPane().getScene());
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
		// TODO bug: for some reason this doesn't match up exactly, few pixels too large so requires scrollbar
		col.prefWidthProperty().bind(this.accounts.widthProperty().subtract(CHECK_COL_WIDTH).divide(this.columnCount));
		col.setMinWidth(110);
		col.setEditable(true);
		return col;
	}
	
	private <T> TableColumn<AccountConfiguration, T> getBasePropertyColumn(String label, String fieldName) {
		final TableColumn<AccountConfiguration, T> col = getBaseColumn(label);
		col.setCellValueFactory(new PropertyValueFactory<AccountConfiguration, T>(fieldName));
		return col;
	}

	private TableColumn<AccountConfiguration, String> createAccountTableColumn(AccountColumn data) {
		final TableColumn<AccountConfiguration, String> col = getBasePropertyColumn(data.getLabel(), data.getFieldName());
		col.setCellFactory(s -> {
			final TableCell<AccountConfiguration, String> cell = new TextFieldTableCell<>(this.config.get());
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
    		this.bindStyle(dialog.getDialogPane().getScene());
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
		this.bindStyle(alert.getDialogPane().getScene());
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
		this.bindStyle(alert.getDialogPane().getScene());
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
		this.bindStyle(alert.getDialogPane().getScene());
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

}
