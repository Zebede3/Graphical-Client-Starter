package starter.launch;

import starter.models.ApplicationConfiguration;
import starter.models.PendingLaunch;

public interface ClientLauncher {

	Process launchAccount(ApplicationConfiguration appConfig, PendingLaunch launch);
	
}
