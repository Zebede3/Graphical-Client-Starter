package starter.models;

public class ScriptCommand  {

	private final String script, args, breakProfile, accountManager;
	
	public ScriptCommand(String script, String args, String breakProfile, String accountManager) {
		this.script = script;
		this.args = args == null ? "" : args;
		this.breakProfile = breakProfile != null && !breakProfile.isEmpty() ? breakProfile : null;
		this.accountManager = accountManager != null && !accountManager.isEmpty() ? accountManager : null;
	}

	public String getScript() {
		return script;
	}

	public String getArgs() {
		return args;
	}

	public String getBreakProfile() {
		return breakProfile;
	}

	public String getAccountManager() {
		return accountManager;
	}
	
}
