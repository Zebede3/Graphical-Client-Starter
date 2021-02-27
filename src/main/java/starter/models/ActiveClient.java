package starter.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ActiveClient {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	private final ProcessHandle process;
	private final long start;
	private final String desc;
	private final String[] accounts;
	
	private Long shutdownTime;
	private Integer relaunchAfterShutdownMinutes;
	
	public ActiveClient(ProcessHandle process, String desc, String[] accountNames, long start, Long shutdownTime, Integer relaunchAfterShutdownMinutes) {
		this.process = process;
		this.desc = desc;
		this.start = start;
		this.accounts = accountNames;
		this.shutdownTime = shutdownTime;
		this.relaunchAfterShutdownMinutes = relaunchAfterShutdownMinutes;
	}
	
	public ActiveClient(ProcessHandle process, PendingLaunch launch) {
		this(process, launch.getName(), 
				Arrays.stream(launch.getAccounts()).map(a -> a.getUsername()).toArray(String[]::new), 
				process.info().startInstant().map(i -> i.toEpochMilli()).orElse(System.currentTimeMillis()),
				extractShutdownTime(launch.getSettings()),
				extractRescheduleTime(launch.getSettings()));
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
		String s = "";
		final LocalDateTime dt = Instant.ofEpochMilli(this.start).atZone(ZoneId.systemDefault()).toLocalDateTime();
		s += "[Launched: " + formatter.format(dt) + "]";
		s += " ";
		if (this.shutdownTime != null) {
			s += "[Shutting Down: " + formatter.format(Instant.ofEpochMilli(this.shutdownTime).atZone(ZoneId.systemDefault()).toLocalDateTime()) + "]"; 
			s += " ";
		}
		s += "[PID: " + this.process.pid() + "]"; 
		s += " ";
		s += this.desc;
		return s;
	}

	public Long getShutdownTime() {
		return this.shutdownTime;
	}
	
	public Integer getRelaunchAfterShutdownMinutes() {
		return this.relaunchAfterShutdownMinutes;
	}
	
	private static Long extractShutdownTime(StarterConfiguration config) {
		if (!config.isScheduleClientShutdown()) {
			return null;
		}
		final LocalTime time = config.getClientShutdownTime();
		final LocalDate date = config.isUseCustomClientShutdownDate()
				? config.getCustomClientShutdownDate()
				: LocalDate.now().atTime(time).isBefore(LocalDateTime.now()) // has this time today already passed
				? LocalDate.now().plusDays(1)
				: LocalDate.now();
		final LocalDateTime shutdownTime = date.atTime(time);
		return shutdownTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
	
	private static Integer extractRescheduleTime(StarterConfiguration config) {
		return config.isScheduleClientShutdown() && config.isRescheduleShutdownClients() ? config.getRescheduleShutdownClientsMinutes() : null;
	}

	public void adjustShutdownTime(StarterConfiguration config) {
		this.shutdownTime = extractShutdownTime(config);
		this.relaunchAfterShutdownMinutes = extractRescheduleTime(config);
	}
	
}
