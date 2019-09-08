package starter.models;

public class PendingLaunch {

	private final AccountConfiguration account;
	private final StarterConfiguration settings;

	public PendingLaunch(AccountConfiguration account, StarterConfiguration settings) {
		this.account = account;
		this.settings = settings;
	}

	public AccountConfiguration getAccount() {
		return this.account;
	}

	public StarterConfiguration getSettings() {
		return this.settings;
	}
	
	@Override
	public String toString() {
		return this.account.toString();
	}
	
}
