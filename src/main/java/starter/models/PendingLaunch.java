package starter.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PendingLaunch implements Comparable<PendingLaunch> {

	private static final Comparator<LocalDateTime> COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());
	
	private final AccountConfiguration[] accounts;
	private final StarterConfiguration settings;
	
	private final LocalDateTime launchTime;
	
	private final String name;
	
	public PendingLaunch(StarterConfiguration settings, AccountConfiguration... accounts) {
		this(settings, accounts.length > 0 ? accounts[0].toString() : "Empty Client", accounts);
	}

	public PendingLaunch(StarterConfiguration settings, String name, AccountConfiguration... accounts) {
		this.accounts = accounts;
		this.settings = settings;
		this.name = name;
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
	
	private PendingLaunch(AccountConfiguration[] accounts, StarterConfiguration settings, LocalDateTime launchTime, String name) {
		this.accounts = accounts;
		this.settings = settings;
		this.launchTime = launchTime;
		this.name = name;
	}
	
	public PendingLaunch withFilteredAccounts(List<AccountConfiguration> remove) {
		return new PendingLaunch(
				Arrays.stream(this.accounts).filter(a -> !remove.contains(a)).toArray(AccountConfiguration[]::new),
				this.settings,
				this.launchTime,
				this.name
		);
	}

	public StarterConfiguration getSettings() {
		return this.settings;
	}
	
	public AccountConfiguration[] getAccounts() {
		return this.accounts;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return (this.launchTime != null ? "[" + this.launchTime.toLocalDate().toString() + "] [" + this.launchTime.toLocalTime().toString() + "] " : "") + this.name;
	}

	public boolean isReadyToLaunch() {
		return this.launchTime == null || !LocalDateTime.now().isBefore(this.launchTime);
	}
	
	@Override
	public int compareTo(PendingLaunch o) {
		return COMPARATOR.compare(this.launchTime, o.launchTime);
	}
	
}
