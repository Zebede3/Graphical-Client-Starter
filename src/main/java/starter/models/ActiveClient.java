package starter.models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ActiveClient {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	private final ProcessHandle process;
	private final long start;
	private final String desc;
	private final String[] accounts;
	
	public ActiveClient(ProcessHandle process, String desc, String[] accountNames, long start) {
		this.process = process;
		this.desc = desc;
		this.start = start;
		this.accounts = accountNames;
	}
	
	public ActiveClient(ProcessHandle process, PendingLaunch launch) {
		this(process, launch.getName(), Arrays.stream(launch.getAccounts()).map(a -> a.getUsername()).toArray(String[]::new), process.info().startInstant().map(i -> i.toEpochMilli()).orElse(System.currentTimeMillis()));
	}
	
	public String[] getAccountNames() {
		return this.accounts;
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
