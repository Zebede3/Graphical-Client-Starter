package starter.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import starter.GraphicalClientStarter;
import starter.models.AccountConfiguration;
import starter.models.PendingLaunch;
import starter.models.ScriptCommand;
import starter.models.StarterConfiguration;
import starter.util.FileUtil;

public class LaunchProcessor {
	
	private static final String LG_SCRIPT_NAME = "LookingGlassStarter";
	
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
		
		System.out.println("Attempting to launch '" + account + "'");
		
		final Map<String, String> args = new LinkedHashMap<>(); // preserve order for printing args
		
		args.put("accountName", account.getUsername());
		
		if (launch.getSettings().isLookingGlass()) {
			
			if (!launchLookingGlassClient(launch))
				return false;
		
			args.put("scriptName", LG_SCRIPT_NAME);
		
			final String script = account.getScript();
			final String scriptArgs = account.getArgs();
			final String acc = account.getUsername();
			final String breakProfile = account.getBreakProfile();
			
			final ScriptCommand command = new ScriptCommand(script, scriptArgs, breakProfile, acc);
			
			args.put("scriptCommand", new String(Base64.getEncoder().encode(new Gson().toJson(command).getBytes())));
			
		}
		else {
			args.put("scriptName", account.getScript());
			if (!account.getArgs().isEmpty())
				args.put("scriptCommand", account.getArgs());
			if (!account.getBreakProfile().isEmpty())
				args.put("breakProfile", account.getBreakProfile());
		}
		
		if (!account.getWorld().isEmpty())
			args.put("world", account.getWorld());
		
		if (account.isUseProxy() && account.getProxy() != null) {
			
			args.put("proxyIP", account.getProxy().getIp());
			args.put("proxyPort", account.getProxy().getPort() + "");

			if (account.getProxy().getUsername() != null && !account.getProxy().getUsername().isEmpty())
				args.put("proxyUsername", account.getProxy().getUsername());

			if (account.getProxy().getPassword() != null && !account.getProxy().getPassword().isEmpty())
				args.put("proxyPassword", account.getProxy().getPassword());
			
		}
		
		if (!account.getHeapSize().isEmpty())
			args.put("heapSize", account.getHeapSize());
		
		final String[] argsArray = args.entrySet().stream()
									.map(e -> e.getKey() + "~" + e.getValue())
									.toArray(String[]::new);
		
		System.out.println("Launching client: " + Arrays.toString(argsArray));
		
		return GraphicalClientStarter.launchClient(argsArray);
		
//		try {
//			Class.forName("StarterNew")
//			.getDeclaredMethod("main", String[].class)
//			.invoke(null, new Object[] { argsArray });
//			return true;
//		} 
//		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
//				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
//			e.printStackTrace();
//			System.out.println("Failed to run command, args: " + Arrays.toString(argsArray));
//			return false;
//		}
		
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
	
}
