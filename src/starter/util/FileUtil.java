package starter.util;

import java.io.File;

public class FileUtil {
	
	public static final File NULL_FILE = new File((System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"));

	public static File getDirectory() {
		final File f = new File(getAppDataDirectory().getAbsolutePath() + File.separator + "Graphical Client Starter");
		f.mkdirs();
		return f;
	}
	
	public static File getSettingsDirectory() {
		final File f = new File(getDirectory().getAbsolutePath() + File.separator + "settings");
		f.mkdirs();
		return f;
	}
	
	public static File getApplicationConfig() {
		return new File(getDirectory().getAbsolutePath() + File.separator + "config.json");
	}
	
	public static File getProxyFile() {
		return new File(getAppDataDirectory().getAbsolutePath() + File.separator + "settings" + File.separator + "proxies.ini");
	}

	public static File getTribotSettingsDirectory() {
		return new File(getAppDataDirectory().getAbsolutePath() + File.separator + "settings");
	}
	
	// Obtained from tribots Util.getAppDataDirectory method
	public static File getAppDataDirectory() {
		File file2 = null;
		File a = null;
		final String a2 = System.getProperty("user.home");
		final String a3 = System.getProperty("os.name").toLowerCase();
		if (a3.contains("win")) {
			final String a4 = System.getenv("APPDATA");
			file2 = (a = ((a4 == null || a4.length() < 1) ? new File(a2, ".tribot" + File.separatorChar)
					: new File(a4, ".tribot" + File.separatorChar)));
		} else if (a3.contains("solaris") || a3.contains("linux") || a3.contains("sunos") || a3.contains("unix")) {
			a = (file2 = new File(a2, ".tribot" + File.separatorChar));
		} else if (a3.contains("mac")) {
			a = new File(a2, "Library" + File.separatorChar + "Application Support" + File.separatorChar + "tribot");
		} else {
			a = new File(a2, "tribot" + File.separatorChar);
		}
		if (file2 != null) {
			if (a.exists()) {
				return a;
			}
			if (a.mkdirs()) {
				return a;
			}
		}
		a = new File("data");
		System.out.println("Couldn't create seperate application data directory. Using application data directory as: "
				+ a.getAbsolutePath());
		return a;
	}

}
