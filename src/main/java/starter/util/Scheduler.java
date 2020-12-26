package starter.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Scheduler {

	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	
	public static ScheduledExecutorService executor() {
		return executor;
	}
	
}
