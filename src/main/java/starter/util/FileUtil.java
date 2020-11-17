package starter.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.JsonSyntaxException;

import starter.gson.GsonFactory;
import starter.models.ApplicationConfiguration;
import starter.models.StarterConfiguration;

public class FileUtil {
	
	public static final File NULL_FILE = new File((System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"));

	public static ApplicationConfiguration readApplicationConfig() {
		final File config = getApplicationConfig();
		if (config.exists()) {
			try {
				final byte[] contents = Files.readAllBytes(config.toPath());
				final ApplicationConfiguration c = GsonFactory.buildGson().fromJson(new String(contents), ApplicationConfiguration.class);
				if (c != null) {
					return c;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ApplicationConfiguration();
	}
	
	public static void saveApplicationConfig(ApplicationConfiguration config) {
		final byte[] bytes = GsonFactory.buildGson().toJson(config).getBytes();
		try {
			Files.write(FileUtil.getApplicationConfig().toPath(), bytes);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static StarterConfiguration readSettingsFile(String name) {
		if (name == null || name.isEmpty())
			return null;
		if (!new File(name).isAbsolute())
			name = FileUtil.getSettingsDirectory().getAbsolutePath() + File.separator + name;
		if (!name.endsWith(".json"))
			name += ".json";
		final File file = new File(name);
		if (!file.exists()) {
			System.out.println("Failed to open '" + name + "', does not exist");
			return null;
		}
		try {
			final byte[] contents = Files.readAllBytes(file.toPath());
			final StarterConfiguration config = GsonFactory.buildGson().fromJson(new String(contents), StarterConfiguration.class);
			return config;
		}
		catch (IOException | JsonSyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean saveSettings(String name, StarterConfiguration model) {
		if (name.isEmpty())
			return false;
		if (!new File(name).isAbsolute())
			name = FileUtil.getSettingsDirectory().getAbsolutePath() + File.separator + name;
		if (!name.endsWith(".json"))
			name += ".json";
		final String save = GsonFactory.buildGson().toJson(model);
		try {
			Files.write(new File(name).toPath(), save.getBytes());
			return true;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
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
	
	public static File getActiveClientCacheFile() {
		return new File(getDirectory().getAbsolutePath() + File.separator + "active_clients.json");
	}
	
	public static File getApplicationConfig() {
		return new File(getDirectory().getAbsolutePath() + File.separator + "config.json");
	}
	
	public static File getProxyFile() {
		return new File(getAppDataDirectory().getAbsolutePath() + File.separator + "settings" + File.separator + "proxies.ini");
	}
	
	public static File getProxyJsonFile() {
		return new File(getAppDataDirectory().getAbsolutePath() + File.separator + "settings" + File.separator + "proxies.json");
	}

	public static File getTribotDependenciesDirectory() {
		return new File(getAppDataDirectory().getAbsolutePath() + File.separator + "dependancies");
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
