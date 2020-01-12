package starter.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;

public class PendingLaunch implements Comparable<PendingLaunch> {

	private static final Comparator<LocalDateTime> COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());
	
	private final AccountConfiguration account;
	private final StarterConfiguration settings;
	
	private final LocalDateTime launchTime;

	public PendingLaunch(AccountConfiguration account, StarterConfiguration settings) {
		this.account = account;
		this.settings = settings;
		if (settings.isScheduleLaunch()) {
			final LocalTime time = settings.getLaunchTime();
			final LocalDate date = settings.isUseCustomLaunchDate()
					? settings.getLaunchDate()
					: LocalDate.now().atTime(time).isBefore(LocalDateTime.now()) // has this time today already passed
					? LocalDate.now().plusDays(1)
					: LocalDate.now();
			this.launchTime = date.atTime(time);
		}
		else
			this.launchTime = null;
	}

	public AccountConfiguration getAccount() {
		return this.account;
	}

	public StarterConfiguration getSettings() {
		return this.settings;
	}
	
	@Override
	public String toString() {
		return (this.launchTime != null ? "[" + this.launchTime.toLocalDate().toString() + "] [" + this.launchTime.toLocalTime().toString() + "] " : "") + this.account.toString();
	}

	public boolean isReadyToLaunch() {
		return this.launchTime == null || !LocalDateTime.now().isBefore(this.launchTime);
	}
	
	@Override
	public int compareTo(PendingLaunch o) {
		return COMPARATOR.compare(this.launchTime, o.launchTime);
	}
	
}
