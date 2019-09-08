package starter.models;

import com.google.gson.Gson;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import starter.gson.GsonFactory;

public class StarterConfiguration {

	private final ObservableList<AccountConfiguration> accounts = FXCollections.observableArrayList();
	
	private final SimpleIntegerProperty delayBetweenLaunch = new SimpleIntegerProperty(30);
	
	private final SimpleBooleanProperty useLookingGlass = new SimpleBooleanProperty(false);
	
	private final SimpleStringProperty lookingGlassPath = new SimpleStringProperty("");

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
	
	
}
