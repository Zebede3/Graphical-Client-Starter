package starter.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import starter.models.AccountConfiguration;
import starter.models.ApplicationConfiguration;
import starter.models.PendingLaunch;
import starter.models.ProxyDescriptor;
import starter.models.ScriptCommand;
import starter.models.SemanticVersion;
import starter.models.StarterConfiguration;
import starter.util.FileUtil;
import starter.util.WorldParseException;
import starter.util.WorldUtil;

public class LaunchProcessor {
	
	private static final Pattern TRIBOT_VERSION_PATTERN = Pattern.compile(".*" + Pattern.quote("TRiBot-") + "(\\d+)\\.(\\d+)\\.(\\d+)(" + Pattern.quote("-beta") + "\\d*)?" + Pattern.quote(".jar"));
	
	private static final String LG_SCRIPT_NAME = "Looking Glass Starter";
	
	private final ObservableList<PendingLaunch> backlog; // this should only be modified on the fx thread
	private final ApplicationConfiguration config;
	
	private volatile PendingLaunch toLaunch;
	
	public LaunchProcessor(ApplicationConfiguration config) {
		this.config = config;
		this.backlog = FXCollections.observableArrayList();
		new Thread(this::run).start();
	}
	
	public ObservableList<PendingLaunch> getBacklog() {
		return this.backlog;
	}
	
	public void launchClients(StarterConfiguration config) {
		final PendingLaunch[] pending = config.getAccounts().stream()
				.filter(AccountConfiguration::isSelected)
				.map(a -> new PendingLaunch(a, config))
				.toArray(PendingLaunch[]::new);
		Platform.runLater(() -> {
			this.backlog.addAll(pending);
			this.backlog.sort(Comparator.naturalOrder());
			System.out.println("Added " + pending.length + " account" + (pending.length == 1 ? "" : "s") + " to launch backlog");
		});
	}
	
	public synchronized boolean hasRemainingLaunches() {
		return !this.backlog.isEmpty() || this.toLaunch != null;
	}
	
	private void run() {
		
		while (true) {
			
			final PendingLaunch acc = this.toLaunch;
			
			if (acc != null) {
				final boolean success = this.launchAccount(acc);
				System.out.println("Launch " + (success ? "succeeded" : "failed"));
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
					
					final CountDownLatch latch = new CountDownLatch(1);
					
					Platform.runLater(() -> {
						
						synchronized (this) {
						
							// double check some things first
							
							if (this.toLaunch != null)
								return;
							
							if (this.getBacklog().isEmpty())
								return;
							
							final PendingLaunch next = this.getBacklog().get(0);
							if (next.isReadyToLaunch()) {
								this.getBacklog().remove(next);
								System.out.println("Pulled '" + next + "' from launch backlog");
								this.toLaunch = next;
							}
							
							latch.countDown();
						}
						
					});
					
					// this thread has nothing to do until the above code runs, so just wait
					try {
						latch.await();
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// next account exists but is not ready yet
					if (this.toLaunch == null) {
						try {
							Thread.sleep(1000L);
						} 
						catch (InterruptedException e) {}
					}
					
				}
				else {
					try {
						Thread.sleep(1000L);
					} 
					catch (InterruptedException e) {}
				}
				
			}
			
		}
		
	}

	private boolean launchAccount(PendingLaunch launch) {
		
		final AccountConfiguration account = launch.getAccount();
		final StarterConfiguration settings = launch.getSettings();
		
		System.out.println("Attempting to launch '" + account + "'");
		
		final List<String> args = new ArrayList<>();
		
		args.add(getJavaCommand(launch));
		
		args.add("-jar");
		
		final String path = findTribotPath(launch);
		if (path == null) {
			System.out.println("Error determining tribot path");
			return false;
		}
		args.add(path);
		
		if (!account.getUsername().trim().isEmpty()) {
			args.add("--charusername");
			args.add(account.getUsername());
		}
		
		if (!account.getPassword().trim().isEmpty()) {
			args.add("--charpassword");
			args.add(account.getPassword());
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
				return false;
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
		
		if (!this.config.isDebugMode())
			args.add("--detach");
		else
			args.add("--attach");
		
		System.out.println("Launching client: " + args.toString());
		
		try {
			if (this.config.isDebugMode()) {
				final Process process = new ProcessBuilder()
						.redirectErrorStream(true)
						.redirectInput(FileUtil.NULL_FILE)
						.command(args)
						.redirectOutput(Redirect.PIPE)
						.start();
				final InputStream is = process.getInputStream();
				new Thread(() -> {
					try (final BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
						br.lines().forEach(System.out::println);
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Finished debugging " + args);
					System.out.println(process.isAlive() + "");
				}).start();
			}
			else {
				new ProcessBuilder()
				.redirectErrorStream(true)
				.redirectInput(FileUtil.NULL_FILE)
				.redirectOutput(FileUtil.NULL_FILE)
				.command(args)
				.start();
			}
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to launch client");
			return false;
		}
	}
	
//	private boolean launchLookingGlassClient(PendingLaunch acc) {
//		
//		final List<String> args = new ArrayList<>();
//		
//		args.add(getJavaCommand(acc));
//		args.add("-jar");
//		args.add(acc.getSettings().getLookingGlassPath());
//		
//		try {
//			new ProcessBuilder()
//			.redirectErrorStream(true)
//			.redirectInput(FileUtil.NULL_FILE)
//			.redirectOutput(FileUtil.NULL_FILE)
//			.command(args)
//			.start();
//			return true;
//		} 
//		catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
	
	private String getJavaCommand(PendingLaunch acc) {
		if (acc.getSettings().isUseCustomJavaPath())
			return acc.getSettings().getCustomJavaPath() + File.separator + "java";
		return "java";
	}
	
	private String findTribotPath(PendingLaunch launch) {
		if (launch.getSettings().isUseCustomTribotPath())
			return launch.getSettings().getCustomTribotPath();
		try {
			return Files.list(new File(FileUtil.getAppDataDirectory().getAbsolutePath() + File.separator + "dependancies" + File.separator).toPath())
			.map(Path::toFile)
			.map(f -> TRIBOT_VERSION_PATTERN.matcher(f.getAbsolutePath()))
			.filter(Matcher::matches)
			.map(matcher -> new Pair<>(matcher.group(0), extractVersion(matcher)))
			.sorted(Comparator.<Pair<String, SemanticVersion>, SemanticVersion>comparing(Pair::getValue).reversed())
			.map(Pair::getKey)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Failed to find tribot jar"));
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private SemanticVersion extractVersion(Matcher matcher) {
		final int mostSig = Integer.parseInt(matcher.group(1));
		final int medSig = Integer.parseInt(matcher.group(2));
		final int leastSig = Integer.parseInt(matcher.group(3));
		return new SemanticVersion(mostSig, medSig, leastSig);
	}
	
}
