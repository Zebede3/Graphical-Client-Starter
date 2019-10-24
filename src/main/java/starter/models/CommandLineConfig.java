package starter.models;

import com.beust.jcommander.Parameter;

public class CommandLineConfig {

	@Parameter(names = "-launchprofile", description = "Launches a profile")
	private String profile;
	
	@Parameter(names = "-onlylaunch", description = "Only launches the profile and doesn't display the ui")
	private boolean closeAfterLaunch;
	
	@Parameter(names = "-debug", description = "Disables the console in the UI and prints to standard output (unnecessary if using -onlylaunch)")
	private boolean debug;
	
	public String getLaunchProfile() {
		return this.profile;
	}
	
	public boolean isLaunchProfile() {
		return this.profile != null;
	}
	
	public boolean isCloseAfterLaunch() {
		return this.isLaunchProfile() && this.closeAfterLaunch;
	}
	
	public boolean isDebug() {
		return this.debug;
	}
	
}
