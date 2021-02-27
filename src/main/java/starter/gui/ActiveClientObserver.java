package starter.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import starter.gson.GsonFactory;
import starter.models.AccountConfiguration;
import starter.models.ActiveClient;
import starter.models.PendingLaunch;
import starter.models.StarterConfiguration;
import starter.util.FileUtil;

public class ActiveClientObserver {

	private final ObservableList<ActiveClient> active;
	private final ScheduledExecutorService exec;
	private final Map<ActiveClient, Future<?>> shutdownTasks;
	private final BiConsumer<ActiveClient, Boolean> relauncher;
	
	// don't rewrite file
	private volatile boolean loading = false;
	
	public ActiveClientObserver(ScheduledExecutorService exec, BiConsumer<ActiveClient, Boolean> relauncher) {
		this.exec = exec;
		this.active = FXCollections.observableArrayList();
		this.shutdownTasks = new WeakHashMap<>();
		this.relauncher = relauncher;
		final Gson gson = GsonFactory.buildGson();
		exec.submit(() -> {
			try {
				final String cached = Files.readString(FileUtil.getActiveClientCacheFile().toPath());
				final CachedActiveClient[] clients = gson.fromJson(cached, CachedActiveClient[].class);
				if (clients != null) {
					reloadCachedClients(clients);
				}
			}
			catch (IOException e) {
				
			}
		});
		this.active.addListener((Change<?> change) -> {
			if (this.loading) {
				return;
			}
			final CachedActiveClient[] cached = this.active.stream().map(item -> {
				return new CachedActiveClient(item.getAccountNames(), item.getDesc(), item.getProcess().pid(), item.getStart(), item.getShutdownTime(), item.getRelaunchAfterShutdownMinutes());
			}).toArray(CachedActiveClient[]::new);
			exec.submit(() -> {
				final String s = gson.toJson(cached);
				try {
					Files.write(FileUtil.getActiveClientCacheFile().toPath(), s.getBytes());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			});
		});
	}
	
	public void applyShutdownSettingsToAllActive(StarterConfiguration config) {
		synchronized (this.shutdownTasks) {
			this.shutdownTasks.values().forEach(f -> {
				f.cancel(false);
			});
			this.shutdownTasks.clear();
			this.active.forEach(client -> {
				client.adjustShutdownTime(config);
				scheduleShutdown(client);
			});
		}
	}
	
	public void shutdown(ActiveClient client) {
		//client.getProcess().descendants().forEach(ProcessHandle::destroy);
		client.getProcess().destroy();
		System.out.println("Destroyed process id: " + client.getProcess().pid());
	}
	
	private void scheduleShutdown(ActiveClient client) {
		if (client.getShutdownTime() == null) {
			return;
		}
		final long remaining = LocalDateTime.now().atZone(ZoneId.systemDefault()).until(Instant.ofEpochMilli(client.getShutdownTime()).atZone(ZoneId.systemDefault()), ChronoUnit.MILLIS);
		final Future<?> shutdownTask = this.exec.schedule(() -> {
			System.out.println("Killing client for scheduled shutdown: " + active);
			shutdown(client);
			if (client.getRelaunchAfterShutdownMinutes() != null && client.getRelaunchAfterShutdownMinutes() >= 0) {
				System.out.println("Re-scheduling client for launch based on settings; relaunching in " + client.getRelaunchAfterShutdownMinutes() + " minutes");
				this.relauncher.accept(client, true);
			}
		}, remaining, TimeUnit.MILLISECONDS);
		synchronized (this.shutdownTasks) {
			this.shutdownTasks.put(client, shutdownTask);
		}
	}
	
	private void reloadCachedClients(CachedActiveClient[] clients) {
		for (CachedActiveClient c : clients) {
			if (c.getAccountNames() == null) {
				System.out.println("Client " + c + " is missing account names. Probably from an older gcs version. Skipping cached active client.");
				continue;
			}
			ProcessHandle.of(c.getPid()).ifPresent(handle -> {
				handle.info().startInstant().ifPresent(start -> {
					if (start.toEpochMilli() == c.getStart()) {
						final ActiveClient active = new ActiveClient(handle, c.getDesc(), c.getAccountNames(), c.getStart(), c.getShutdownTime(), c.getRelaunchAfterShutdownMinutes());
						System.out.println("Found previously active client: " + active);
						Platform.runLater(() -> {
							this.loading = true;
							this.active.add(active);
							this.loading = false;
						});
						if (active.getShutdownTime() != null) {
							scheduleShutdown(active);
						}
						handle.onExit().thenAccept(ph -> {
							System.out.println("Client process ended: " + active);
							Platform.runLater(() -> {
								this.active.remove(active);
							});
							synchronized (this.shutdownTasks) {
								final Future<?> shutdown = this.shutdownTasks.remove(active);
								if (shutdown != null) {
									shutdown.cancel(false);
								}
							}
						});
					}
				});
			});
		}
	}
	
	public ObservableList<ActiveClient> getActive() {
		return this.active;
	}
	
	public boolean isActive(AccountConfiguration acc) {
		return this.active.stream().anyMatch(c -> Arrays.stream(c.getAccountNames()).anyMatch(a -> Objects.equals(a, acc.getUsername())));
	}
	
	public void clientLaunched(ProcessHandle process, PendingLaunch launchConfig) {
		final ActiveClient active = new ActiveClient(process, launchConfig);
		System.out.println("Found active client: " + active);
		Platform.runLater(() -> {
			this.active.add(active);
		});
		if (active.getShutdownTime() != null) {
			scheduleShutdown(active);
		}
		process.onExit().thenAccept(ph -> {
			System.out.println("Client process ended: " + active);
			Platform.runLater(() -> {
				this.active.remove(active);
			});
			synchronized (this.shutdownTasks) {
				final Future<?> shutdown = this.shutdownTasks.remove(active);
				if (shutdown != null) {
					shutdown.cancel(false);
				}
			}
		});
	}
	
	// keep as lightweight as possible
	private static class CachedActiveClient {
		
		private final String desc;
		private final long pid;
		private final long start;
		private final String[] accountNames;
		private final Long shutdownTime;
		private final Integer relaunchAfterShutdownMinutes;
		
		public CachedActiveClient(String[] accountNames, String desc, long pid, long start, Long shutdownTime, Integer relaunchAfterShutdownMinutes) {
			this.desc = desc;
			this.accountNames = accountNames;
			this.pid = pid;
			this.start = start;
			this.shutdownTime = shutdownTime;
			this.relaunchAfterShutdownMinutes = relaunchAfterShutdownMinutes;
		}
		
		public long getStart() {
			return this.start;
		}

		public String getDesc() {
			return this.desc;
		}

		public long getPid() {
			return this.pid;
		}
		
		public String[] getAccountNames() {
			return this.accountNames;
		}
		
		public Long getShutdownTime() {
			return this.shutdownTime;
		}

		public Integer getRelaunchAfterShutdownMinutes() {
			return this.relaunchAfterShutdownMinutes;
		}
		
	}
	
}
