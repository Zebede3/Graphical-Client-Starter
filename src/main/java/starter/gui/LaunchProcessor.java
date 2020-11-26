package starter.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import starter.launch.ClientLauncher;
import starter.launch.TribotLauncher;
import starter.models.AccountConfiguration;
import starter.models.ApplicationConfiguration;
import starter.models.PendingLaunch;
import starter.models.StarterConfiguration;

public class LaunchProcessor {
	
	private static final Pattern LAUNCHED_PID_REGEX = Pattern.compile(".*Launched Client Process ID: (\\d+).*");
	
	private final ObservableList<PendingLaunch> backlog; // this should only be modified on the fx thread
	private final ApplicationConfiguration config;
	private final ActiveClientObserver activeClientObserver;
	
	private final ClientLauncher launcher;
	
	private volatile PendingLaunch toLaunch;
	
	public LaunchProcessor(ApplicationConfiguration config, ActiveClientObserver activeClientObserver) {
		this.config = config;
		this.backlog = FXCollections.observableArrayList();
		this.activeClientObserver = activeClientObserver;
		this.launcher = new TribotLauncher();
		new Thread(this::run).start();
	}
	
	public ObservableList<PendingLaunch> getBacklog() {
		return this.backlog;
	}
	
	public void launchClients(StarterConfiguration config) {
		Platform.runLater(() -> {
			System.out.println("Launching " + (config.isOnlyLaunchInactiveAccounts() ? "Selected Inactive Accounts" : "Selected Accounts"));
			final PendingLaunch[] pending = config.getAccounts().stream()
					.filter(AccountConfiguration::isSelected)
					.filter(acc -> !config.isOnlyLaunchInactiveAccounts() || !this.activeClientObserver.isActive(acc))
					.collect(Collectors.groupingBy(a -> a.getClient().trim(), LinkedHashMap::new, Collectors.toList()))
					.entrySet()
					.stream()
					.map(a -> {
						if (a.getKey().isEmpty()) {
							if (config.isAutoBatchAccounts()) {
								final AtomicInteger current = new AtomicInteger(0);
								final Map<AccountConfiguration, Integer> clientIndexes = new HashMap<>();
								final int batchSize = config.getAutoBatchAccountQuantity() > 0 ? config.getAutoBatchAccountQuantity() : 1;
								return a.getValue().stream()
								.collect(Collectors.groupingBy(val -> clientIndexes.computeIfAbsent(val, (v) -> current.getAndIncrement()) / batchSize, LinkedHashMap::new, Collectors.toList()))
								.entrySet()
								.stream()
								.map(e -> {
									return new PendingLaunch(config, "Auto Batch " + e.getKey() + " / " + System.currentTimeMillis() + " (Size: " + e.getValue().size() + ")", e.getValue().toArray(new AccountConfiguration[0]));
								})
								.collect(Collectors.toList());
							}
							
							return a.getValue().stream().map(acc -> new PendingLaunch(config, acc)).collect(Collectors.toList());
						}
						
						return Arrays.asList(new PendingLaunch(config, a.getValue().toArray(new AccountConfiguration[0])));
					})
					.flatMap(Collection::stream)
					.toArray(PendingLaunch[]::new);
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
	
	private boolean launchAccount(PendingLaunch pending) {
		if (pending.getSettings().isOnlyLaunchInactiveAccounts()) {
			final List<AccountConfiguration> active = new ArrayList<>();
			final CountDownLatch latch = new CountDownLatch(1);
			final PendingLaunch finalCopy = pending;
			Platform.runLater(() -> {
				for (AccountConfiguration acc : finalCopy.getAccounts()) {
					if (this.activeClientObserver.isActive(acc)) {
						active.add(acc);
					}
				}
				latch.countDown();
			});
			try {
				latch.await();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (active.size() > 0) {
				System.out.println("The following accounts were doubled check at launch time and are already active. Filtering accounts from launch:");
				active.forEach(a -> System.out.println(a));
				pending = pending.withFilteredAccounts(active);
				if (pending.getAccounts().length == 0) {
					System.out.println("After filtering, the client to launch is empty. Skipping launch.");
					return true;
				}
			}
		}
		final Process process = this.launcher.launchAccount(this.config, pending);
		if (process == null) {
			return false;
		}
		final InputStream is = process.getInputStream();
		final PendingLaunch finalCopy = pending;
		new Thread(() -> {
			try (final BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				while (true) {
					final String line = br.readLine();
					if (line == null) {
						System.out.println("Failed to find active client after launch for: " + finalCopy);
						break;
					}
					if (this.config.isDebugMode()) {
						System.out.println(line);
					}
					final Matcher matcher = LAUNCHED_PID_REGEX.matcher(line);
					if (matcher.matches()) {
						final long pid = Long.parseLong(matcher.group(1));
						ProcessHandle.of(pid).ifPresent(handle -> {
							this.activeClientObserver.clientLaunched(handle, finalCopy);
						});
						break;
					}
				}
				if (this.config.isDebugMode()) {
					br.lines().forEach(System.out::println);	
				}
				else {
					br.transferTo(Writer.nullWriter());
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			if (this.config.isDebugMode()) {
				System.out.println("Finished debugging " + finalCopy);
				System.out.println("Process is alive: " + process.isAlive());	
			}
		}).start();
		return true;
	}
	
}
