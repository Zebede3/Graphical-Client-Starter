package starter.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import starter.gson.GsonFactory;

public class AccountConfiguration {

	private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

	private final SimpleStringProperty username = new SimpleStringProperty("");
	private final SimpleStringProperty password = new SimpleStringProperty("");
	private final SimpleStringProperty pin = new SimpleStringProperty("");
	private final SimpleStringProperty script = new SimpleStringProperty("");

	private final SimpleStringProperty args = new SimpleStringProperty("");
	private final SimpleStringProperty world = new SimpleStringProperty("");
	private final SimpleStringProperty breakProfile = new SimpleStringProperty("");
	private final SimpleStringProperty heapSize = new SimpleStringProperty("");
	
	private final SimpleBooleanProperty useProxy = new SimpleBooleanProperty(false);
	private final SimpleObjectProperty<ProxyDescriptor> proxy = new SimpleObjectProperty<>();
	
	private final SimpleStringProperty notes = new SimpleStringProperty("");
	
	private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>();
	
	public AccountConfiguration copy() {
		final Gson gson = GsonFactory.buildGson();
		return gson.fromJson(gson.toJson(this), AccountConfiguration.class);
	}
	
	public boolean isSelected() {
		return this.selected.get();
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	public String getUsername() {
		return this.username.get();
	}

	public void setUsername(String username) {
		this.username.set(username);
	}

	public String getScript() {
		return this.script.get();
	}

	public void setScript(String script) {
		this.script.set(script);
	}

	public String getArgs() {
		return this.args.get();
	}

	public void setArgs(String args) {
		this.args.set(args);
	}

	public String getWorld() {
		return this.world.get();
	}

	public void setWorld(String world) {
		this.world.set(world);
	}

	public String getBreakProfile() {
		return this.breakProfile.get();
	}

	public void setBreakProfile(String breakProfile) {
		this.breakProfile.set(breakProfile);
	}

	public String getHeapSize() {
		return this.heapSize.get();
	}
	
	public void setPin(String pin) {
		this.pin.set(pin);
	}

	public String getPin() {
		return this.pin.get();
	}
	
	public void setPassword(String password) {
		this.password.set(password);
	}

	public String getPassword() {
		return this.password.get();
	}

	public void setHeapSize(String heapSize) {
		this.heapSize.set(heapSize);
	}
	
	public SimpleBooleanProperty selectedProperty() {
		return this.selected;
	}
	
	public boolean isUseProxy() {
		return this.useProxy.get();
	}
	
	public void setUseProxy(boolean useProxy) {
		this.useProxy.set(useProxy);
	}
	
	public ProxyDescriptor getProxy() {
		return this.proxy.get();
	}
	
	public void setProxy(ProxyDescriptor proxy) {
		this.proxy.set(proxy);
	}

	public SimpleBooleanProperty useProxyProperty() {
		return this.useProxy;
	}
	
	public void setColor(Color c) {
		this.color.set(c);
	}
	
	public Color getColor() {
		return this.color.get();
	}
	
	public SimpleObjectProperty<Color> colorProperty() {
		return this.color;
	}

	public void setNotes(String notes) {
		this.notes.set(notes);
	}

	public String getNotes() {
		return this.notes.get();
	}
	
	@Override
	public String toString() {
		
		final List<String> components = new ArrayList<>();
		
		components.add("Username: " + this.username.get());
		if (!this.password.get().isEmpty())
			components.add("Password: " + this.password.get());
		if (!this.pin.get().isEmpty())
			components.add("Pin: " + this.pin.get());
		components.add("Script: " + this.script.get());
		
		if (!this.args.get().isEmpty())
			components.add("Args: " + this.args.get());
	
		if (!this.world.get().isEmpty())
			components.add("World: " + this.world.get());
		
		if (!this.breakProfile.get().isEmpty())
			components.add("Break Profile: " + this.breakProfile.get());
		
		if (!this.heapSize.get().isEmpty())
			components.add("Heap Size: " + this.heapSize.get());
		
		if (this.useProxy.get() && this.proxy.get() != null)
			components.add("Proxy: " + this.proxy.get());
		
		return components.stream().collect(Collectors.joining(", "));
	}
	
}
