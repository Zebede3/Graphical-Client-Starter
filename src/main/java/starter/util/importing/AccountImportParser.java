package starter.util.importing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;
import starter.models.AccountConfiguration;
import starter.models.ProxyDescriptor;
import starter.util.EnumUtil;
import starter.util.ReflectionUtil;

public class AccountImportParser {

	public static AccountConfiguration[] parse(String format, File file) {
		
		final ImportParser parser = new ImportParser(format);
		
		try {
			return Files.readAllLines(file.toPath())
					.stream()
					.map(parser::parse)
					.filter(Objects::nonNull)
					.toArray(AccountConfiguration[]::new);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new AccountConfiguration[0];
		}
	}
	
	private static class ImportParser {
		
		private final List<AccountImportField> fields;
		private final Pattern pattern;
		
		public ImportParser(String format) {
			this.fields = new ArrayList<>();
			String result = "";
			int last = 0;
			string_iteration:
			for (int i = 0; i < format.length(); i++) {
				final String remaining = format.substring(i);
				for (AccountImportField field : AccountImportField.values()) {
					final String pattern = field.getSymbol();
					if (remaining.startsWith(pattern)) {
						final int lastFieldEnd = result.length() - (i - last);
						String nextResult = "";
						if (last > 0)
							nextResult += result.substring(0, lastFieldEnd);
						if (lastFieldEnd < result.length())
							nextResult += Pattern.quote(result.substring(lastFieldEnd));
						nextResult += field.getRegex();
						result = nextResult;
						i += pattern.length() - 1;
						last = i + 1;
						this.fields.add(field);
						continue string_iteration;
					}
				}
				result += format.charAt(i);
			}
			
			final int lastFieldEnd = result.length() - (format.length() - last);
			String nextResult = "";
			if (last > 0)
				nextResult += result.substring(0, lastFieldEnd);
			if (lastFieldEnd < result.length())
				nextResult += Pattern.quote(result.substring(lastFieldEnd));
			result = nextResult;
			
			this.pattern = Pattern.compile(result);
			System.out.println("Parsed import format: " + format + " -> " + result + " (" + this.fields + ")");
		}
		
		public AccountConfiguration parse(String line) {
			final Matcher matcher = this.pattern.matcher(line);
			if (!matcher.matches())
				return null;
			final AccountConfiguration acc = new AccountConfiguration();
			for (int i = 0; i < this.fields.size(); i++) {
				final AccountImportField field = this.fields.get(i);
				final String input = matcher.group(i + 1);
				field.setField(acc, input);
			}
			System.out.println("Parsed account: " + acc.toString());
			return acc;
		}
		
	}
	
	public enum AccountImportField {
		
		USERNAME("username"),
		PASSWORD("password"),
		PIN("pin"),
		SCRIPT("script"),
		ARGS("args"),
		WORLD("world"),
		BREAK_PROFILE("breakProfile"),
		HEAP_SIZE("heapSize"),
		USE_PROXY("useProxy"),
		PROXY_IP("ip"), // proxies are handled in a special way - they have a subtype
		PROXY_PORT("port"),
		PROXY_USERNAME("username"),
		PROXY_PASSWORD("password"),
		COLOR("color"),
		;
		
		private final String field;
		
		private AccountImportField(String field) {
			this.field = field;
		}
		
		private void setField(AccountConfiguration acc, String value) {
			switch (this) {
			case PROXY_IP:
			case PROXY_USERNAME:
			case PROXY_PASSWORD:
				if (acc.getProxy() == null)
					acc.setProxy(new ProxyDescriptor("", "", 0, "", ""));
				ReflectionUtil.setValue(acc.getProxy(), this.field, value);
				break;
			case PROXY_PORT:
				if (acc.getProxy() == null)
					acc.setProxy(new ProxyDescriptor("", "", 0, "", ""));
				ReflectionUtil.setValue(acc.getProxy(), this.field, Integer.parseInt(value));
				break;
			case COLOR:
				ReflectionUtil.setValue(acc, this.field, Color.web(value));
				break;
			case USE_PROXY:
				ReflectionUtil.setValue(acc, this.field, Boolean.parseBoolean(value));
				break;
			default:
				ReflectionUtil.setValue(acc, this.field, value);
			}
		}
		
		private String getRegex() {
			switch (this) {
			case ARGS:
			case BREAK_PROFILE:
			case PASSWORD:
			case PROXY_IP:
			case PROXY_PASSWORD:
			case PROXY_USERNAME:
			case SCRIPT:
			case USERNAME:
			case COLOR:
				return "(.+)";
			case USE_PROXY:
				return "(true|false)";
			case WORLD:
			case PROXY_PORT:
			case PIN:
			case HEAP_SIZE:
				return "(\\d+)";
			}
			throw new IllegalStateException();
		}
		
		public String getSymbol() {
			return "${" + this.name().toLowerCase().replace("_", "") + "}";
		}
		
		public Class<?> getType() {
			switch (this) {
			case ARGS:
			case BREAK_PROFILE:
			case PASSWORD:
			case PROXY_IP:
			case PROXY_PASSWORD:
			case PROXY_USERNAME:
			case SCRIPT:
			case USERNAME:
			case COLOR:
				return String.class;
			case USE_PROXY:
				return boolean.class;
			case WORLD:
			case PROXY_PORT:
			case PIN:
			case HEAP_SIZE:
				return int.class;
			}
			throw new IllegalStateException();
		}
		
		@Override
		public String toString() {
			return EnumUtil.toString(this);
		}
		
	}

}
