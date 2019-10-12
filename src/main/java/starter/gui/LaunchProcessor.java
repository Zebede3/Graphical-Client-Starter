package starter.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import starter.models.AccountConfiguration;
import starter.models.PendingLaunch;
import starter.models.ScriptCommand;
import starter.models.StarterConfiguration;
import starter.util.FileUtil;
import starter.util.WorldParseException;
import starter.util.WorldUtil;

public class LaunchProcessor {
	
	private static final Pattern TRIBOT_VERSION_PATTERN = Pattern.compile(".*" + Pattern.quote("TRiBot-") + "(\\d+)\\.(\\d+)\\.(\\d+)" + Pattern.quote(".jar"));
	
	private static final String LG_SCRIPT_NAME = "Looking Glass Starter";
	
	private final ObservableList<PendingLaunch> backlog; // this should only be modified on the fx thread
	
	private volatile PendingLaunch toLaunch;
	
	public LaunchProcessor() {
		this.backlog = FXCollections.observableArrayList();
		new Thread(this::run).start();
	}
	
	public ObservableList<PendingLaunch> getBacklog() {
		return this.backlog;
	}
	
	public void launchClients(StarterConfiguration config) {
		Platform.runLater(() -> {
			this.backlog.addAll(config.getAccounts().stream()
								.filter(AccountConfiguration::isSelected)
								.map(a -> new PendingLaunch(a, config))
								.toArray(PendingLaunch[]::new));
		});
	}
	
	private void run() {
		
		while (true) {
			
			final PendingLaunch acc = this.toLaunch;
			
			if (acc != null) {
				this.launchAccount(acc);
				final boolean more = !this.backlog.isEmpty();
				this.toLaunch = null;
				if (more) {
					final long end = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(acc.getSettings().getDelayBetweenLaunch(), TimeUnit.SECONDS);
					System.out.println("Waiting " + acc.getSettings().getDelayBetweenLaunch() + " seconds before next launch");
					while (System.currentTimeMillis() < end) {
						try {
							Thread.sleep(1000);
						}
						catch (InterruptedException e) {}
					}	
				}
			}
			else {
				
				// check for newly launchable account
				// can safely check isEmpty on separate thread
				if (!this.getBacklog().isEmpty()) {
					
					Platform.runLater(() -> {
						
						synchronized (this) {
						
							// double check some things first
							
							if (this.toLaunch != null)
								return;
							
							if (this.getBacklog().isEmpty())
								return;
							
							final PendingLaunch account = this.getBacklog().remove(0);
							System.out.println("Pulled '" + account + "' from launch backlog");
							this.toLaunch = account;
						
						}
						
					});
					
				}
				
				try {
					Thread.sleep(1000L);
				} 
				catch (InterruptedException e) {}
				
			}
			
		}
		
	}

	private boolean launchAccount(PendingLaunch launch) {
		
		final AccountConfiguration account = launch.getAccount();
		final StarterConfiguration settings = launch.getSettings();
		
		System.out.println("Attempting to launch '" + account + "'");
		
		final List<String> args = new ArrayList<>();
		
		args.add("java");
		
		args.add("-jar");
		
		final String path = findTribotPath(launch);
		if (path == null) {
			System.out.println("Error determining tribot path");
			return false;
		}
		args.add(path);
		
		if (!account.getUsername().isEmpty()) {
			args.add("--charusername");
			args.add(account.getUsername());
		}
		
		if (!account.getPassword().isEmpty()) {
			args.add("--charpassword");
			args.add(account.getPassword());
		}
		
		if (!account.getPin().isEmpty()) {
			args.add("--charpin");
			args.add(account.getPin());
		}
		
		if (launch.getSettings().isLookingGlass()) {
			
			if (!launchLookingGlassClient(launch))
				return false;
		
			args.add("--script");
			args.add(LG_SCRIPT_NAME);
		
			final String script = account.getScript();
			final String scriptArgs = account.getArgs();
			final String acc = account.getUsername();
			final String breakProfile = account.getBreakProfile();
			
			final ScriptCommand command = new ScriptCommand(script, scriptArgs, breakProfile, acc);
			
			args.add("--scriptargs");
			args.add(new String(Base64.getEncoder().encode(new Gson().toJson(command).getBytes())));
			
		}
		else {
			if (!account.getScript().isEmpty()) {
				args.add("--script");
				args.add(account.getScript());
			}
			if (!account.getArgs().isEmpty()) {
				args.add("--scriptargs");
				args.add(account.getArgs());
			}
			if (!account.getBreakProfile().isEmpty()) {
				args.add("--breakprofile");
				args.add(account.getBreakProfile());
			}
		}
		
		if (!account.getWorld().isEmpty()) {
			final String world;
			try {
				world = WorldUtil.parseWorld(account.getWorld());
			}
			catch (WorldParseException e) {
				e.printStackTrace();
				System.out.println("Failed to parse world");
				return false;
			}
			args.add("--charworld");
			args.add(world);
		}
		
		if (account.isUseProxy() && account.getProxy() != null) {
			
			if (!account.getProxy().getIp().isEmpty()) {
				args.add("--proxyhost");
				args.add(account.getProxy().getIp());
			}
			
			if (account.getProxy().getPort() > 0) {
				args.add("--proxyport");
				args.add(account.getProxy().getPort() + "");
			}

			if (account.getProxy().getUsername() != null && !account.getProxy().getUsername().isEmpty()) {
				args.add("--proxyusername");
				args.add(account.getProxy().getUsername());
			}

			if (account.getProxy().getPassword() != null && !account.getProxy().getPassword().isEmpty()) {
				args.add("--proxypassword");
				args.add(account.getProxy().getPassword());
			}
			
		}
		
		if (!account.getHeapSize().isEmpty()) {
			args.add("--mem");
			args.add(account.getHeapSize());
		}
		
		if (settings.isLogin()) {
			args.add("--username");
			args.add(settings.getTribotUsername());
			args.add("--password");
			args.add(settings.getTribotPassword());
		}
		else if (settings.isSupplySid()) {
			args.add("--sid");
			args.add(settings.getSid());
		}
		
		System.out.println("Launching client: " + args.toString());
		
		try {
			new ProcessBuilder()
			.redirectErrorStream(true)
			.redirectInput(FileUtil.NULL_FILE)
			.redirectOutput(FileUtil.NULL_FILE)
			.command(args)
			.start();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to launch client");
			return false;
		}
	}
	
	private boolean launchLookingGlassClient(PendingLaunch acc) {
		
		final List<String> args = new ArrayList<>();
		
		args.add("java");
		args.add("-jar");
		args.add(acc.getSettings().getLookingGlassPath());
		
		try {
			new ProcessBuilder()
			.redirectErrorStream(true)
			.redirectInput(FileUtil.NULL_FILE)
			.redirectOutput(FileUtil.NULL_FILE)
			.command(args)
			.start();
			return true;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String findTribotPath(PendingLaunch launch) {
		if (launch.getSettings().isUseCustomTribotPath())
			return launch.getSettings().getCustomTribotPath();
		try {
			return Files.list(new File(FileUtil.getAppDataDirectory().getAbsolutePath() + File.separator + "dependancies" + File.separator).toPath())
			.map(Path::toFile)
			.map(f -> TRIBOT_VERSION_PATTERN.matcher(f.getAbsolutePath()))
			.filter(Matcher::matches)
			.sorted(Comparator.comparingInt(this::extractVersion).reversed())
			.map(m -> m.group(0))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Failed to find tribot jar"));
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private int extractVersion(Matcher matcher) {
		final int mostSig = Integer.parseInt(matcher.group(1));
		final int medSig = Integer.parseInt(matcher.group(2));
		final int leastSig = Integer.parseInt(matcher.group(3));
		return ((mostSig << 24) | (medSig << 12) | leastSig);
	}
	
}
