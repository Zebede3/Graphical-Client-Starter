package starter.models;

import java.util.Objects;

import starter.util.EnumUtil;
import starter.util.ReflectionUtil;

public enum AccountColumn {

	SELECTED(null, "selected"),
	
	NAME("Account Name", "username"),
	PASSWORD("Password", "password"),
	PIN("Bank Pin", "pin"),
	
	SCRIPT("Script", "script"),
	ARGS("Script Arguments", "args"),
	
	WORLD("World", "world"),
	BREAK_PROFILE("Break Profile", "breakProfile"),
	HEAP_SIZE("Heap Size", "heapSize"),
	USE_PROXY("Use Proxy", "useProxy"),
	PROXY("Proxy", "proxy"),
	PROXY_IP("Proxy IP", "ip"),
	PROXY_PORT("Proxy Port", "port"),
	PROXY_USER("Proxy Username", "username"),
	PROXY_PASS("Proxy Password", "password"),
	;

	private final String label, fieldName;
	
	private AccountColumn(String label, String fieldName) {
		this.label = label;
		this.fieldName = fieldName;
	}
	
	public boolean isDefaultColumn() {
		switch (this) {
		case PASSWORD:
		case PIN:
		case PROXY_IP:
		case PROXY_PORT:
		case PROXY_USER:
		case PROXY_PASS:
			return false;
		default:
			return true;
		}
	}
	
	// some of these kinda modify final fields w/ reflection but its fine for now, could look to change
	// if value is empty it is generally taken to be none
	public void setField(AccountConfiguration acc, String value) {
		Objects.requireNonNull(acc);
		Objects.requireNonNull(value);
		switch (this) {
		case PROXY_IP:
		case PROXY_USER:
		case PROXY_PASS:
			if (acc.getProxy() == null)
				acc.setProxy(new ProxyDescriptor("", "", 0, "", ""));
			if (value.isEmpty()
					&& acc.getProxy().getPort() == 0
					&& acc.getProxy().getUsername().isEmpty()
					&& acc.getProxy().getPassword().isEmpty()) {
				acc.setProxy(null);
				return;
			}
			ReflectionUtil.setValueDirectly(acc.getProxy(), this.getFieldName(), value);
			break;
		case PROXY_PORT:
			if (acc.getProxy() == null)
				acc.setProxy(new ProxyDescriptor("", "", 0, "", ""));
			if (value.isEmpty()
					&& acc.getProxy().getIp().isEmpty()
					&& acc.getProxy().getUsername().isEmpty()
					&& acc.getProxy().getPassword().isEmpty()) {
				acc.setProxy(null);
				return;
			}
			try {
				if (value.isEmpty())
					value = "0";
				ReflectionUtil.setValueDirectly(acc.getProxy(), this.getFieldName(), Integer.parseInt(value));
			}
			catch (NumberFormatException e) {
				System.out.println("Invalid port, not an integer");
				//e.printStackTrace();
			}
			break;
		case USE_PROXY:
		case SELECTED:
			ReflectionUtil.setValue(acc, this.getFieldName(), Boolean.parseBoolean(value), boolean.class);
			break;
		case PROXY:
			// take this to be no proxy
			if (value.isEmpty() || value.equalsIgnoreCase("null")) {
				acc.setProxy(null);
				return;
			}
			System.out.println("Setting proxy object not supported");
			break;
		default:
			ReflectionUtil.setValue(acc, this.getFieldName(), value);
		}
	}
	
	@Override
	public String toString() {
		return EnumUtil.toString(this);
	}

	public String getLabel() {
		return this.label;
	}

	public String getFieldName() {
		return this.fieldName;
	}
	
}
