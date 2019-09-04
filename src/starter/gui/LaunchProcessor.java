package starter.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import starter.models.AccountConfiguration;
import starter.models.StarterConfiguration;

public class LaunchProcessor {
	
	private final ObservableList<AccountConfiguration> backlog; // this should only be modified on the fx thread
	
	private volatile AccountConfiguration toLaunch;
	
	private int timeBetweenLaunch;
	
	public LaunchProcessor() {
		this.backlog = FXCollections.observableArrayList();
		this.timeBetweenLaunch = 30;
		new Thread(this::run).start();
	}
	
	public ObservableList<AccountConfiguration> getBacklog() {
		return this.backlog;
	}
	
	public void launchClients(StarterConfiguration config) {
		this.timeBetweenLaunch = config.getDelayBetweenLaunch();
		Platform.runLater(() -> {
			this.backlog.addAll(config.getAccounts().stream().filter(AccountConfiguration::isSelected).toArray(AccountConfiguration[]::new));
		});
	}
	
	private void run() {
		
		while (true) {
			
			if (this.toLaunch != null) {
				this.launchAccount(this.toLaunch);
				final boolean more = !this.backlog.isEmpty();
				this.toLaunch = null;
				if (more) {
					final long end = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(this.timeBetweenLaunch, TimeUnit.SECONDS);
					System.out.println("Waiting " + this.timeBetweenLaunch + " seconds before next launch");
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
						
							if (this.toLaunch != null)
								return;
							
							if (!this.getBacklog().isEmpty()) { // double check to ensure not empty
								final AccountConfiguration account = this.getBacklog().remove(0);
								System.out.println("Pulled '" + account + "' from launch backlog");
								this.toLaunch = account;
							}
						
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

	private boolean launchAccount(AccountConfiguration account) {
		
		System.out.println("Attempting to launch '" + account + "'");
		
		final Map<String, String> args = new LinkedHashMap<>(); // preserve order for printing args
		
		args.put("accountName", account.getUsername());
		args.put("scriptName", account.getScript());
		
		if (!account.getWorld().isEmpty())
			args.put("world", account.getWorld());
		
		if (!account.getBreakProfile().isEmpty())
			args.put("breakProfile", account.getBreakProfile());
		
		if (!account.getArgs().isEmpty())
			args.put("scriptCommand", account.getArgs());
		
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
		
		try {
			Class.forName("StarterNew")
			.getDeclaredMethod("main", String[].class)
			.invoke(null, new Object[] { argsArray });
			return true;
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Failed to run command, args: " + Arrays.toString(argsArray));
			return false;
		}
		
	}
	
}
