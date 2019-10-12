package starter.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import starter.gson.GsonFactory;
import starter.gui.AccountColumn;

public class StarterConfiguration {

	private final ObservableList<AccountConfiguration> accounts = FXCollections.observableArrayList();
	
	private final SimpleIntegerProperty delayBetweenLaunch = new SimpleIntegerProperty(30);
	
	private final SimpleBooleanProperty useLookingGlass = new SimpleBooleanProperty(false);
	
	private final SimpleStringProperty lookingGlassPath = new SimpleStringProperty("");

	private final SimpleBooleanProperty loginProperty = new SimpleBooleanProperty(false);
	private final SimpleStringProperty tribotUsername = new SimpleStringProperty("");
	private final SimpleStringProperty tribotPassword = new SimpleStringProperty("");
	
	private final SimpleBooleanProperty supplySid = new SimpleBooleanProperty(false);
	private final SimpleStringProperty sid = new SimpleStringProperty("");
	
	private final SimpleBooleanProperty useCustomTribotPath = new SimpleBooleanProperty(false);
	private final SimpleStringProperty customTribotPath = new SimpleStringProperty("");
	
	private final Map<AccountColumn, SimpleBooleanProperty> displayColumns;

	public StarterConfiguration() {
		final Map<AccountColumn, SimpleBooleanProperty> temp = Arrays.stream(AccountColumn.values())
								.collect(Collectors.toMap(Function.identity(), v -> new SimpleBooleanProperty(v.isDefaultColumn())));
		this.displayColumns = Collections.unmodifiableMap(temp);
	}
	
	public SimpleBooleanProperty displayColumnProperty(AccountColumn col) {
		final SimpleBooleanProperty prop = this.displayColumns.get(col);
		if (prop == null)
			throw new IllegalArgumentException(String.valueOf(col));
		return prop;
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
	
	public String getLookingGlassPath() {
		return this.lookingGlassPath.get();
	}
	
	public void setLookingGlassPath(String path) {
		this.lookingGlassPath.set(path);
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
		
	public boolean isUseCustomTribotPath() {
		return this.useCustomTribotPath.get();
	}
	
	public void setUseCustomTribotPath(boolean use) {
		this.useCustomTribotPath.set(use);
	}
	
	public SimpleBooleanProperty useCustomTribotPathProperty() {
		return this.useCustomTribotPath;
	}
	
	public String getCustomTribotPath() {
		return this.customTribotPath.get();
	}
	
	public void setCustomTribotPath(String path) {
		this.customTribotPath.set(path);
	}
	
	public SimpleStringProperty customTribotPathProperty() {
		return this.customTribotPath;
	}
	
}