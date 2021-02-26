package starter.models;

import java.util.Arrays;
import java.util.Objects;

import com.google.gson.JsonParseException;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import starter.gson.GsonFactory;
import starter.util.EnumUtil;
import starter.util.ReflectionUtil;
import starter.util.AccountImportParser.AccountImportField;

public enum AccountColumn {

	SELECTED(null, "selected"),
	
	NAME("Login Name", "username"),
	PASSWORD("Password", "password"),
	PIN("Bank Pin", "pin"),
	//TOTP_SECRET("TOTP Secret", "totpSecret"),
	
	CLIENT("Client", "client"),
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
	NOTES("Notes", "notes"),
	;

	private final String label, fieldName;
	
	private AccountColumn(String label, String fieldName) {
		this.label = label;
		this.fieldName = fieldName;
	}
	
	public boolean isDefaultColumn() {
		switch (this) {
		case PIN:
		case PROXY_IP:
		case PROXY_PORT:
		case PROXY_USER:
		case PROXY_PASS:
		case NOTES:
		case CLIENT:
		//case TOTP_SECRET:
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
			final ProxyDescriptor copy = acc.getProxy().copy(); // copy so we force an update on the proxy field
			ReflectionUtil.setValueDirectly(copy, this.getFieldName(), value);
			acc.setProxy(copy);
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
			if (value.isEmpty())
				value = "0";
			int intValue = 0;
			try {
				intValue = Integer.parseInt(value);
			}
			catch (NumberFormatException e) {
				System.out.println("Invalid port, not an integer");
			}
			final ProxyDescriptor pcopy = acc.getProxy().copy(); // copy so we force an update on the proxy field
			ReflectionUtil.setValueDirectly(pcopy, this.getFieldName(), intValue);
			acc.setProxy(pcopy);
			break;
		case USE_PROXY:
		case SELECTED:
			ReflectionUtil.setValue(acc, this.getFieldName(), parseBoolean(value), boolean.class);
			break;
		case PROXY:
			try {
				final ProxyDescriptor proxy = GsonFactory.buildGson().fromJson(value, ProxyDescriptor.class);
				if (proxy != null && !proxy.isValid())
					acc.setProxy(null);
				else
					acc.setProxy(proxy);
			}
			catch (JsonParseException e) {
				acc.setProxy(null);
			}
			break;
		default:
			ReflectionUtil.setValue(acc, this.getFieldName(), value);
		}
	}
	
	private boolean parseBoolean(String s) {
		if (s == null) {
			return false;
		}
		s = s.toLowerCase();
		return s.equals("y") || s.equals("yes") || s.equals("true") || s.equals("1");
	}
	
	public String getCopyText(TablePosition<AccountConfiguration, ?> pos) {
		switch (this) {
		case PROXY:
			return GsonFactory.buildGson(false).toJson(pos.getTableView().getItems().get(pos.getRow()).getProxy());
		default: 
			return String.valueOf(pos.getTableColumn().getCellData(pos.getRow()));
		}
	}
	
	public String getCopyText(AccountConfiguration account, TableColumn<AccountConfiguration, ?> column) {
		switch (this) {
		case PROXY:
			return GsonFactory.buildGson(false).toJson(account.getProxy());
		default: 
			return String.valueOf(column.getCellData(account));
		}
	}
	
	@Override
	public String toString() {
		return this.label != null && !this.label.isEmpty() ? this.label : EnumUtil.toString(this); 
	}

	public String getLabel() {
		return this.label;
	}

	public String getFieldName() {
		return this.fieldName;
	}
	
	public String getSymbol() {
		// try to be consistent first, find a matching import field
		return Arrays.stream(AccountImportField.values())
				.filter(a -> a.getCorrespondingAccountColumn() == this)
				.findFirst()
				.map(f -> f.getSymbol())
				.orElseGet(() -> "${" + this.name().toLowerCase().replace("_", "") + "}");
	}
	
}
