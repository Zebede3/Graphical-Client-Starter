package starter.util.importing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;
import starter.util.EnumUtil;
import starter.util.ReflectionUtil;

public class AccountImportParser {

	public static AccountConfiguration[] parse(String format, File file, Map<AccountImportField, String> defaults) {
		
		final ImportParser parser = new ImportParser(format, defaults);
		
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
		private final Map<AccountImportField, String> defaults;
		
		public ImportParser(String format, Map<AccountImportField, String> defaults) {
			this.fields = new ArrayList<>();
			this.defaults = defaults;
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
			final Map<AccountImportField, String> defaults = new HashMap<>(this.defaults);
			for (int i = 0; i < this.fields.size(); i++) {
				final AccountImportField field = this.fields.get(i);
				final String input = matcher.group(i + 1);
				field.setField(acc, input);
				defaults.remove(field);
			}
			defaults.forEach((field, val) -> field.setField(acc, val));
			System.out.println("Parsed account: " + acc.toString());
			return acc;
		}
		
	}
	
	public enum AccountImportField {
		
		USERNAME(AccountColumn.NAME),
		PASSWORD(AccountColumn.PASSWORD),
		PIN(AccountColumn.PIN),
		SCRIPT(AccountColumn.SCRIPT),
		ARGS(AccountColumn.ARGS),
		WORLD(AccountColumn.WORLD),
		BREAK_PROFILE(AccountColumn.BREAK_PROFILE),
		HEAP_SIZE(AccountColumn.HEAP_SIZE),
		USE_PROXY(AccountColumn.USE_PROXY),
		PROXY_IP(AccountColumn.PROXY_IP),
		PROXY_PORT(AccountColumn.PROXY_PORT),
		PROXY_USERNAME(AccountColumn.PROXY_USER),
		PROXY_PASSWORD(AccountColumn.PROXY_PASS),
		COLOR(null),
		;
		
		private final AccountColumn corresponding;
		
		private AccountImportField(AccountColumn corresponding) {
			this.corresponding = corresponding;
		}
		
		private void setField(AccountConfiguration acc, String value) {
			switch (this) {
			case COLOR:
				ReflectionUtil.setValue(acc, "color", Color.web(value));
				break;
			default:
				this.corresponding.setField(acc, value);
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
