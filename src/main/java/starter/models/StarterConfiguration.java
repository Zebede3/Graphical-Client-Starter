package starter.models;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import starter.gson.GsonFactory;
import starter.util.ImportStrategy;

public class StarterConfiguration {

	// Core info
	
	private final ObservableList<AccountConfiguration> accounts = FXCollections.observableArrayList();
	
	private final SimpleIntegerProperty delayBetweenLaunch = new SimpleIntegerProperty(30);
	
	// Settings
	
	private final SimpleBooleanProperty useLookingGlass = new SimpleBooleanProperty(false);
	
	private final SimpleStringProperty lookingGlassPath = new SimpleStringProperty("");

	private final SimpleBooleanProperty loginProperty = new SimpleBooleanProperty(false);
	private final SimpleStringProperty tribotUsername = new SimpleStringProperty("");
	private final SimpleStringProperty tribotPassword = new SimpleStringProperty("");
	
	private final SimpleBooleanProperty supplySid = new SimpleBooleanProperty(false);
	private final SimpleStringProperty sid = new SimpleStringProperty("");
	
	//private final SimpleBooleanProperty useCustomTribotPath = new SimpleBooleanProperty(false);
	private final SimpleStringProperty customTribotPath = new SimpleStringProperty(new File("").getAbsolutePath());
	
	private final Map<AccountColumn, SimpleBooleanProperty> displayColumns;

//	private final SimpleBooleanProperty useCustomJavaPath = new SimpleBooleanProperty(false);
//	private final SimpleStringProperty customJavaPath = new SimpleStringProperty("");
	
	private final SimpleBooleanProperty scheduleLaunch = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty useCustomLaunchDate = new SimpleBooleanProperty(false);
	private final SimpleObjectProperty<LocalDate> customLaunchDate = new SimpleObjectProperty<>(LocalDate.now());
	private final SimpleObjectProperty<LocalTime> launchTime = new SimpleObjectProperty<>(LocalTime.now());
	
	private final SimpleBooleanProperty scheduleClientShutdown = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty useCustomClientShutdownDate = new SimpleBooleanProperty(false);
	private final SimpleObjectProperty<LocalDate> customClientShutdownDate = new SimpleObjectProperty<>(LocalDate.now());
	private final SimpleObjectProperty<LocalTime> clientShutdownTime = new SimpleObjectProperty<>(LocalTime.now());
	
	private final SimpleBooleanProperty rescheduleShutdownClients = new SimpleBooleanProperty(false);
	private final SimpleIntegerProperty rescheduleShutdownClientsMinutes = new SimpleIntegerProperty(540);
	
	private final ObservableList<Integer> worldBlacklist = FXCollections.observableArrayList();
	
	private final SimpleBooleanProperty onlyLaunchInactiveAccounts = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty minimizeClients = new SimpleBooleanProperty(false);
	
	private final SimpleBooleanProperty autoBatchAccounts = new SimpleBooleanProperty(false);
	private final SimpleIntegerProperty autoBatchAccountQuantity = new SimpleIntegerProperty(5);
	
	private final SimpleBooleanProperty restartClosedClients = new SimpleBooleanProperty(false);
	
	private final SimpleObjectProperty<ImportStrategy> importActionProperty = new SimpleObjectProperty<>(ImportStrategy.CREATE_NEW);
	
	public StarterConfiguration() {
		this.displayColumns = Arrays.stream(AccountColumn.values())
								.collect(Collectors.toMap(Function.identity(), v -> new SimpleBooleanProperty(v.isDefaultColumn())));
	}
	
	public SimpleBooleanProperty displayColumnProperty(AccountColumn col) {
		if (col == null)
			throw new NullPointerException();
		return this.displayColumns.computeIfAbsent(col, k -> new SimpleBooleanProperty(col.isDefaultColumn()));
	}
	
	public StarterConfiguration copy() {
		final Gson gson = GsonFactory.buildGson();
		return gson.fromJson(gson.toJson(this), StarterConfiguration.class);
	}
	
	public ObservableList<AccountConfiguration> getAccounts() {
		return this.accounts;
	}

	public int getDelayBetweenLaunch() {
		return this.delayBetweenLaunch.get();
	}

	public void setDelayBetweenLaunch(int delayBetweenLaunch) {
		this.delayBetweenLaunch.set(delayBetweenLaunch);
	}
	
	public SimpleIntegerProperty delayBetweenLaunchProperty() {
		return this.delayBetweenLaunch;
	}
	
	public boolean isLookingGlass() {
		return this.useLookingGlass.get();
	}
	
	public void setLookingGlass(boolean use) {
		this.useLookingGlass.set(use);
	}
	
	public SimpleBooleanProperty lookingGlassProperty() {
		return this.useLookingGlass;
	}
	
	public String getLookingGlassPath() {
		return this.lookingGlassPath.get();
	}
	
	public void setLookingGlassPath(String path) {
		this.lookingGlassPath.set(path);
	}
	
	public SimpleStringProperty lookingGlassPathProperty() {
		return this.lookingGlassPath;
	}
	
	public String getTribotUsername() {
		return this.tribotUsername.get();
	}
	
	public String getTribotPassword() {
		return this.tribotPassword.get();
	}
	
	public void setTribotUsername(String username) {
		this.tribotUsername.set(username);
	}
	
	public void setTribotPassword(String password) {
		this.tribotPassword.set(password);
	}
	
	public SimpleStringProperty tribotUsernameProperty() {
		return this.tribotUsername;
	}
	
	public SimpleStringProperty tribotPasswordProperty() {
		return this.tribotPassword;
	}
	
	public boolean isLogin() {
		return this.loginProperty.get();
	}
	
	public void setLogin(boolean login) {
		this.loginProperty.set(login);
	}
	
	public SimpleBooleanProperty loginProperty() {
		return this.loginProperty;
	}
	
	public boolean isSupplySid() {
		return this.supplySid.get();
	}
	
	public void setSupplySid(boolean supply) {
		this.supplySid.set(supply);
	}
	
	public SimpleBooleanProperty supplySidProperty() {
		return this.supplySid;
	}
	
	public String getSid() {
		return this.sid.get();
	}
	
	public void setSid(String password) {
		this.sid.set(password);
	}
	
	public SimpleStringProperty sidProperty() {
		return this.sid;
	}
		
//	public boolean isUseCustomTribotPath() {
//		return this.useCustomTribotPath.get();
//	}
//	
//	public void setUseCustomTribotPath(boolean use) {
//		this.useCustomTribotPath.set(use);
//	}
//	
//	public SimpleBooleanProperty useCustomTribotPathProperty() {
//		return this.useCustomTribotPath;
//	}
	
	public String getCustomTribotPath() {
		return this.customTribotPath.get();
	}
	
	public void setCustomTribotPath(String path) {
		this.customTribotPath.set(path);
	}
	
	public SimpleStringProperty customTribotPathProperty() {
		return this.customTribotPath;
	}
	
//	public boolean isUseCustomJavaPath() {
//		return this.useCustomJavaPath.get();
//	}
//	
//	public void setUseCustomJavaPath(boolean use) {
//		this.useCustomJavaPath.set(use);
//	}
//	
//	public SimpleBooleanProperty useCustomJavaPathProperty() {
//		return this.useCustomJavaPath;
//	}
//	
//	public String getCustomJavaPath() {
//		return this.customJavaPath.get();
//	}
//	
//	public void setCustomJavaPath(String path) {
//		this.customJavaPath.set(path);
//	}
//	
//	public SimpleStringProperty customJavaPathProperty() {
//		return this.customJavaPath;
//	}
	
	public boolean isScheduleLaunch() {
		return this.scheduleLaunch.get();
	}
	
	public void setScheduleLaunch(boolean schedule) {
		this.scheduleLaunch .set(schedule);
	}
	
	public SimpleBooleanProperty scheduleLaunchProperty() {
		return this.scheduleLaunch;
	}
	
	public LocalDate getLaunchDate() {
		return this.customLaunchDate.get();
	}
	
	public void setLaunchDate(LocalDate date) {
		this.customLaunchDate.set(date);
	}
	
	public SimpleObjectProperty<LocalDate> customLaunchDateProperty(){
		return this.customLaunchDate;
	}
	
	public boolean isUseCustomLaunchDate() {
		return this.useCustomLaunchDate.get();
	}
	
	public void setUseCustomLaunchDate(boolean customLaunchDate) {
		this.useCustomLaunchDate.set(customLaunchDate);
	}
	
	public SimpleBooleanProperty useCustomLaunchDateProperty() {
		return this.scheduleLaunch;
	}
	
	public LocalTime getLaunchTime() {
		return this.launchTime.get();
	}
	
	public void setLaunchTime(LocalTime time) {
		this.launchTime.set(time);
	}
	
	public SimpleObjectProperty<LocalTime> launchTimeProperty() {
		return this.launchTime;
	}
	
	public boolean isScheduleClientShutdown() {
		return this.scheduleClientShutdown.get();
	}
	
	public void setScheduleClientShutdown(boolean schedule) {
		this.scheduleClientShutdown .set(schedule);
	}
	
	public SimpleBooleanProperty scheduleClientShutdownProperty() {
		return this.scheduleClientShutdown;
	}
	
	public LocalDate getCustomClientShutdownDate() {
		return this.customClientShutdownDate.get();
	}
	
	public void setCustomClientShutdownDate(LocalDate date) {
		this.customClientShutdownDate.set(date);
	}
	
	public SimpleObjectProperty<LocalDate> customClientShutdownDateProperty(){
		return this.customClientShutdownDate;
	}
	
	public boolean isUseCustomClientShutdownDate() {
		return this.useCustomClientShutdownDate.get();
	}
	
	public void setUseCustomClientShutdownDate(boolean customLaunchDate) {
		this.useCustomClientShutdownDate.set(customLaunchDate);
	}
	
	public SimpleBooleanProperty useCustomClientShutdownDateProperty() {
		return this.useCustomClientShutdownDate;
	}
	
	public LocalTime getClientShutdownTime() {
		return this.clientShutdownTime.get();
	}
	
	public void setClientShutdownTime(LocalTime time) {
		this.clientShutdownTime.set(time);
	}
	
	public SimpleObjectProperty<LocalTime> clientShutdownTimeProperty() {
		return this.clientShutdownTime;
	}
	
	public ObservableList<Integer> worldBlacklist() {
		return this.worldBlacklist;
	}
	
	public boolean isOnlyLaunchInactiveAccounts() {
		return this.onlyLaunchInactiveAccounts.get();
	}
	
	public SimpleBooleanProperty onlyLaunchInactiveAccountsProperty() {
		return this.onlyLaunchInactiveAccounts;
	}
	
	public boolean isMinimizeClients() {
		return this.minimizeClients.get();
	}
	
	public void setMinimizeClients(boolean minimize) {
		this.minimizeClients.set(minimize);
	}
	
	public SimpleBooleanProperty minimizeClientsProperty() {
		return this.minimizeClients;
	}
	
	public boolean isAutoBatchAccounts() {
		return this.autoBatchAccounts.get();
	}
	
	public void setAutoBatchAccounts(boolean autoBatchAccounts) {
		this.autoBatchAccounts.set(autoBatchAccounts);
	}
	
	public SimpleBooleanProperty autoBatchAccountsProperty() {
		return this.autoBatchAccounts;
	}
	
	public int getAutoBatchAccountQuantity() {
		return this.autoBatchAccountQuantity.get();
	}
	
	public void setAutoBatchAccountQuantity(int autoBatchAccountQuantity) {
		this.autoBatchAccountQuantity.set(autoBatchAccountQuantity);
	}
	
	public SimpleIntegerProperty autoBatchAccountQuantityProperty() {
		return this.autoBatchAccountQuantity;
	}
	
	public boolean isRestartClosedClients() {
		return this.restartClosedClients.get();
	}
	
	public void setRestartClosedClients(boolean restart) {
		this.restartClosedClients.set(restart);
	}
	
	public SimpleBooleanProperty restartClosedClientsProperty() {
		return this.restartClosedClients;
	}
	
	public int getRescheduleShutdownClientsMinutes() {
		return this.rescheduleShutdownClientsMinutes.get();
	}
	
	public void setRescheduleShutdownClientsMinutes(int rescheduleShutdownClientsMinutes) {
		this.rescheduleShutdownClientsMinutes.set(rescheduleShutdownClientsMinutes);
	}
	
	public SimpleIntegerProperty rescheduleShutdownClientsMinutesProperty() {
		return this.rescheduleShutdownClientsMinutes;
	}
	
	public boolean isRescheduleShutdownClients() {
		return this.rescheduleShutdownClients.get();
	}
	
	public void setRescheduleShutdownClients(boolean rescheduleShutdownClients) {
		this.rescheduleShutdownClients.set(rescheduleShutdownClients);
	}
	
	public SimpleBooleanProperty rescheduleShutdownClientsProperty() {
		return this.rescheduleShutdownClients;
	}
	
	public ImportStrategy getImportAction() {
		if (this.importActionProperty.get() == null) {
			setImportAction(ImportStrategy.CREATE_NEW);
		}
		return this.importActionProperty.get();
	}
	
	public void setImportAction(ImportStrategy a) {
		this.importActionProperty.set(a);
	}
	
	public SimpleObjectProperty<ImportStrategy> importActionProperty() {
		if (this.importActionProperty.get() == null) {
			setImportAction(ImportStrategy.CREATE_NEW);
		}
		return this.importActionProperty;
	}
	
}
