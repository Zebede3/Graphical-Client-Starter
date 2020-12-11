package starter.launch;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import starter.models.AccountConfiguration;
import starter.models.ApplicationConfiguration;
import starter.models.PendingLaunch;
import starter.models.ProxyDescriptor;
import starter.models.StarterConfiguration;
import starter.util.FileUtil;
import starter.util.WorldParseException;
import starter.util.WorldUtil;

public class TribotLauncher implements ClientLauncher {
	
	private static final String PRINT_PID = "System.out.println(\"Launched Client Process ID: \" + p.pid());";
	private static final String ADD_AFTER = "final Process p = Runtime.getRuntime().exec(commandLineArgs.toArray(new String[0]));";
	
	private volatile boolean triedModify;
	
	private volatile boolean supportsMinimize = false;
	
	@Override
	public Process launchAccount(ApplicationConfiguration appConfig, PendingLaunch launch) {
		
		final AccountConfiguration[] accounts = launch.getAccounts();
		final StarterConfiguration settings = launch.getSettings();
		
		System.out.println("Attempting to launch client '" + launch + "' with accounts: " + Arrays.stream(launch.getAccounts()).map(a -> a.toString()).collect(Collectors.joining(", ")));
				
		if (!this.triedModify) {
			this.triedModify = true;
			modifyBuildGradle(settings.getCustomTribotPath());
		}
		
		final List<String> args = new ArrayList<>();
		
		args.add(settings.getCustomTribotPath() + File.separator + "tribot-gradle-launcher" + File.separator
				+ (System.getProperty("os.name").toLowerCase().contains("win") ? "gradlew.bat" : "gradlew"));
		
		if (appConfig.isDebugMode()) {
			args.add("runAttached");
		}
		else {
			args.add("runDetached");
		}

		final Map<String, String[]> accountArgs = new LinkedHashMap<>();
		for (int i = 0; i < accounts.length; i++) {
			final int index = i;
			buildAccountArgs(settings, accounts[i])
			.forEach((key, val) -> {
				accountArgs.computeIfAbsent(key, (a) -> new String[accounts.length])[index] = val;
			});
		}
		
		accountArgs.forEach((key, values) -> {
			args.add(key);
			args.add(Arrays.stream(values).map(s -> {
				if (s == null || s.trim().isEmpty()) {
					return "";
				}
				s = s.trim();
				if (s.contains(",")) {
					final String quoted = quote(s);
					if (appConfig.isDebugMode()) {
						System.out.println("Quoted " + s + " because it contains a comma -> " + quoted);
					}
					return quoted;
				}
				return s;
			}).collect(Collectors.joining(",")));
		});
		
		if (launch.getSettings().isLookingGlass()) {
			args.add("--lgpath");
			args.add(launch.getSettings().getLookingGlassPath());
			args.add("--lgdelay");
			args.add("15");
		}
		
		if (Arrays.stream(accounts).anyMatch(a -> !a.getHeapSize().trim().isEmpty())) {
			args.add("--mem");
			final int sum = Arrays.stream(accounts).mapToInt(a -> {
				try {
					return Integer.parseInt(a.getHeapSize().trim());
				}
				catch (NumberFormatException e) {
					return 512;
				}
			}).sum();
			args.add(sum + "");
		}
		
		if (settings.isLogin()) {
			args.add("--username");
			args.add(settings.getTribotUsername().trim());
			args.add("--password");
			args.add(settings.getTribotPassword().trim());
		}
		if (settings.isSupplySid()) {
			args.add("--sid");
			args.add(settings.getSid().trim());
		}
		
		if (settings.isMinimizeClients()) {
			if (this.supportsMinimize) {
				args.add("--minimize");
			}
			else {
				System.out.println("Your TRiBot build.gradle file does not support the --minimize arg");
			}
		}
		
		System.out.println("Launching client: " + args.toString());
		
		try {
			return fakeJavaHome(new ProcessBuilder()
					.directory(new File(settings.getCustomTribotPath(), "tribot-gradle-launcher"))
					.redirectErrorStream(true)
					.redirectInput(FileUtil.NULL_FILE)
					.command(args)
					.redirectOutput(Redirect.PIPE), settings.getCustomTribotPath())
					.start();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to launch client");
			return null;
		}
	}
	
	private ProcessBuilder fakeJavaHome(ProcessBuilder pb, String tribotPath) {
		final File jre = new File(tribotPath, "jre");
		pb.environment().put("JAVA_HOME", jre.getAbsolutePath());
		System.out.println("Process Environment Properties: " + pb.environment());
		return pb;
	}
	
	private void modifyBuildGradle(String path) {
		try {
			final List<String> lines = Files.readAllLines(new File(path + File.separator + "tribot-gradle-launcher" + File.separator + "build.gradle").toPath());
			if (lines.stream().noneMatch(s -> s.trim().equals(PRINT_PID))) {
				for (int i = 0; i < lines.size(); i++) {
					final String s = lines.get(i);
					if (s.trim().equals(ADD_AFTER)) {
						lines.add(i + 1, PRINT_PID);
						final String contents = lines.stream().collect(Collectors.joining(System.lineSeparator()));
						Files.writeString(new File(path + File.separator + "tribot-gradle-launcher" + File.separator + "build.gradle").toPath(), contents);
						System.out.println("Modified build.gradle");
						break;
					}
				}
			}
			if (lines.stream().anyMatch(s -> s.contains("--minimize"))) {
				this.supportsMinimize = true;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to modify build.gradle");
		}	
	}
	
	private Map<String, String> buildAccountArgs(StarterConfiguration settings, AccountConfiguration account) {
		
		final Map<String, String> args = new LinkedHashMap<>();
		
		boolean usingSplitUsername = false;
		if (account.getUsername().contains(":") && account.getPassword().trim().isEmpty()) {
			final String[] split = account.getUsername().split(":");
			if (split.length == 2) {
				usingSplitUsername = true;
				args.put("--charusername", split[0].trim());
				args.put("--charpassword", split[1].trim());
			}
		}
		
		if (!usingSplitUsername) {
			if (!account.getUsername().trim().isEmpty()) {
				args.put("--charusername", account.getUsername().trim());
			}
			if (!account.getPassword().trim().isEmpty()) {
				args.put("--charpassword", account.getPassword().trim());
			}	
		}
		
		if (account.getPin().length() == 4) {
			args.put("--charpin", account.getPin().trim());
		}
		
		if (!account.getScript().trim().isEmpty()) {
			args.put("--script", account.getScript().trim());
		}
		if (!account.getArgs().trim().isEmpty()) {
			args.put("--scriptargs", account.getArgs().trim());
		}
		if (!account.getBreakProfile().trim().isEmpty()) {
			args.put("--breakprofile", account.getBreakProfile().trim());
		}
		
		if (!account.getWorld().trim().isEmpty()) {
			final String world;
			try {
				world = WorldUtil.parseWorld(account.getWorld().trim(), settings.worldBlacklist());
			}
			catch (WorldParseException e) {
				e.printStackTrace();
				System.out.println("Failed to parse world");
				return null;
			}
			if (world != null) {
				args.put("--charworld", world);
			}
		}
		
		final ProxyDescriptor proxy = account.getProxy();
		
		if (account.isUseProxy() && proxy != null) {
			
			if (proxy.getIp() != null && !proxy.getIp().trim().isEmpty()) {
				args.put("--proxyhost", proxy.getIp().trim());
			}
			
			if (proxy.getPort() > 0) {
				args.put("--proxyport", proxy.getPort() + "");
			}

			if (proxy.getUsername() != null && !proxy.getUsername().trim().isEmpty()) {
				args.put("--proxyusername", proxy.getUsername().trim());
			}

			if (proxy.getPassword() != null && !proxy.getPassword().trim().isEmpty()) {
				args.put("--proxypassword", proxy.getPassword().trim());
			}
			
		}
		
		return args;
	}

	// We have to double quote this because the quote needs to survive from GCS -> tribot gradle launcher -> tribot client
	private String quote(String s) {
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			return "\\\\\\\"" + s + "\\\\\\\"";
		}
		return "\"" + s + "\"";
	}
	
}
