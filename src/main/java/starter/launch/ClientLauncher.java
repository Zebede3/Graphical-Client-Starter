package starter.launch;

import starter.gui.ActiveClientObserver;
import starter.models.ApplicationConfiguration;
import starter.models.PendingLaunch;

public interface ClientLauncher {

	Process launchAccount(ApplicationConfiguration appConfig, PendingLaunch launch);
	
	void extractActiveClient(ApplicationConfiguration config, ActiveClientObserver obs, Process process, PendingLaunch launch);
	
}
