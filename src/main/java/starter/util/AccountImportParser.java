package starter.util;

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
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import starter.models.AccountColumn;
import starter.models.AccountConfiguration;

public class AccountImportParser {
	
	public static class AccountImportResult {
		private final AccountConfiguration[] accounts;
		private final AccountColumn[] columns;
		public AccountImportResult(AccountConfiguration[] accounts, AccountColumn[] columns) {
			this.accounts = accounts;
			this.columns = columns;
		}
		public AccountConfiguration[] getAccounts() {
			return this.accounts;
		}
		public AccountColumn[] getColumns() {
			return this.columns;
		}
	}
	
	public static AccountImportResult parse(String format, File file, Map<AccountImportField, String> defaults) {
		
		final ImportParser parser = new ImportParser(format, defaults);
		
		try {
			final AccountConfiguration[] accs = Files.readAllLines(file.toPath())
					.stream()
					.map(parser::parse)
					.filter(Objects::nonNull)
					.toArray(AccountConfiguration[]::new);
			final List<AccountColumn> columns = parser.fields.stream()
					.map(f -> f.corresponding)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			defaults.entrySet().stream()
				.filter(e -> e.getValue() != null && !e.getValue().trim().isEmpty())
				.map(Map.Entry::getKey)
				.map(val -> val.corresponding)
				.forEach(columns::add);
			return new AccountImportResult(accs, columns.toArray(new AccountColumn[0]));
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
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
		
		SELECTED(AccountColumn.SELECTED),
		USERNAME(AccountColumn.NAME),
		PASSWORD(AccountColumn.PASSWORD),
		PIN(AccountColumn.PIN),
		//TOTP_SECRET(AccountColumn.TOTP_SECRET),
		CLIENT(AccountColumn.CLIENT),
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
			//case TOTP_SECRET:
			case SCRIPT:
			case USERNAME:
			case COLOR:
			case CLIENT:
			case WORLD:
				return "(.+)";
			case USE_PROXY:
			case SELECTED:
				return "(?i)(true|false|yes|no|0|1|y|n)";
			case PROXY_PORT:
			case PIN:
			case HEAP_SIZE:
				return "(\\-?\\d+)";
			}
			throw new IllegalStateException();
		}
		
		public AccountColumn getCorrespondingAccountColumn() {
			return this.corresponding;
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
			case CLIENT:
			case WORLD:
			//case TOTP_SECRET:
				return String.class;
			case USE_PROXY:
			case SELECTED:
				return boolean.class;
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
