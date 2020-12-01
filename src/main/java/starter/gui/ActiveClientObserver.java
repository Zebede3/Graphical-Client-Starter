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
	
	// don't rewrite file
	private volatile boolean loading = false;
	
	public ActiveClientObserver(ScheduledExecutorService exec) {
		this.exec = exec;
		this.active = FXCollections.observableArrayList();
		this.shutdownTasks = new WeakHashMap<>();
		final Gson gson = GsonFactory.buildGson();
		exec.submit(() -> {
			try {
				final String cached = Files.readString(FileUtil.getActiveClientCacheFile().toPath());
				final CachedActiveClient[] clients = gson.fromJson(cached, CachedActiveClient[].class);
				if (clients != null) {
					for (CachedActiveClient c : clients) {
						ProcessHandle.of(c.getPid()).ifPresent(handle -> {
							handle.info().startInstant().ifPresent(start -> {
								if (start.toEpochMilli() == c.getStart()) {
									final ActiveClient active = new ActiveClient(handle, c.getDesc(), c.getAccountNames(), c.getStart(), c.getShutdownTime());
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
			}
			catch (IOException e) {
				
			}
		});
		this.active.addListener((Change<?> change) -> {
			if (this.loading) {
				return;
			}
			final CachedActiveClient[] cached = this.active.stream().map(item -> {
				return new CachedActiveClient(item.getAccountNames(), item.getDesc(), item.getProcess().pid(), item.getStart(), item.getShutdownTime());
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
	
	private void scheduleShutdown(ActiveClient client) {
		if (client.getShutdownTime() == null) {
			return;
		}
		final long remaining = LocalDateTime.now().atZone(ZoneId.systemDefault()).until(Instant.ofEpochMilli(client.getShutdownTime()).atZone(ZoneId.systemDefault()), ChronoUnit.MILLIS);
		final Future<?> shutdownTask = this.exec.schedule(() -> {
			System.out.println("Killing client for scheduled shutdown: " + active);
			client.getProcess().destroy();
		}, remaining, TimeUnit.MILLISECONDS);
		synchronized (this.shutdownTasks) {
			this.shutdownTasks.put(client, shutdownTask);
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
		
		public CachedActiveClient(String[] accountNames, String desc, long pid, long start, Long shutdownTime) {
			this.desc = desc;
			this.accountNames = accountNames;
			this.pid = pid;
			this.start = start;
			this.shutdownTime = shutdownTime;
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
		
	}
	
}
