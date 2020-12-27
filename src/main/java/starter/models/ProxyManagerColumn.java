package starter.models;

import starter.util.ReflectionUtil;

public enum ProxyManagerColumn {

	NAME("Name", "name"),
	IP("IP", "ip"),
	PORT("Port", "port"),
	USERNAME("Username", "username"),
	PASSWORD("Password", "password"),
	;
	
	private final String label;
	private final String fieldName;
	
	private ProxyManagerColumn(String label, String fieldName) {
		this.label = label;
		this.fieldName = fieldName;
	}
	
	public void set(ProxyDescriptorModel model, String value) {
		ReflectionUtil.setValue(model, this.fieldName, value);
	}
	
	public String get(ProxyDescriptorModel model) {
		return ReflectionUtil.getValue(model, this.fieldName);
	}

	public String getLabel() {
		return this.label;
	}

	public String getFieldName() {
		return this.fieldName;
	}
	
	@Override
	public String toString() {
		return this.label;
	}
	
}
