package starter.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import starter.gson.GsonFactory;
import starter.models.AccountConfiguration;
import starter.models.ActiveClient;
import starter.models.PendingLaunch;
import starter.util.FileUtil;

public class ActiveClientObserver {

	private final ObservableList<ActiveClient> active;

	// don't rewrite file
	private volatile boolean loading = false;
	
	public ActiveClientObserver() {
		this.active = FXCollections.observableArrayList();
		final Gson gson = GsonFactory.buildGson();
		final ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.submit(() -> {
			try {
				final String cached = Files.readString(FileUtil.getActiveClientCacheFile().toPath());
				final CachedActiveClient[] clients = gson.fromJson(cached, CachedActiveClient[].class);
				if (clients != null) {
					for (CachedActiveClient c : clients) {
						ProcessHandle.of(c.getPid()).ifPresent(handle -> {
							handle.info().startInstant().ifPresent(start -> {
								if (start.toEpochMilli() == c.getStart()) {
									final ActiveClient active = new ActiveClient(handle, c.getDesc(), c.getKey(), c.getStart());
									System.out.println("Found previously active client: " + active);
									Platform.runLater(() -> {
										this.loading = true;
										this.active.add(active);
										this.loading = false;
									});
									handle.onExit().thenAccept(ph -> {
										Platform.runLater(() -> {
											this.active.remove(active);
										});
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
				return new CachedActiveClient(item.getKey(), item.getDesc(), item.getProcess().pid(), item.getStart());
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
	
	public ObservableList<ActiveClient> getActive() {
		return this.active;
	}
	
	public boolean isActive(AccountConfiguration acc) {
		return this.active.stream().anyMatch(c -> Objects.equals(c.getKey(), acc.getUsername()));
	}
	
	public void clientLaunched(ProcessHandle process, PendingLaunch launchConfig) {
		final ActiveClient active = new ActiveClient(process, launchConfig);
		System.out.println("Found active client: " + active);
		Platform.runLater(() -> {
			this.active.add(active);
		});
		process.onExit().thenAccept(ph -> {
			Platform.runLater(() -> {
				this.active.remove(active);
			});
		});
	}
	
	// keep as lightweight as possible
	private static class CachedActiveClient {
		
		private final String desc;
		private final long pid;
		private final long start;
		private final String key;
		
		public CachedActiveClient(String key, String desc, long pid, long start) {
			this.desc = desc;
			this.key = key;
			this.pid = pid;
			this.start = start;
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
		
		public String getKey() {
			return this.key;
		}
		
	}
	
}
