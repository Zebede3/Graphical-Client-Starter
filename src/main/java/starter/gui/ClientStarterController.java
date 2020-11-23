package starter.gui;

import java.awt.Rectangle;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.LongExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import starter.GraphicalClientStarter;
import starter.data.KeyCombinations;
import starter.gui.import_accs.ImportController;
import starter.gui.java_path.JavaPathController;
import starter.gui.launch_speed.LaunchSpeedController;
import starter.gui.lg.LookingGlassController;
import starter.gui.proxy_manager.ProxyManagerController;
import starter.gui.schedule.ScheduleController;
import starter.gui.tribot.jar_path.CustomJarController;
import starter.gui.tribot.signin.TRiBotSignInController;
import starter.gui.worlds.blacklist.WorldBlacklistController;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;
import starter.models.ActiveClient;
import starter.models.ApplicationConfiguration;
import starter.models.CommandLineConfig;
import starter.models.PendingLaunch;
import starter.models.ProxyColumnData;
import starter.models.ProxyDescriptor;
import starter.models.StarterConfiguration;
import starter.models.Theme;
import starter.util.ClipboardUtil;
import starter.util.ExportUtil;
import starter.util.FXUtil;
import starter.util.FileUtil;
import starter.util.LinkUtil;
import starter.util.PromptUtil;
import starter.util.ReflectionUtil;
import starter.util.ScreenUtil;
import starter.util.TribotProxyGrabber;

public class ClientStarterController implements Initializable {

	private static final String LAST = "last.json";
	
	private static final String SOURCE_REPO_PATH = "https://github.com/Naton1/Graphical-Client-Starter/";
	private static final String THREAD_PATH = "https://tribot.org/forums/topic/80538-graphical-client-starter/";
	private static final String DOWNLOAD_PATH = "https://github.com/Naton1/Graphical-Client-Starter/releases/";
	private static final String DOWNLOAD_UPDATER_PATH = "https://github.com/Naton1/Graphical-Client-Starter-Loader/releases";
	
	private static final AccountColumn[] STRING_ACCOUNT_COLUMN_DATA = {
			AccountColumn.NAME,
			AccountColumn.PASSWORD,
			AccountColumn.PIN,
			AccountColumn.SCRIPT,
			AccountColumn.ARGS,
			AccountColumn.WORLD,
			AccountColumn.BREAK_PROFILE, 
			AccountColumn.HEAP_SIZE,
			AccountColumn.NOTES,
	};
	
	private static final ProxyColumnData[] PROXY_COLUMN_DATA = {
			new ProxyColumnData(
				ProxyDescriptor::getIp,
				AccountColumn.PROXY_IP),
			new ProxyColumnData(
				p -> Integer.toString(p.getPort()),
				AccountColumn.PROXY_PORT),
			new ProxyColumnData(
				ProxyDescriptor::getUsername,
				AccountColumn.PROXY_USER),
			new ProxyColumnData(
				ProxyDescriptor::getPassword,
				AccountColumn.PROXY_PASS)
	};
	
	private static final Comparator<AccountConfiguration> COLOR_COMPARATOR = 
			Comparator.<AccountConfiguration>comparingInt(c -> {
				final Color col = c.getColor();
				if (col == null)
					return Integer.MIN_VALUE;
				int r = (int)( col.getRed() * 255);
				int g = (int)( col.getGreen() * 255);
				int b = (int)( col.getBlue() * 255);
				return (r << 16) + (g << 8) + b;
			});
	
	private static final String TYPE = "application/x-java-serialized-object";
	private static final DataFormat SERIALIZED_MIME_TYPE;

	static {
		final DataFormat lookup = DataFormat.lookupMimeType(TYPE);
		SERIALIZED_MIME_TYPE = lookup != null ? lookup : new DataFormat(TYPE);
	}
	
	private static final int CHECK_COL_WIDTH = 30;
	
	private final SimpleObjectProperty<StarterConfiguration> model = new SimpleObjectProperty<>();
	
	private final SimpleStringProperty lastSaveName = new SimpleStringProperty(null);
	private final SimpleBooleanProperty outdated = new SimpleBooleanProperty(false);
	
	private final ProxyDescriptor[] tribotProxies = TribotProxyGrabber.getProxies();
	
	private final Map<AccountColumn, TableColumn<AccountConfiguration, ?>> columns = new HashMap<>();
	private final Map<AccountColumn, CustomCheckMenuItem> columnItems = new HashMap<>();
	
	private final ListChangeListener<Object> listUpdateListener = (e) -> {
		this.updated();
	};
	
	private final ChangeListener<Object> updateListener = (obs, old, newv) -> {
		this.updated();
	};
	
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	
	private final UndoHandler undo = new UndoHandler(this.model);
	
	private final TribotPathScanner scanner = new TribotPathScanner();
	
	private ObservableList<ProxyDescriptor> proxies;
	
	private ApplicationConfiguration config;
	
	@FXML
	private TableView<AccountConfiguration> accounts;
	
	@FXML
	private MenuItem save;
	
	@FXML
	private CheckMenuItem autoSaveLast;
	
	@FXML
	private CheckMenuItem onlyLaunchInactiveAccounts;
	
	@FXML
	private CheckMenuItem minimizeClients;
	
	@FXML
	private ListView<String> console;
	
	@FXML
	private ListView<PendingLaunch> launchQueue;
	
	@FXML
	private ListView<ActiveClient> activeClients;
	
	@FXML
	private Menu columnSelection;
	
	@FXML
	private Menu theme;
	
	@FXML
	private Menu selectionMode;
	
	@FXML
	private CheckMenuItem debugMode;
	
	@FXML
	private Button launchButton;
	
	@FXML
	private Tab accountsTab;
	
	@FXML
	private Tab consoleTab;
	
	@FXML
	private Tab launchQueueTab;
	
	@FXML
	private Tab activeClientsTab;
	
	private LongBinding columnCount; // set after initializing column menu items
	
	private LaunchProcessor launcher;
	private ActiveClientObserver activeClientObserver;
	
	private Stage stage;
	
	private ListChangeListener<AccountConfiguration> lastListListener = null;
	
	private volatile boolean initialized;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		setupConsole();
		this.config = FileUtil.readApplicationConfig();
		setupProxies();
		this.config.runOnChange(() -> FileUtil.saveApplicationConfig(this.config));
		this.autoSaveLast.selectedProperty().bindBidirectional(this.config.autoSaveLastProperty());
		this.debugMode.selectedProperty().bindBidirectional(this.config.debugModeProperty());
		this.save.disableProperty().bind(this.outdated.not());
		setupColumnSelection();
		setupAccountTable();
		setupTheme();
		setupSelectionMode();
		setupActiveClients();
		setupLaunchQueue();
		setupTabCounts();
		setupModelBindings();
		this.model.set(new StarterConfiguration());
		this.launchButton.setOnMouseClicked(this::launch);
		if (this.config.isAutoSaveLast()) {
			load(LAST);
		}
		this.initialized = true;
		this.tryScanTribotPath(this.model.get());
	}
	
	public void init(Stage stage) {
		this.stage = stage;
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
			this.quit();
			e.consume();
		});
		this.bindStyle(stage.getScene());
		
		final EventHandler<WindowEvent> onFirstShow = e -> {
			
			if (Double.isNaN(this.config.getWidth()))
				this.config.setWidth(this.stage.getWidth());
			if (Double.isNaN(this.config.getHeight()))
				this.config.setHeight(this.stage.getHeight());
			
			if (this.config.getWidth() < 200)
				this.config.setWidth(500);
			if (this.config.getHeight() < 200)
				this.config.setHeight(500);
			
			this.stage.setWidth(this.config.getWidth());
			this.stage.setHeight(this.config.getHeight());

			this.stage.setMaximized(this.config.isMaximized());
			
			this.stage.maximizedProperty().addListener((obs, old, newv) -> {
				this.config.setMaximized(newv);
			});
			
			this.config.widthProperty().addListener((obs, old, newv) -> {
				this.stage.setWidth(newv.doubleValue());
			});
			
			this.config.heightProperty().addListener((obs, old, newv) -> {
				this.stage.setHeight(newv.doubleValue());
			});
			
			this.config.heightProperty().bind(this.stage.heightProperty());
			this.config.widthProperty().bind(this.stage.widthProperty());
			
			if (Double.isNaN(this.config.getX()))
				this.config.setX(this.stage.getX());
			if (Double.isNaN(this.config.getY()))
				this.config.setY(this.stage.getY());
			
			final Rectangle base = new Rectangle((int)this.config.getX(), (int)this.config.getY(), (int)this.config.getWidth(), (int)this.config.getHeight());
			
			if (!ScreenUtil.isOnScreen(base, 0.20)) {
				this.stage.centerOnScreen();
			}
			else {
				this.stage.setX(this.config.getX());
				this.stage.setY(this.config.getY());
			}
			
			this.config.xProperty().addListener((obs, old, newv) -> {
				this.stage.setX(newv.doubleValue());
			});
			
			this.config.yProperty().addListener((obs, old, newv) -> {
				this.stage.setY(newv.doubleValue());
			});
			
			this.config.xProperty().bind(this.stage.xProperty());
			this.config.yProperty().bind(this.stage.yProperty());
		};
		
		this.stage.addEventHandler(WindowEvent.WINDOW_SHOWN, onFirstShow);
		this.stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
			this.stage.removeEventHandler(WindowEvent.WINDOW_SHOWN, onFirstShow);
		});
	}
	
	@FXML
	public void addNewAccount() {
		this.undo.cacheAccounts();
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
		this.undo.cacheAccounts();
		this.accounts.getItems().removeAll(selected);
		updated();
	}
	
	@FXML
	public void clearAccountTable() {
		if (this.accounts.getItems().size() == 0)
			return;
		if (!confirmRemoval(this.accounts.getItems().size()))
			return;
		this.undo.cacheAccounts();
		this.accounts.getItems().clear();
		updated();
	}
	
	private void setupModelBindings() {
		this.model.addListener((obs, old, newv) -> {
			if (old != null) {
				if (this.lastListListener != null)
					old.getAccounts().removeListener(this.lastListListener);
				this.accounts.setItems(FXCollections.observableArrayList());
				for (AccountColumn col : AccountColumn.values()) {
					final SimpleBooleanProperty prop = newv.displayColumnProperty(col);
					final CustomCheckMenuItem item = this.columnItems.get(col);
					item.selectedProperty().unbindBidirectional(prop);
				}
				this.onlyLaunchInactiveAccounts.selectedProperty().unbindBidirectional(old.onlyLaunchInactiveAccountsProperty());
				this.minimizeClients.selectedProperty().unbindBidirectional(old.minimizeClientsProperty());
				removeMiscUpdateListeners(old);
			}
			if (newv != null) {
				this.accounts.setItems(newv.getAccounts());
				for (AccountColumn col : AccountColumn.values()) {
					final SimpleBooleanProperty prop = newv.displayColumnProperty(col);
					final CustomCheckMenuItem item = this.columnItems.get(col);
					item.selectedProperty().bindBidirectional(prop);
				}
				this.onlyLaunchInactiveAccounts.selectedProperty().bindBidirectional(newv.onlyLaunchInactiveAccountsProperty());
				this.minimizeClients.selectedProperty().bindBidirectional(newv.minimizeClientsProperty());
				addMiscUpdateListeners(newv);
				this.lastListListener = e -> {
					this.launchButton.textProperty().unbind();
					final List<BooleanProperty> deps  = newv.getAccounts().stream().map(AccountConfiguration::selectedProperty).collect(Collectors.toList());
					deps.add(newv.scheduleLaunchProperty());
					this.launchButton.textProperty().bind(Bindings.createStringBinding(() -> {
						final String scheduled = newv.isScheduleLaunch() ? " - Scheduled" : "";
						final String selected = newv.getAccounts().stream().filter(AccountConfiguration::isSelected).count() + "/" + newv.getAccounts().size();
						return "Launch Selected Accounts" + scheduled + " (" + selected + ")";
					}, deps.toArray(new BooleanProperty[0])));
				};
				this.lastListListener.onChanged(null);
				newv.getAccounts().addListener(this.lastListListener);
				
				tryScanTribotPath(newv);
			}
		});
	}
	
	private void tryScanTribotPath(StarterConfiguration config) {
		if (!this.initialized) {
			return;
		}
		if (config.getCustomTribotPath().equals(new File("").getAbsolutePath()) || !new File(config.getCustomTribotPath()).isDirectory()) {
			final String prev = config.getCustomTribotPath();
			this.executor.submit(() -> {
				System.out.println("Scanning file system for tribot install directory");
				final long start = System.currentTimeMillis();
				final File f = this.scanner.findTribotInstallDirectory();
				System.out.println("Found tribot install: " + f);
				System.out.println("Scan took " + (System.currentTimeMillis() - start) + " ms");
				if (f != null && config.getCustomTribotPath().equals(prev)) {
					System.out.println("Setting tribot install setting to " + f);
					config.setCustomTribotPath(f.getAbsolutePath());
				}
			});	
		}
	}
	
	private void setupTabCounts() {
		this.accounts.itemsProperty().addListener((e, obs, newv) -> {
			this.accountsTab.textProperty().unbind();
			this.accountsTab.textProperty().bind(Bindings.createStringBinding(() -> {
				return newv.size() > 0 ? "Accounts (" + newv.size() + ")" : "Accounts";
			}, newv));
		});
		this.launchQueueTab.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.launchQueue.getItems().size() > 0 ? "Launch Queue (" + this.launchQueue.getItems().size() + ")" : "Launch Queue";
		}, this.launchQueue.getItems()));
		this.activeClientsTab.textProperty().bind(Bindings.createStringBinding(() -> {
			return this.activeClients.getItems().size() > 0 ? "Active Clients (" + this.activeClients.getItems().size() + ")" : "Active Clients";
		}, this.activeClients.getItems()));
	}
	
	private void launch(MouseEvent e) {
		if (e.getClickCount() != 1)
			return;
		if (this.model.get().getCustomTribotPath().trim().isEmpty() 
				|| this.model.get().getCustomTribotPath().trim().equals(FileUtil.getTribotDependenciesDirectory().getAbsolutePath())
				|| this.model.get().getCustomTribotPath().trim().equals(new File("").getAbsolutePath())
				|| !new File(this.model.get().getCustomTribotPath()).isDirectory()) {
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Missing TRiBot Installer Path");
			alert.setHeaderText("The TRiBot CLI must be invoked through the TRiBot installer's gradle launcher.");
			alert.setContentText("Please provide the path to your tribot installer folder (this will be a directory/folder, named TRiBot with a folder inside of it called tribot-gradle-launcher. You must set the path to be the TRiBot folder.). Visit Settings -> TRiBot.jar in the menu bar.");
			alert.showAndWait();
			return;
		}
		launch();
	}
	
	private void launch() {
		final StarterConfiguration config = this.model.get().copy();
		this.launcher.launchClients(config);
	}
	
	public void launch(String save, boolean closeAfter) {
		load(save);
		launch();
		if (closeAfter) {
			this.executor.scheduleWithFixedDelay(() -> {
				if (this.launcher.hasRemainingLaunches())
					return;
				System.exit(0);
			}, 1L, 1L, TimeUnit.SECONDS);
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
		FileUtil.saveSettings(this.lastSaveName.get(), this.model.get());
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
		FileUtil.saveSettings(save.getName(), this.model.get());
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
		this.undo.cacheAccounts();
		this.model.set(new StarterConfiguration());
		updated();
		this.outdated.set(false);
		this.lastSaveName.set(null);
	}
	
	@FXML
	public void exportAccountsCsv() {
		
		ExportUtil.exportAccounts(this.stage, 
				this.accounts.getItems(), 
				Arrays.stream(AccountColumn.values()).filter(c -> this.model.get().displayColumnProperty(c).get()).collect(Collectors.toList()),
				this.columns,
				ExportUtil.CSV
		);
	}
	
	@FXML
	public void exportAccountsTsv() {
		
		ExportUtil.exportAccounts(this.stage, 
				this.accounts.getItems(), 
				Arrays.stream(AccountColumn.values()).filter(c -> this.model.get().displayColumnProperty(c).get()).collect(Collectors.toList()),
				this.columns,
				ExportUtil.TSV);
	}
	
	@FXML
	public void exportAccountsCsvSelected() {
		
		ExportUtil.exportAccounts(this.stage, 
				this.accounts.getItems().stream().filter(s -> s.isSelected()).collect(Collectors.toList()), 
				Arrays.stream(AccountColumn.values()).filter(c -> this.model.get().displayColumnProperty(c).get()).collect(Collectors.toList()),
				this.columns,
				ExportUtil.CSV
		);
	}
	
	@FXML
	public void exportAccountsTsvSelected() {
		
		ExportUtil.exportAccounts(this.stage, 
				this.accounts.getItems().stream().filter(s -> s.isSelected()).collect(Collectors.toList()), 
				Arrays.stream(AccountColumn.values()).filter(c -> this.model.get().displayColumnProperty(c).get()).collect(Collectors.toList()),
				this.columns,
				ExportUtil.TSV
		);
	}
	
	@FXML
	public void viewSource() {
		LinkUtil.showLink("View Source", "Source Repository", SOURCE_REPO_PATH, this.stage, this::bindStyle);
	}
	
	@FXML
	public void viewThread() {
		LinkUtil.showLink("View Thread", "TRiBot Thread", THREAD_PATH, this.stage, this::bindStyle);
	}
	
	@FXML
	public void viewDownload() {
		LinkUtil.showLink("View Download", "Download", DOWNLOAD_PATH, this.stage, this::bindStyle);
	}
	
	@FXML
	public void viewUpdaterDownload() {
		LinkUtil.showLink("View Updater Download", "Download Updater", DOWNLOAD_UPDATER_PATH, this.stage, this::bindStyle);
	}
	
	@FXML
	public void colorSelectedAccounts() {
		PromptUtil.colorAccounts(this.stage, this::bindStyle, c -> {
			this.undo.cacheAccounts();
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
		final StarterConfiguration settings = FileUtil.readSettingsFile(open.getAbsolutePath());
		if (settings == null)
			return;
		this.undo.cacheAccounts();
		this.accounts.getItems().addAll(settings.getAccounts());
		this.updated();
	}
	
	@FXML
	public void configureLaunchSpeed() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/launch_speed.fxml")
		.withWindowName("Launch Speed")
		.withApplicationConfig(this.config)
		.<LaunchSpeedController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
	@FXML
	public void importFromTextFile() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/import.fxml")
		.withWindowName("Import Accounts")
		.withApplicationConfig(this.config)
		.<ImportController>onCreation((stage, controller) -> {
			controller.init(stage, accs -> {
				this.undo.cacheAccounts();
				this.accounts.getItems().addAll(accs);
				this.updated();
			}, this.config);
		})
		.build();
	}
	
	@FXML
	public void resetSelectedColors() {
		this.undo.cacheAccounts();
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
		.withApplicationConfig(this.config)
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
		.withApplicationConfig(this.config)
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
		.withWindowName("TRiBot Path")
		.withApplicationConfig(this.config)
		.<CustomJarController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
	@FXML
	public void configureWorldBlacklist() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/world_blacklist.fxml")
		.withWindowName("Random World Blacklist")
		.withApplicationConfig(this.config)
		.<WorldBlacklistController>onCreation((stage, controller) -> {
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
		.withApplicationConfig(this.config)
		.<TRiBotSignInController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
	@FXML
	public void displayScheduleLaunches() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/schedule.fxml")
		.withWindowName("Schedule Launches")
		.withApplicationConfig(this.config)
		.<ScheduleController>onCreation((stage, controller) -> {
			controller.init(stage, this.model);
		})
		.build();
	}
	
	@FXML
	public void sortByColorAsc() {
		this.undo.cacheAccounts();
		this.accounts.getItems().sort(COLOR_COMPARATOR);
		this.updated();
	}
	
	@FXML
	public void sortByColorDesc() {
		this.undo.cacheAccounts();
		this.accounts.getItems().sort(COLOR_COMPARATOR.reversed());
		this.updated();
	}
	
	@FXML
	public void showProxyManager() {
		new UIBuilder()
		.withParent(this.stage)
		.withFxml("/fxml/proxy_manager.fxml")
		.withWindowName("Proxy Manager")
		.withApplicationConfig(this.config)
		.<ProxyManagerController>onCreation((stage, controller) -> {
			controller.init(stage, this.config, this.tribotProxies);
		})
		.build();
	}
	
	private void setupTheme() {
		final ToggleGroup group = new ToggleGroup();
		for (Theme theme : Theme.values()) {
			final RadioMenuItem item = new RadioMenuItem(theme.toString());
			item.setToggleGroup(group);
			item.selectedProperty().addListener((obs, old, newv) -> {
				if (newv)
					this.config.setTheme(theme);
			});
			this.theme.getItems().add(item);
			item.setSelected(this.config.getTheme() == theme);
		}
	}
	
	private void addMiscUpdateListeners(StarterConfiguration config) {
		for (ObservableValue<?> obs : extractMiscObservables(config))
			obs.addListener(this.updateListener);
		config.worldBlacklist().addListener(this.listUpdateListener);
	}
	
	private void removeMiscUpdateListeners(StarterConfiguration config) {
		for (ObservableValue<?> obs : extractMiscObservables(config))
			obs.removeListener(this.updateListener);
		config.worldBlacklist().removeListener(this.listUpdateListener);
	}
	
	private List<ObservableValue<?>> extractMiscObservables(StarterConfiguration config) {
		final List<ObservableValue<?>> obs = new ArrayList<>();
		for (AccountColumn c : AccountColumn.values())
			obs.add(config.displayColumnProperty(c));
		//obs.add(config.customJavaPathProperty());
		//obs.add(config.useCustomJavaPathProperty());
		obs.add(config.lookingGlassPathProperty());
		obs.add(config.lookingGlassProperty());
		obs.add(config.loginProperty());
		obs.add(config.tribotUsernameProperty());
		obs.add(config.tribotPasswordProperty());
		obs.add(config.supplySidProperty());
		obs.add(config.sidProperty());
		obs.add(config.customTribotPathProperty());
		//obs.add(config.useCustomTribotPathProperty());
		obs.add(config.customLaunchDateProperty());
		obs.add(config.scheduleLaunchProperty());
		obs.add(config.useCustomLaunchDateProperty());
		obs.add(config.launchTimeProperty());
		obs.add(config.onlyLaunchInactiveAccountsProperty());
		obs.add(config.minimizeClientsProperty());
		return obs;
	}
	
	private void bindStyle(Scene scene) {
		final ChangeListener<Theme> themeListener = (obs, old, newv) -> {
			scene.getStylesheets().setAll(newv.getCss());
		};
		themeListener.changed(this.config.themeProperty(), null, this.config.getTheme());
		this.config.themeProperty().addListener(themeListener);
		scene.addEventHandler(WindowEvent.WINDOW_HIDDEN, e -> this.config.themeProperty().removeListener(themeListener));
	}
	
	private void setupProxies() {
		final ObservableList<ProxyDescriptor> observableTribotProxies = this.config.isIncludeTribotProxies()
				? FXCollections.observableArrayList(this.tribotProxies)
				: FXCollections.observableArrayList();
		this.proxies = FXUtil.merge(observableTribotProxies, this.config.proxies());
		this.config.includeTribotProxiesProperty().addListener((obs, old, newv) -> {
			if (newv)
				observableTribotProxies.setAll(this.tribotProxies);
			else
				observableTribotProxies.clear();
		});
	}
	
	private void setupActiveClients() {
		this.activeClientObserver = new ActiveClientObserver();
		this.activeClients.setItems(this.activeClientObserver.getActive());
		this.activeClients.setPlaceholder(new Text("No active clients"));
		this.activeClients.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		final ContextMenu cm = new ContextMenu();
		final MenuItem killSelected = new MenuItem("Shutdown Selected Clients");
		killSelected.setOnAction(e -> {
			System.out.println("Killing selected clients");
			this.activeClients.getSelectionModel().getSelectedItems().forEach(client -> {
				this.executor.submit(() -> {
					System.out.println("Killing client: " + client);
					client.getProcess().destroy();
				});
			});
		});
		final MenuItem killAll = new MenuItem("Shutdown All Clients");
		killAll.setOnAction(e -> {
			System.out.println("Killing all clients");
			this.activeClients.getItems().forEach(client -> {
				this.executor.submit(() -> {
					System.out.println("Killing client: " + client);
					client.getProcess().destroy();
				});
			});
		});
		cm.getItems().addAll(killSelected, killAll);
		this.activeClients.setContextMenu(cm);
	}
	
	private void setupLaunchQueue() {
		
		this.launcher = new LaunchProcessor(this.config, this.activeClientObserver);
		
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
			final CustomCheckMenuItem item = new CustomCheckMenuItem(col.toString(), 100);
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
			return this.columnItems.values().stream().filter(CustomCheckMenuItem::isSelected).count();
		}, this.columnItems.values().stream().map(CustomCheckMenuItem::selectedProperty).toArray(BooleanProperty[]::new));
		this.columnSelection.getItems().add(new SeparatorMenuItem());
		final MenuItem selectAll = new MenuItem("Select All");
		selectAll.setOnAction(e -> {
			this.columnItems.values().forEach(c -> c.setSelected(true));
		});
		final MenuItem deselectAll = new MenuItem("Deselect All");
		deselectAll.setOnAction(e -> {
			this.columnItems.values().forEach(c -> c.setSelected(false));
		});
		this.columnSelection.getItems().addAll(selectAll, deselectAll);
	}
	
	private void addColumn(AccountColumn col) {
		final TableColumn<AccountConfiguration, ?> column = this.columns.get(col);
		if (this.accounts.getColumns().contains(column))
			return;
		final int index = (int) (Arrays.stream(AccountColumn.values())
				.filter(c -> c.ordinal() < col.ordinal())
				.filter(c -> this.columnItems.get(c).isSelected())
				.count());
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
		final CommandLineConfig clConfig = GraphicalClientStarter.getConfig();
		if (clConfig.isDebug()) {
			this.console.setPlaceholder(new Text("Console disabled; output sent to standard out instead (launched with -debug)"));
			this.console.setDisable(true);
			return;
		}
		else if (clConfig.isCloseAfterLaunch())
			return;
		this.console.setPlaceholder(new Text("No messages to display"));
		final PrintStream ps = new PrintStream(new ConsoleOutputStream(this.console), false);
		System.setOut(ps);
		System.setErr(ps);
		this.console.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		final ContextMenu cm = new ContextMenu();
		final MenuItem copy = new MenuItem("Copy Selected");
		copy.setOnAction(e -> {
			final String s = this.console.getSelectionModel().getSelectedItems().stream().collect(Collectors.joining(System.lineSeparator()));
			ClipboardUtil.set(s);
		});
		copy.setAccelerator(KeyCombinations.COPY_KEY_COMBO);
		final MenuItem copyAll = new MenuItem("Copy All");
		copyAll.setOnAction(e -> {
			final String s = this.console.getItems().stream().collect(Collectors.joining(System.lineSeparator()));
			ClipboardUtil.set(s);
		});
		copyAll.setAccelerator(KeyCombinations.CTRL_SHIFT_C_COMBO);
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
		if (Platform.isFxApplicationThread())
			this.stage.hide();
		System.exit(0);
	}
	
	private void setupSelectionMode() {
		final ToggleGroup group = new ToggleGroup();
		final Map<starter.models.SelectionMode, RadioMenuItem> map = new HashMap<>();
		for (starter.models.SelectionMode mode : starter.models.SelectionMode.values()) {
			final RadioMenuItem item = new RadioMenuItem(mode.toString());
			item.setToggleGroup(group);
			map.put(mode, item);
			item.setSelected(this.config.getSelectionMode() == mode);
			this.selectionMode.getItems().add(item);
		}
		this.config.selectionModeProperty().bind(Bindings.createObjectBinding(() -> {
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
		listener.changed(this.config.selectionModeProperty(), null, this.config.getSelectionMode());
		this.config.selectionModeProperty().addListener(listener);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupAccountTable() {
		
		// might be able to replace this by just checking the context menu and if anything matches
		this.accounts.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (KeyCombinations.COPY_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.getSelectionMode()) {
				case CELL:
					copyCellsToClipboard();
					break;
				case ROW:
					ClipboardUtil.copyAccountsToClipboard(this.accounts.getSelectionModel().getSelectedItems());
					break;
				}
			}
			else if (KeyCombinations.PASTE_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.getSelectionMode()) {
				case CELL:
					pasteCellsFromClipboard();
					break;
				case ROW:
					pasteAccountFromClipboard();
					break;
				}
			}
			else if (KeyCombinations.D_CTRL_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.getSelectionMode()) {
				case CELL:
					deleteSelectedCells();
					break;
				case ROW:
					duplicateSelectedAccounts();
					break;
				}
			}
			else if (KeyCombinations.S_ALT_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					this.accounts.getSelectionModel().getSelectedItems().forEach(a -> {
						a.setSelected(true);
					});
					break;
				}
			}
			else if (KeyCombinations.D_ALT_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					this.accounts.getSelectionModel().getSelectedItems().forEach(a -> {
						a.setSelected(false);
					});
					break;
				}
			}
			else if (KeyCombinations.DELETE_KEY_COMBO.match(e)) {
				e.consume();
				switch (this.config.getSelectionMode()) {
				case CELL:
					deleteSelectedCells();
					break;
				case ROW:
					deleteSelectedAccounts();
					break;
				}
			}
			else if (KeyCombinations.N_CTRL_KEY_COMBO.match(e)) {
				e.consume();
				this.addNewAccount();
			}
			else if (KeyCombinations.CTRL_SHIFT_C_COMBO.match(e)) {
				e.consume();
				this.clearAccountTable();
			}
			else if (KeyCombinations.REDO.match(e)) {
				e.consume();
				this.undo.redoAccounts();
			}
			else if (KeyCombinations.UNDO.match(e)) {
				e.consume();
				this.undo.undoAccounts();
			}
			else {
				switch (e.getCode()) {
				case TAB: {
					selectNextCell(e.isShiftDown());
					e.consume();
					break;
				}
				default:
					if (e.getCode().isDigitKey() || e.getCode().isLetterKey()) {
						final TablePosition pos = this.accounts.getFocusModel().getFocusedCell();
						this.accounts.edit(pos.getRow(), pos.getTableColumn());
					}
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
			final TextFieldTableCell<AccountConfiguration> cell = new TextFieldTableCell<>(this.config);
			setCellTraversable(cell);
			createDefaultTableCellContextMenu(cell, col);
			return cell;
		});
		col.setOnEditCommit(e -> {
			this.undo.cacheAccounts();
			data.getCorresponding().setField(e.getRowValue(), e.getNewValue());
			updated();
			refreshAccounts();
		});
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set '" + data.getLabel() + "' for selected accounts");
		set.setOnAction(e -> {
			PromptUtil.promptUpdateSelected(data.getLabel(), this.stage, this::bindStyle,
					value -> {
						this.undo.cacheAccounts();
		    			this.accounts.getItems().stream()
		    				.filter(AccountConfiguration::isSelected)
		    				.forEach(a -> {
		    					data.getCorresponding().setField(a, value);
		    				});
		    			updated();
		    			refreshAccounts();
		    		});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private TableColumn<AccountConfiguration, Boolean> createSelectAccountTableColumn() {
		
		final CheckBox selectAll = new CheckBox();
		selectAll.selectedProperty().addListener((observable, old, newValue) -> {
			this.undo.cacheAccounts();
			this.accounts.getItems().forEach(acc -> acc.setSelected(newValue));
			updated();
		});

		final TableColumn<AccountConfiguration, Boolean> selected = new TableColumn<>();
		selected.setGraphic(selectAll);
		selected.setPrefWidth(CHECK_COL_WIDTH);

		selected.setSortable(false);
		selected.setEditable(true);
		selected.setCellFactory(lv -> {
			final CheckBoxTableCell<AccountConfiguration, Boolean> cell = new CheckBoxTableCell<>(this.config, index -> this.accounts.getItems().get(index).selectedProperty());
			this.createDefaultTableCellContextMenu(cell, selected);
			return cell;
		});
		
		selected.setCellValueFactory(new PropertyValueFactory<AccountConfiguration, Boolean>("selected"));
		
		final ContextMenu cm = new ContextMenu();
		
		final Menu selectRows = new Menu("Select Rows");
		
		final MenuItem selectRowsByIndex = new MenuItem("By Index");
		selectRowsByIndex.setOnAction(e -> {
			final Set<Integer> select = PromptUtil.promptRowsToSelectByIndex(this.stage, this::bindStyle);
			if (select == null)
				return;
			this.undo.cacheAccounts();
			select.forEach(index -> this.accounts.getItems().get(index).setSelected(true));
			this.updated();
		});
		selectRows.getItems().add(selectRowsByIndex);
		for (AccountColumn data : STRING_ACCOUNT_COLUMN_DATA) {
			final MenuItem item = new MenuItem("By " + data.getLabel());
			item.setOnAction(e -> {
				final String val = PromptUtil.promptRowsToSelect(data.getLabel(), this.stage, this::bindStyle);
				if (val == null)
					return;
				this.undo.cacheAccounts();
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
				final String val = PromptUtil.promptRowsToSelect(data.getLabel(), this.stage, this::bindStyle);
				if (val == null)
					return;
				this.undo.cacheAccounts();
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
			final Color c = PromptUtil.promptRowsToSelectByColor(this.stage, this::bindStyle);
			if (c == null)
				return;
			this.accounts.getItems().stream().filter(acc -> c.equals(acc.getColor())).forEach(acc -> acc.setSelected(true));
		});
		selectRows.getItems().add(selectRowsByColor);
		
		final Menu selectRowsOnly = new Menu("Select Only Rows");
		
		final MenuItem selectRowsOnlyByIndex = new MenuItem("By Index");
		selectRowsOnlyByIndex.setOnAction(e -> {
			final Set<Integer> select = PromptUtil.promptRowsToSelectByIndex(this.stage, this::bindStyle);
			if (select == null)
				return;
			this.undo.cacheAccounts();
			for (int i = 0; i < this.accounts.getItems().size(); i++)
				this.accounts.getItems().get(i).setSelected(select.contains(i));
			this.updated();
		});
		selectRowsOnly.getItems().add(selectRowsOnlyByIndex);
		
		for (AccountColumn data : STRING_ACCOUNT_COLUMN_DATA) {
			final MenuItem item = new MenuItem("By " + data.getLabel());
			item.setOnAction(e -> {
				final String val = PromptUtil.promptRowsToSelect(data.getLabel(), this.stage, this::bindStyle);
				if (val == null)
					return;
				this.undo.cacheAccounts();
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
				final String val = PromptUtil.promptRowsToSelect(data.getLabel(), this.stage, this::bindStyle);
				if (val == null)
					return;
				this.undo.cacheAccounts();
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
			final Color c = PromptUtil.promptRowsToSelectByColor(this.stage, this::bindStyle);
			if (c == null)
				return;
			this.accounts.getItems().stream().forEach(acc -> acc.setSelected(c.equals(acc.getColor())));
		});
		selectRowsOnly.getItems().add(selectRowsOnlyByColor);
		
		cm.getItems().addAll(selectRows, selectRowsOnly);
		
		selected.setContextMenu(cm);
		
		return selected;
	}
	
	private <T> ContextMenu createDefaultTableContextMenu() {

		final ContextMenu cm = new ContextMenu();

		final MenuItem newAcc = new MenuItem("New");
		newAcc.setOnAction(e -> {
			this.addNewAccount();
		});
		newAcc.setAccelerator(KeyCombinations.N_CTRL_KEY_COMBO);
		
		final MenuItem undo = new MenuItem("Undo");
		undo.setOnAction(e -> {
			this.undo.undoAccounts();
			this.updated();
		});
		undo.disableProperty().bind(this.undo.undoEmptyBinding());
		undo.setAccelerator(KeyCombinations.UNDO);

		final MenuItem redo = new MenuItem("Redo");
		redo.setOnAction(e -> {
			this.undo.redoAccounts();
			this.updated();
		});
		redo.disableProperty().bind(this.undo.redoEmptyBinding());
		redo.setAccelerator(KeyCombinations.REDO);
		
		final MenuItem clear = new MenuItem("Clear");
		clear.setOnAction(e -> {
			this.clearAccountTable();
		});
		clear.setAccelerator(KeyCombinations.CTRL_SHIFT_C_COMBO);

		cm.getItems().addAll(newAcc, new SeparatorMenuItem(), clear, new SeparatorMenuItem(), undo, redo);

		return cm;
	}
	
	private <T> void createDefaultTableCellContextMenu(TableCell<AccountConfiguration, T> cell, TableColumn<AccountConfiguration, T> col) {
		cell.contextMenuProperty().bind(Bindings.when(cell.emptyProperty())
				.then((ContextMenu)null)
				.otherwise(Bindings.when(this.config.selectionModeProperty()
				.isEqualTo(starter.models.SelectionMode.CELL))
				.then(createDefaultTableCellContextMenu(cell, col, starter.models.SelectionMode.CELL))
				.otherwise(createDefaultTableCellContextMenu(cell, col, starter.models.SelectionMode.ROW))));
	}
	
	private <T> ContextMenu createDefaultTableCellContextMenu(TableCell<AccountConfiguration, T> cell, TableColumn<AccountConfiguration, T> col,
			starter.models.SelectionMode mode) {
		
		final ContextMenu cm = createDefaultTableContextMenu();
		
		final List<MenuItem> defaultItems = new ArrayList<>(cm.getItems());
		
		final MenuItem edit = new MenuItem("Edit Cell");
		edit.setOnAction(e -> {
			this.accounts.edit(cell.getIndex(), col);
		});
		edit.disableProperty().bind(Bindings.createBooleanBinding(() -> {
			final TableRow<?> row = cell.getTableRow();
			return row == null || row.getItem() == null;
		}, cell.tableRowProperty()));
		
		switch (mode) {
		case ROW:
			final MenuItem duplicate = new MenuItem();
			duplicate.textProperty().bind(Bindings.createStringBinding(() -> {
				return this.accounts.getSelectionModel().getSelectedItems().size() > 1
						? "Duplicate Rows"
						: "Duplicate Row";
			}, this.accounts.getSelectionModel().selectedItemProperty()));
			duplicate.setOnAction(e -> {
				duplicateSelectedAccounts();
			});
			duplicate.setAccelerator(KeyCombinations.D_CTRL_KEY_COMBO);
			duplicate.disableProperty().bind(cell.itemProperty().isNull());
			
			final MenuItem selectedHighlighted = new MenuItem();
			selectedHighlighted.textProperty().bind(Bindings.createStringBinding(() -> {
				return this.accounts.getSelectionModel().getSelectedItems().size() > 1
						? "Select Rows"
						: "Select Row";
			}, this.accounts.getSelectionModel().selectedItemProperty()));
			selectedHighlighted.setOnAction(e -> {
				this.accounts.getSelectionModel().getSelectedItems().forEach(a -> {
					a.setSelected(true);
				});
			});
			selectedHighlighted.setAccelerator(KeyCombinations.S_ALT_KEY_COMBO);
			selectedHighlighted.disableProperty().bind(cell.itemProperty().isNull());
			
			final MenuItem unselectedHighlighted = new MenuItem();
			unselectedHighlighted.textProperty().bind(Bindings.createStringBinding(() -> {
				return this.accounts.getSelectionModel().getSelectedItems().size() > 1
						? "Deselect Rows"
						: "Deselect Row";
			}, this.accounts.getSelectionModel().selectedItemProperty()));
			unselectedHighlighted.setOnAction(e -> {
				this.accounts.getSelectionModel().getSelectedItems().forEach(a -> {
					a.setSelected(false);
				});
			});
			unselectedHighlighted.setAccelerator(KeyCombinations.D_ALT_KEY_COMBO);
			unselectedHighlighted.disableProperty().bind(cell.itemProperty().isNull());
			
			final MenuItem delete = new MenuItem();
			delete.textProperty().bind(Bindings.createStringBinding(() -> {
				return this.accounts.getSelectionModel().getSelectedItems().size() > 1
						? "Delete Rows"
						: "Delete Row";
			}, this.accounts.getSelectionModel().selectedItemProperty()));
			delete.setOnAction(e -> {
				deleteSelectedAccounts();
			});
			delete.setAccelerator(KeyCombinations.DELETE_KEY_COMBO);
			
			delete.disableProperty().bind(cell.itemProperty().isNull());
			
			final MenuItem copyRows = new MenuItem();
			copyRows.textProperty().bind(Bindings.createStringBinding(() -> {
				return this.accounts.getSelectionModel().getSelectedItems().size() > 1
						? "Copy Rows"
						: "Copy Row";
			}, this.accounts.getSelectionModel().selectedItemProperty()));
			copyRows.setOnAction(e -> {
				ClipboardUtil.copyAccountsToClipboard(this.accounts.getSelectionModel().getSelectedItems());
			});
			copyRows.disableProperty().bind(cell.itemProperty().isNull());
			copyRows.setAccelerator(KeyCombinations.COPY_KEY_COMBO);
			
			final MenuItem pasteRows =  new MenuItem("Paste Row(s)");
			pasteRows.setOnAction(e -> {
				pasteAccountFromClipboard();
			});
			pasteRows.setAccelerator(KeyCombinations.PASTE_KEY_COMBO);
			// these accelerators don't directly get triggered but the table has event handlers to handle them,
			// so they exist to notify the user of the shortcuts
			cm.getItems().clear();
			cm.getItems().addAll(edit, new SeparatorMenuItem(), copyRows, pasteRows, new SeparatorMenuItem(), selectedHighlighted, unselectedHighlighted, new SeparatorMenuItem(), duplicate, delete, new SeparatorMenuItem());
			cm.getItems().addAll(defaultItems);
			break;
		case CELL:
			final MenuItem copyCells = new MenuItem();
			copyCells.textProperty().bind(Bindings.createStringBinding(() -> {
				return this.accounts.getSelectionModel().getSelectedCells().size() > 1
						? "Copy Cells"
						: "Copy Cell";
			}, this.accounts.getSelectionModel().selectedItemProperty()));
			copyCells.setOnAction(e -> {
				copyCellsToClipboard();
			});
			copyCells.disableProperty().bind(cell.itemProperty().isNull());
			copyCells.setAccelerator(KeyCombinations.COPY_KEY_COMBO);
			
			final MenuItem pasteCells =  new MenuItem("Paste Cell(s)");
			pasteCells.setOnAction(e -> {
				pasteCellsFromClipboard();
			});
			pasteCells.setAccelerator(KeyCombinations.PASTE_KEY_COMBO);
			// these accelerators don't directly get triggered but the table has event handlers to handle them,
			// so they exist to notify the user of the shortcuts
			final MenuItem deleteCells = new MenuItem();
			deleteCells.textProperty().bind(Bindings.createStringBinding(() -> {
				return this.accounts.getSelectionModel().getSelectedCells().size() > 1
						? "Delete Cells"
						: "Delete Cell";
			}, this.accounts.getSelectionModel().selectedItemProperty()));
			deleteCells.setOnAction(e -> {
				deleteSelectedCells();
			});
			deleteCells.setAccelerator(KeyCombinations.D_CTRL_KEY_COMBO);
			
			deleteCells.disableProperty().bind(cell.itemProperty().isNull());
			cm.getItems().clear();
			cm.getItems().addAll(edit, new SeparatorMenuItem(), copyCells, pasteCells, new SeparatorMenuItem(), deleteCells, new SeparatorMenuItem());
			cm.getItems().addAll(defaultItems);
			break;
		}
		
		return cm;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
			copy += getColumnType(pos.getTableColumn()).getCopyText(pos);
		}
		ClipboardUtil.set(copy);
	}
	
	private AccountColumn getColumnType(TableColumn<AccountConfiguration, ?> col) {
		return this.columns.entrySet()
				.stream()
				.filter(e -> e.getValue() == col)
				.findFirst()
				.map(Map.Entry::getKey)
				.orElseThrow(IllegalArgumentException::new);
	}
	
	@SuppressWarnings("unchecked")
	private void deleteSelectedCells() {
		this.undo.cacheAccounts();
		this.accounts.getSelectionModel().getSelectedCells().forEach(pos -> {
			this.getColumnType(pos.getTableColumn()).setField(this.accounts.getItems().get(pos.getRow()), "");
		});
		refreshAccounts();
		this.updated();
	}
	
	private void deleteSelectedAccounts() {
		final List<AccountConfiguration> accs = this.accounts.getSelectionModel().getSelectedItems();
		if (accs.size() == 0)
			return;
		if (!this.confirmRemoval(accs.size()))
			return;
		this.undo.cacheAccounts();
		this.accounts.getItems().removeAll(accs);
		this.updated();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void pasteCellsFromClipboard() {
		
		final List<TablePosition> selected = this.accounts.getSelectionModel().getSelectedCells();
		if (selected.size() == 0)
			return;
		
		final TablePosition last = selected.get(0);
		final int row = last.getRow();
		final int col = last.getColumn();
		
		final String copied = ClipboardUtil.getText();
		if (copied == null)
			return;
		
		this.undo.cacheAccounts();
		
		final AccountColumn[] sorted = this.columns.entrySet()
				.stream()
				.filter(e -> this.accounts.getColumns().contains(e.getValue()))
				.sorted(Comparator.comparingInt(e -> this.accounts.getColumns().indexOf(e.getValue())))
				.map(Map.Entry::getKey)
				.toArray(AccountColumn[]::new);
		
		final String[] lines = copied.split(Pattern.quote(System.lineSeparator()), -1);
		
		// check for special case, pasting one cell to many cells
		if (lines.length == 1) {
			final String[] cells = lines[0].split(Pattern.quote("\t"), -1);
			if (cells.length == 1) {
				final String value = cells[0];
				selected.forEach(pos -> {
					this.getColumnType(pos.getTableColumn()).setField(this.accounts.getItems().get(pos.getRow()), value);
				});
				refreshAccounts();
				this.updated();
				return;
			}
		}
		
		int rows = 0;
		int cols = 0;
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
		refreshAccounts();
		this.updated();
	}
	
	private void pasteAccountFromClipboard() {
		
		final AccountConfiguration[] accs = ClipboardUtil.grabFromClipboard(AccountConfiguration[].class);
		if (accs == null)
			return;
		
		final AccountConfiguration acc = this.accounts.getSelectionModel().getSelectedItem();
		final int index = acc != null ? this.accounts.getItems().indexOf(acc) : this.accounts.getItems().size() - 1;
		
		this.undo.cacheAccounts();
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
		this.undo.cacheAccounts();
		this.accounts.getItems().addAll(index + 1, accs);
		this.updated();
		this.accounts.getSelectionModel().clearSelection();
		this.accounts.getSelectionModel().selectRange(index + 1, index + 1 + accs.size());
	}
	
	private void setAccountRowFactory() {
		
		this.accounts.setRowFactory(t -> {
			
			final TableRow<AccountConfiguration> row = new TableRow<>();
			
			row.setEditable(true);
			
			row.itemProperty().addListener((obs, old, newv) -> {
				row.styleProperty().unbind();
				if (newv != null) {
					row.styleProperty().bind(Bindings.createStringBinding(() -> {
								final Color color = newv.getColor();
								if (color == null)
									return "";
								if (this.accounts.getSelectionModel().getSelectedItems().contains(newv)) {
									return "-fx-selection-bar: " + FXUtil.colorToCssRgb(color.brighter())
										+ "-fx-selection-bar-non-focused: " + FXUtil.colorToCssRgb(color.darker());
								}
								return "-fx-background-color : " + FXUtil.colorToCssRgb(color);
							}, newv.colorProperty(), this.accounts.getSelectionModel().selectedItemProperty()));
				}
				else
					row.setStyle("");
			});

			row.setOnDragDetected(e -> {
				switch (this.config.getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					if (row.isEmpty())
						return;
					if (e.getButton() != MouseButton.PRIMARY)
						return;
					if (e.isControlDown()) {
						// multi select
						row.startFullDrag();
						row.setUserData(this.accounts.getSelectionModel().getSelectedIndices().stream().mapToInt(Integer::intValue).toArray());
						e.consume();
						break;
					}
					row.setUserData(null);
					final Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					final ClipboardContent cc = new ClipboardContent();
					final List<Integer> list = this.accounts.getSelectionModel().getSelectedIndices().stream().collect(Collectors.toList());
					list.add(0, row.getIndex());
					final int[] indexes = list.stream().distinct().mapToInt(Integer::intValue).toArray();
					cc.put(SERIALIZED_MIME_TYPE, indexes);
					db.setContent(cc);
					e.consume();
					break;
				}
			});
			
			row.setOnMouseDragEntered(event -> {
				switch (this.config.getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					final TableRow<?> sourceRow = (TableRow<?>) event.getGestureSource();
					if (sourceRow.getUserData() instanceof int[]) {
						final int[] prev = (int[]) sourceRow.getUserData();
						final int start = sourceRow.getIndex();
						final int current = row.getIndex();
						final int lower = Math.min(start, current);
						final int higher = Math.max(start, current);
						this.accounts.getSelectionModel().getSelectedIndices().stream().collect(Collectors.toList())
						.forEach(i -> {
							if (i < lower || i > higher) {
								for (int prevIndex = 0; prevIndex < prev.length; prevIndex++) {
									if (prev[prevIndex] == i) {
										return;
									}
								}
								this.accounts.getSelectionModel().clearSelection(i);
							}
						});
						this.accounts.getSelectionModel().selectRange(lower, higher + 1);
						return;
					}
					break;
				}
			});

			row.setOnDragOver(event -> {
				switch (this.config.getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					final TableRow<?> sourceRow = (TableRow<?>) event.getGestureSource();
					if (sourceRow.getUserData() instanceof int[]) {
						return;
					}
					final Dragboard db = event.getDragboard();
					if (db.hasContent(SERIALIZED_MIME_TYPE)) {
						final int[] content = (int[]) db.getContent(SERIALIZED_MIME_TYPE);
						if (content.length > 0 && row.getIndex() != content[0]) {
							event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
							event.consume();
						}
					}
					break;
				}
			});

			row.setOnDragDropped(event -> {
				switch (this.config.getSelectionMode()) {
				case CELL:
					break;
				case ROW:
					
					final TableRow<?> sourceRow = (TableRow<?>) event.getGestureSource();
					if (sourceRow.getUserData() instanceof int[]) {
						row.setUserData(null);
						return;
					}
					
					final Dragboard db = event.getDragboard();
					if (db.hasContent(SERIALIZED_MIME_TYPE)) {

						final int dropIndex = row.getIndex();
						if (dropIndex >= this.accounts.getItems().size())
							return;
						
						final int[] indexes = (int[]) db.getContent(SERIALIZED_MIME_TYPE);
						if (indexes.length == 0) {
							return;
						}
						
						final int change = dropIndex - indexes[0];
						
						Arrays.sort(indexes);
						
						final List<AccountConfiguration> changes = new ArrayList<>();
						
						final Map<Integer, AccountConfiguration> updates = new HashMap<>();
						
						for (int index : indexes) {
							final AccountConfiguration draggedItem = this.accounts.getItems().get(index);
							updates.put(index, draggedItem);
						}
					
						for (int index : indexes) {
							final AccountConfiguration draggedItem = updates.get(index);
							this.accounts.getItems().remove(draggedItem);
						}
						
						int under = 0;
						for (int index : indexes) {
							final AccountConfiguration draggedItem = updates.get(index);
							int newIndex = index + change;
							if (newIndex < under) {
								newIndex = under;
								under++;
							}
							if (newIndex > this.accounts.getItems().size()) {
								newIndex = this.accounts.getItems().size();
							}
							changes.add(draggedItem);
							this.accounts.getItems().add(newIndex, draggedItem);	
						}

						event.setDropCompleted(true);
						this.accounts.getSelectionModel().clearSelection();
						for (AccountConfiguration newIndex : changes) {
							this.accounts.getSelectionModel().select(newIndex);
						}
						
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
		col.setCellFactory(lv -> {
			final CheckBoxTableCell<AccountConfiguration, Boolean> cell = new CheckBoxTableCell<>(this.config, index -> this.accounts.getItems().get(index).useProxyProperty());
			this.createDefaultTableCellContextMenu(cell, col);
			return cell;
		});
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
				this.undo.cacheAccounts();
				this.accounts.getItems().stream()
						.filter(AccountConfiguration::isSelected)
						.forEach(a -> a.setUseProxy(type == enable));
				updated();
				refreshAccounts();
			});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private TableColumn<AccountConfiguration, ProxyDescriptor> createProxyTableColumn() {
		final TableColumn<AccountConfiguration, ProxyDescriptor> col = getBasePropertyColumn("Proxy", "proxy");
		col.setCellFactory(lv -> {
			final ObservableList<ProxyDescriptor> none = FXCollections.observableArrayList(ProxyDescriptor.NO_PROXY);
			final ObservableList<ProxyDescriptor> merged = FXUtil.merge(this.proxies, none);
			final ComboBoxTableCell<AccountConfiguration, ProxyDescriptor> cell = new ComboBoxTableCell<>(this.config, merged.toArray(new ProxyDescriptor[0]));
			createDefaultTableCellContextMenu(cell, col);
			return cell;
		});
		col.setOnEditCommit(e -> {
			this.undo.cacheAccounts();
			final ProxyDescriptor actual = e.getNewValue() == ProxyDescriptor.NO_PROXY
					? null
					: e.getNewValue();
			e.getRowValue().setProxy(actual);
			this.updated();
			refreshAccounts();
		});
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set 'Proxy' for selected accounts");
		set.setOnAction(e -> {
			final ObservableList<ProxyDescriptor> none = FXCollections.observableArrayList(ProxyDescriptor.NO_PROXY);
			final ObservableList<ProxyDescriptor> merged = FXUtil.merge(this.proxies, none);
    		final ChoiceDialog<ProxyDescriptor> dialog = new ChoiceDialog<>(null, merged);
    		this.bindStyle(dialog.getDialogPane().getScene());
    		dialog.setTitle("Set Proxy");
    		dialog.setHeaderText("Set 'Proxy' for selected accounts");
    		dialog.setContentText("Select Proxy");
    		dialog.initOwner(this.stage);
    		dialog.showAndWait().ifPresent(value -> {
    			final ProxyDescriptor actual = value == ProxyDescriptor.NO_PROXY
						    					? null
						    					: value;
    			this.undo.cacheAccounts();
    			this.accounts.getItems().stream()
    				.filter(AccountConfiguration::isSelected)
    				.forEach(a -> ReflectionUtil.setValue(a, "proxy", actual, ProxyDescriptor.class));
    			updated();
    			refreshAccounts();
    		});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private <T> TableColumn<AccountConfiguration, T> getBaseColumn(String label) {
		final TableColumn<AccountConfiguration, T> col = new TableColumn<>(label);
		final SimpleDoubleProperty widthProperty = new SimpleDoubleProperty(0);
		final AtomicBoolean appliedListener = new AtomicBoolean(false);
		this.accounts.getChildrenUnmodifiable().addListener((Change<?> c) -> {
			if (appliedListener.get()) {
				return;
			}
			final ScrollBar s = this.accounts.lookupAll(".scroll-bar").stream()
			.map(node -> (ScrollBar) node)
			.filter(scroll -> scroll.getOrientation() == Orientation.VERTICAL)
			.findFirst()
			.orElse(null);
			if (s != null) {
				appliedListener.set(true);
				widthProperty.bind(Bindings.when(s.visibleProperty()).then(s.widthProperty()).otherwise(0D));
			}
		});
		final IntegerExpression colWidth = Bindings.createIntegerBinding(() -> {
			return this.columnItems.get(AccountColumn.SELECTED).selectedProperty().get() ? CHECK_COL_WIDTH : 0;
		}, this.columnCount, this.model);
		final LongExpression colCount = Bindings.createLongBinding(() -> {
			return this.columnItems.get(AccountColumn.SELECTED).selectedProperty().get() ? this.columnCount.get() - 1 : this.columnCount.get();
		}, this.columnCount, this.model);
		col.prefWidthProperty().bind(this.accounts.widthProperty().subtract(colWidth).subtract(widthProperty).subtract(colCount.add(1)).divide(colCount));
		col.setMaxWidth(Double.MAX_VALUE);
		col.setMinWidth(110);
		col.setEditable(true);
		return col;
	}
	
	private <T> TableColumn<AccountConfiguration, T> getBasePropertyColumn(String label, String fieldName) {
		final TableColumn<AccountConfiguration, T> col = getBaseColumn(label);
		col.setCellValueFactory(new PropertyValueFactory<AccountConfiguration, T>(fieldName));
		return col;
	}
	
	private void setCellTraversable(TextFieldTableCell<?> cell) {
		cell.getTextField().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.TAB) {
				e.consume();
				if (cell.isEditing()) {
					cell.commitEdit(cell.getTextField().getText());
					this.accounts.requestFocus();
					this.selectNextCell(false);
				}
			}
		});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void selectNextCell(boolean backwards) {
		switch (this.config.getSelectionMode()) {
		case CELL:
			final List<TablePosition> selected = this.accounts.getSelectionModel().getSelectedCells();
			if (backwards) {
				this.accounts.getSelectionModel().selectPrevious();
			} 
			else {
				this.accounts.getSelectionModel().selectNext();
			}
			for (TablePosition pos : selected)
				this.accounts.getSelectionModel().clearSelection(pos.getRow(), pos.getTableColumn());
			break;
		case ROW:
			final List<Integer> selectedRows = this.accounts.getSelectionModel().getSelectedIndices();
			if (backwards) {
				this.accounts.getSelectionModel().selectPrevious();
			} 
			else {
				this.accounts.getSelectionModel().selectNext();
			}
			for (Integer pos : selectedRows)
				this.accounts.getSelectionModel().clearSelection(pos);
			break;
		}
	}

	private TableColumn<AccountConfiguration, String> createAccountTableColumn(AccountColumn data) {
		final TableColumn<AccountConfiguration, String> col = getBasePropertyColumn(data.getLabel(), data.getFieldName());
		col.setCellFactory(s -> {
			final TextFieldTableCell<AccountConfiguration> cell = new TextFieldTableCell<>(this.config);
			setCellTraversable(cell);
			createDefaultTableCellContextMenu(cell, col);
			return cell;
		});
		col.setOnEditCommit(e -> {
			this.undo.cacheAccounts();
			ReflectionUtil.setValue(e.getRowValue(), data.getFieldName(), e.getNewValue());
			updated();
			refreshAccounts();
		});
		final ContextMenu cm = new ContextMenu();
		final MenuItem set = new MenuItem("Set '" + data.getLabel() + "' for selected accounts");
		set.setOnAction(e -> {
			PromptUtil.promptUpdateSelected(data.getLabel(), this.stage, this::bindStyle, value -> {
    			this.undo.cacheAccounts();
    			this.accounts.getItems().stream()
    				.filter(AccountConfiguration::isSelected)
    				.forEach(a -> ReflectionUtil.setValue(a, data.getFieldName(), value));
    			updated();
    			refreshAccounts();
			});
		});
		cm.getItems().add(set);
		col.setContextMenu(cm);
		return col;
	}
	
	private void updated() {
		if (this.config.isAutoSaveLast())
			FileUtil.saveSettings(LAST, this.model.get());
		this.outdated.set(true);
	}
	
	private void load(String name) {
		final StarterConfiguration config = FileUtil.readSettingsFile(name);
		if (config == null)
			return;
		this.undo.cacheAccounts();
		this.model.set(config);
		updated();
		this.outdated.set(false);
		if (!name.equals(LAST))
			this.lastSaveName.set(name);
	}

	private void refreshAccounts() {
		// this is a memory-expensive operation, immediately clean up old cells
		this.accounts.refresh();
		this.executor.submit(() -> System.gc());
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
		if (this.config.isDontShowSaveConfirm())
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
		this.config.setDontShowSaveConfirm(dontAskAgain.isSelected());
	}
	
	private boolean confirmExit() {
		if (this.config.isDontShowExitConfirm())
			return true;
		this.stage.setIconified(false);
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		this.bindStyle(alert.getDialogPane().getScene());
		alert.setTitle("Confirm Exit");
		alert.setHeaderText("Do you want to exit the application?");
		alert.initOwner(this.stage);
		final CheckBox dontAskAgain = new CheckBox("Don't ask again");
		alert.getDialogPane().setContent(dontAskAgain);
		alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
		final boolean result = alert.showAndWait().get() == ButtonType.YES;
		this.config.setDontShowExitConfirm(dontAskAgain.isSelected());
		return result;
	}

}
