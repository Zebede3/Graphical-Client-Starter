package starter.models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ActiveClient {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	private final ProcessHandle process;
	private final long start;
	private final String desc;
	private final String key;
	
	public ActiveClient(ProcessHandle process, String desc, String key, long start) {
		this.process = process;
		this.desc = desc;
		this.start = start;
		this.key = key;
	}
	
	public ActiveClient(ProcessHandle process, PendingLaunch launch) {
		this(process, launch.getAccount().toString(), launch.getAccount().getUsername(), process.info().startInstant().map(i -> i.toEpochMilli()).orElse(System.currentTimeMillis()));
	}
	
	public String getKey() {
		return this.key;
	}
	
	public ProcessHandle getProcess() {
		return this.process;
	}
	
	public long getStart() {
		return this.start;
	}
	
	public String getDesc() {
		return this.desc;
	}
	
	@Override
	public String toString() {
		final LocalDateTime dt = Instant.ofEpochMilli(this.start).atZone(ZoneId.systemDefault()).toLocalDateTime();
		return "[Launched: " + formatter.format(dt) + "] [PID: " + this.process.pid() + "] "  + this.desc;
	}
	
}
