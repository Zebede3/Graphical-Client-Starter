package starter.models;

import java.util.Map;
import java.util.Objects;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import starter.util.FileUtil;
import starter.util.AccountImportParser.AccountImportField;

/**
 * This information persists across save files
 *
 */
public class ApplicationConfiguration {

	private final SimpleBooleanProperty dontShowExitConfirm = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty dontShowSaveConfirm = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty dontShowDoubleLaunchWarning = new SimpleBooleanProperty(false);
	
	private final SimpleBooleanProperty autoSaveLast = new SimpleBooleanProperty(true);
	
	private final SimpleObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.DEFAULT);
	
	private final SimpleDoubleProperty widthProperty = new SimpleDoubleProperty(Double.NaN);
	private final SimpleDoubleProperty heightProperty = new SimpleDoubleProperty(Double.NaN);
	
	private final SimpleBooleanProperty maximizedProperty = new SimpleBooleanProperty(false);
	
	private final SimpleDoubleProperty xProperty = new SimpleDoubleProperty(Double.NaN);
	private final SimpleDoubleProperty yProperty = new SimpleDoubleProperty(Double.NaN);
	
	private final SimpleObjectProperty<SelectionMode> selectionModeProperty = new SimpleObjectProperty<>(SelectionMode.ROW);
	
	private final SimpleBooleanProperty debugMode = new SimpleBooleanProperty(false);
	
	private final ObservableList<ProxyDescriptor> proxies = FXCollections.observableArrayList();
	private final SimpleBooleanProperty includeTribotProxies = new SimpleBooleanProperty(true);
	
	private final SimpleStringProperty lastImportPattern = new SimpleStringProperty("${username}:${password}");
	private final SimpleStringProperty lastImportFile = new SimpleStringProperty(FileUtil.getDirectory().getAbsolutePath());
	private final SimpleObjectProperty<Map<AccountImportField, String>> lastImportDefaults = new SimpleObjectProperty<>(null);
	
	private final SimpleBooleanProperty showTribotImportAutocomplete = new SimpleBooleanProperty(false);
	
	public boolean isAutoSaveLast() {
		return this.autoSaveLast.get();
	}
	
	public void setAutoSaveLast(boolean autoSave) {
		this.autoSaveLast.set(autoSave);
	}
	
	public boolean isDontShowDoubleLaunchWarning() {
		return this.dontShowDoubleLaunchWarning.get();
	}
	
	public void setDontShowDoubleLaunchWarning(boolean dontShow) {
		this.dontShowDoubleLaunchWarning.set(dontShow);
	}
	
	public boolean isDontShowExitConfirm() {
		return this.dontShowExitConfirm.get();
	}
	
	public void setDontShowExitConfirm(boolean dontShow) {
		this.dontShowExitConfirm.set(dontShow);
	}
	
	public boolean isDontShowSaveConfirm() {
		return this.dontShowSaveConfirm.get();
	}
	
	public void setDontShowSaveConfirm(boolean dontShow) {
		this.dontShowSaveConfirm.set(dontShow);
	}
	
	public Theme getTheme() {
		// backwards compatibility stuff 10/15
		final Theme theme = this.theme.get();
		if (theme == null) {
			this.theme.set(Theme.DEFAULT);
			return Theme.DEFAULT;
		}
		return theme;
	}
	
	public void setTheme(Theme theme) {
		this.theme.set(Objects.requireNonNull(theme));
	}
	
	public SimpleObjectProperty<Theme> themeProperty() {
		return this.theme;
	}
	
	public SelectionMode getSelectionMode() {
		// backwards compatibility stuff 10/15
		final SelectionMode selectionMode = this.selectionModeProperty.get();
		if (selectionMode == null) {
			this.selectionModeProperty.set(SelectionMode.ROW);
			return SelectionMode.ROW;
		}
		return selectionMode;
	}
	
	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionModeProperty.set(Objects.requireNonNull(selectionMode));
	}
	
	public SimpleObjectProperty<SelectionMode> selectionModeProperty() {
		return this.selectionModeProperty;
	}
	
	public double getWidth() {
		return this.widthProperty.get();
	}
	
	public void setWidth(double width) {
		this.widthProperty.set(width);
	}
	
	public SimpleDoubleProperty widthProperty() {
		return this.widthProperty;
	}
	
	public double getHeight() {
		return this.heightProperty.get();
	}
	
	public void setHeight(double height) {
		this.heightProperty.set(height);
	}
	
	public SimpleDoubleProperty heightProperty() {
		return this.heightProperty;
	}

	public double getX() {
		return this.xProperty.get();
	}
	
	public void setX(double x) {
		this.xProperty.set(x);
	}
	
	public SimpleDoubleProperty xProperty() {
		return this.xProperty;
	}
	
	public double getY() {
		return this.yProperty.get();
	}
	
	public void setY(double y) {
		this.yProperty.set(y);
	}
	
	public SimpleDoubleProperty yProperty() {
		return this.yProperty;
	}
	
	public void runOnChange(Runnable run) {
		addListeners(run, this.dontShowExitConfirm, this.dontShowSaveConfirm, this.autoSaveLast,
				this.theme, this.widthProperty, this.heightProperty, this.xProperty, this.yProperty,
				this.selectionModeProperty, this.debugMode, this.includeTribotProxies, this.lastImportDefaults, 
				this.lastImportFile, this.lastImportPattern, this.dontShowDoubleLaunchWarning, this.showTribotImportAutocomplete);
		this.proxies.addListener((Change<?> change) -> run.run());
	}
	
	public String getLastImportFile() {
		return this.lastImportFile.get();
	}
	
	public void setLastImportFile(String file) {
		this.lastImportFile.set(file);
	}
	
	public void setLastImportPattern(String pattern) {
		this.lastImportPattern.set(pattern);
	}
	
	public String getLastImportPattern() {
		return this.lastImportPattern.get();
	}
	
	public Map<AccountImportField, String> getLastImportDefaults() {
		return this.lastImportDefaults.get();
	}
	
	public void setLastImportDefaults(Map<AccountImportField, String> lastDefaults) {
		this.lastImportDefaults.set(lastDefaults);
	}
	
	public boolean isDebugMode() {
		return this.debugMode.get();
	}
	
	public void setDebugMode(boolean enable) {
		this.debugMode.set(enable);
	}
	
	public SimpleBooleanProperty debugModeProperty() {
		return this.debugMode;
	}

	public SimpleBooleanProperty autoSaveLastProperty() {
		return this.autoSaveLast;
	}

	public SimpleBooleanProperty includeTribotProxiesProperty() {
		return this.includeTribotProxies;
	}
	
	public boolean isIncludeTribotProxies() {
		return this.includeTribotProxies.get();
	}
	
	public void setIncludeTribotProxies(boolean include) {
		this.includeTribotProxies.set(include);
	}
	
	public ObservableList<ProxyDescriptor> proxies() {
		return this.proxies;
	}
	
	public boolean isMaximized() {
		return this.maximizedProperty.get();
	}
	
	public void setMaximized(boolean maximized) {
		this.maximizedProperty.set(maximized);
	}
	
	public boolean isShowTribotImportAutocomplete() {
		return this.showTribotImportAutocomplete.get();
	}
	
	public void setShowTribotAutocomplete(boolean show) {
		this.showTribotImportAutocomplete.set(show);
	}
	
	public SimpleBooleanProperty showTribotImportAutocompleteProperty() {
		return this.showTribotImportAutocomplete;
	}
	
	private void addListeners(Runnable run, ObservableValue<?>... obs) {
		for (ObservableValue<?> ob : obs)
			ob.addListener((o, old, newv) -> run.run());
	}
	
}
