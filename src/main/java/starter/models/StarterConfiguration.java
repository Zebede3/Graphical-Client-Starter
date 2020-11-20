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
	
	private final ObservableList<Integer> worldBlacklist = FXCollections.observableArrayList();
	
	private final SimpleBooleanProperty onlyLaunchInactiveAccounts = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty minimizeClients = new SimpleBooleanProperty(false);
	
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
	
}
