package starter.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProxyDescriptorModel {

	private final SimpleStringProperty name;
	private final SimpleStringProperty username;
	private final SimpleStringProperty password;
	private final SimpleStringProperty ip;
	private final SimpleStringProperty port;
	private final SimpleBooleanProperty editable;
	
	private final SimpleObjectProperty<Boolean> working;
	
	public ProxyDescriptorModel() {
		this.name = new SimpleStringProperty("");
		this.username = new SimpleStringProperty("");
		this.password = new SimpleStringProperty("");
		this.ip = new SimpleStringProperty("");
		this.port = new SimpleStringProperty("");
		this.editable = new SimpleBooleanProperty(true);
		this.working = new SimpleObjectProperty<>(null);
	}
	
	public ProxyDescriptorModel(ProxyDescriptor proxy, boolean editable) {
		this.name = new SimpleStringProperty(proxy.getName());
		this.username = new SimpleStringProperty(proxy.getUsername());
		this.password = new SimpleStringProperty(proxy.getPassword());
		this.ip = new SimpleStringProperty(proxy.getIp());
		this.port = new SimpleStringProperty(proxy.getPort() + "");
		this.editable = new SimpleBooleanProperty(editable);
		this.working = new SimpleObjectProperty<>(null);
	}
	
	public void setName(String name) {
		this.name.set(name);
	}
	
	public String getName() {
		return this.name.get();
	}
	
	public SimpleStringProperty nameProperty() {
		return this.name;
	}
	
	public void setUsername(String username) {
		this.username.set(username);
	}
	
	public String getUsername() {
		return this.username.get();
	}
	
	public SimpleStringProperty usernameProperty() {
		return this.username;
	}
	
	public void setPassword(String password) {
		this.password.set(password);
	}
	
	public String getPassword() {
		return this.password.get();
	}
	
	public SimpleStringProperty passwordProperty() {
		return this.password;
	}
	
	public String getIp() {
		return this.ip.get();
	}
	
	public void setIp(String ip) {
		this.ip.set(ip);
	}
	
	public SimpleStringProperty ipProperty() {
		return this.ip;
	}
	
	public String getPort() {
		return this.port.get();
	}
	
	public void setPort(String port) {
		this.port.set(port);
	}
	
	public SimpleStringProperty portProperty() {
		return this.port;
	}
	
	public SimpleBooleanProperty editableProperty() {
		return this.editable;
	}
	
	public boolean hasBeenChecked() {
		return this.working.get() != null;
	}
	
	public void resetChecked() {
		this.working.set(null);
	}
	
	public boolean isWorking() {
		return this.hasBeenChecked() && this.working.get();
	}
	
	public void setWorking(boolean working) {
		this.working.set(working);
	}
	
	public SimpleObjectProperty<Boolean> workingProperty() {
		return this.working;
	}
	
	public ProxyDescriptor toDescriptor() throws NumberFormatException {
		return new ProxyDescriptor(this.name.get(), this.ip.get(), Integer.parseInt(this.port.get()), this.username.get(), this.password.get());
	}
	
	@Override
	public String toString() {
		return this.name + " (" + this.ip + ":" + this.port + ")";
	}
	
}
