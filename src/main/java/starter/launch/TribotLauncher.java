package starter.launch;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
	
	@Override
	public Process launchAccount(ApplicationConfiguration appConfig, PendingLaunch launch) {
		
		final AccountConfiguration account = launch.getAccount();
		final StarterConfiguration settings = launch.getSettings();
		
		System.out.println("Attempting to launch '" + account + "'");
		
		if (!this.triedModify) {
			this.triedModify = true;
			modifyBuildGradle(settings.getCustomTribotPath());
		}
		
		final List<String> args = new ArrayList<>();
		
		args.add(settings.getCustomTribotPath() + File.separator + "tribot-gradle-launcher" + File.separator
				+ (System.getProperty("os.name").toLowerCase().contains("win") ? "gradlew.bat" : "gradlew"));
		
		args.add("runDetached");
		
		boolean usingSplitUsername = false;
		if (account.getUsername().contains(":") && account.getPassword().trim().isEmpty()) {
			final String[] split = account.getUsername().split(":");
			if (split.length == 2) {
				usingSplitUsername = true;
				args.add("--charusername");
				args.add(split[0]);
				args.add("--charpassword");
				args.add(split[1]);
			}
		}
		
		if (!usingSplitUsername) {
			if (!account.getUsername().trim().isEmpty()) {
				args.add("--charusername");
				args.add(account.getUsername());
			}
			if (!account.getPassword().trim().isEmpty()) {
				args.add("--charpassword");
				args.add(account.getPassword());
			}	
		}
		
		if (!account.getPin().trim().isEmpty()) {
			args.add("--charpin");
			args.add(account.getPin());
		}
		
		if (!account.getScript().trim().isEmpty()) {
			args.add("--script");
			args.add(account.getScript());
		}
		if (!account.getArgs().trim().isEmpty()) {
			args.add("--scriptargs");
			args.add(account.getArgs());
		}
		if (!account.getBreakProfile().trim().isEmpty()) {
			args.add("--breakprofile");
			args.add(account.getBreakProfile());
		}
		
		if (launch.getSettings().isLookingGlass()) {
			args.add("--lgpath");
			args.add(launch.getSettings().getLookingGlassPath());
			args.add("--lgdelay");
			args.add("15");
		}
		
		if (!account.getWorld().trim().isEmpty()) {
			final String world;
			try {
				world = WorldUtil.parseWorld(account.getWorld(), settings.worldBlacklist());
			}
			catch (WorldParseException e) {
				e.printStackTrace();
				System.out.println("Failed to parse world");
				return null;
			}
			if (world != null) {
				args.add("--charworld");
				args.add(world);
			}
		}
		
		final ProxyDescriptor proxy = account.getProxy();
		
		if (account.isUseProxy() && proxy != null) {
			
			if (proxy.getIp() != null && !proxy.getIp().trim().isEmpty()) {
				args.add("--proxyhost");
				args.add(proxy.getIp());
			}
			
			if (proxy.getPort() > 0) {
				args.add("--proxyport");
				args.add(proxy.getPort() + "");
			}

			if (proxy.getUsername() != null && !proxy.getUsername().trim().isEmpty()) {
				args.add("--proxyusername");
				args.add(proxy.getUsername());
			}

			if (proxy.getPassword() != null && !proxy.getPassword().trim().isEmpty()) {
				args.add("--proxypassword");
				args.add(proxy.getPassword());
			}
			
		}
		
		if (!account.getHeapSize().trim().isEmpty()) {
			args.add("--mem");
			args.add(account.getHeapSize());
		}
		
		if (settings.isLogin()) {
			args.add("--username");
			args.add(settings.getTribotUsername());
			args.add("--password");
			args.add(settings.getTribotPassword());
		}
		if (settings.isSupplySid()) {
			args.add("--sid");
			args.add(settings.getSid());
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
		pb.environment().put("JAVA_HOME", new File(tribotPath, "jre").getAbsolutePath());
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
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to modify build.gradle");
		}	
	}
	
}
