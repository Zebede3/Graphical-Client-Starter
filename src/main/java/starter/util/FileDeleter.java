package starter.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class FileDeleter {
	
	private static final boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
	
	public static void run() {
		
		System.out.println("Operating system: " + System.getProperty("os.name"));
		
		final boolean deleteHooks = deleteHooks();
		System.out.println("Deleted hooks: " + deleteHooks);
		final boolean deleteJagexCache = deleteJagexCache();
		System.out.println("Deleted jagex cache: " + deleteJagexCache);
		final boolean deleteRandom = deleteRandom();
		System.out.println("Deleted random.dat: " + deleteRandom);
		final boolean deleteLookingGlassJars = deleteLookingGlassJars();
		System.out.println("Deleted looking glass agents: " + deleteLookingGlassJars);
		final boolean deleteCachedGamepacks = deleteCachedGamepacks();
		System.out.println("Deleted cached gamepacks: " + deleteCachedGamepacks);
		final boolean deleteJagexCachePath = deleteJagexCachePath();
		System.out.println("Deleted stored jagex cache path: " + deleteJagexCachePath);
		
		System.out.println("File deletion finished");
	}
	
	private static boolean deleteJagexCachePath() {
		final String user = System.getProperty("user.dir");
		final File f = new File(user, "jagex_cl_oldschool_LIVE.dat");
		System.out.println("Jagex cache path path: " + f.getAbsolutePath());
		if (!f.exists()) {
			return true;
		}
		try {
			Files.delete(f.toPath());
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean deleteHooks() {
		final File f = new File(FileUtil.getTribotSettingsDirectory().getAbsolutePath() + File.separator + "hooks.dat");
		System.out.println("Hooks path: " + f.getAbsolutePath());
		if (!f.exists()) {
			return true;
		}
		try {
			Files.delete(f.toPath());
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean deleteJagexCache() {
		final String s = windows ? System.getenv("USERPROFILE") : System.getProperty("user.home");
		final File f = new File(s, "jagexcache");
		try {
			System.out.println("Jagex cache path: " + f.getAbsolutePath());
			recursiveDelete(f);
			return true;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean deleteRandom() {
		
		final String s = windows ? System.getenv("USERPROFILE") : System.getProperty("user.home");
		
		final File f = new File(s, "random.dat");
		System.out.println("Random.dat path: " + f.getAbsolutePath());
		if (!f.exists())
			return true;
		
		try {
			Files.delete(f.toPath());
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean deleteLookingGlassJars() {
	
		final String s = System.getProperty("java.io.tmpdir");
		
		final File file = new File(s);
		
		return Arrays.stream(file.listFiles())
			.filter(f -> f.getName().toLowerCase().contains("tribot") || f.getName().toLowerCase().contains("t1_agent"))
			.filter(f -> f.getName().contains(".jar"))
			.peek(f -> System.out.println("Looking glass agent detected: " + f.getAbsolutePath()))
			.allMatch(f -> {
				try {
					Files.delete(f.toPath());
					return true;
				}
				catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			});
	}
	
	private static boolean deleteCachedGamepacks() {
		
		final String s = System.getProperty("java.io.tmpdir");
		
		final File file = new File(s);
		
		return Arrays.stream(file.listFiles())
			.filter(f -> f.getName().startsWith("os_client"))
			.filter(f -> f.getName().contains(".jar"))
			.peek(f -> System.out.println("Cached gamepack detected: " + f.getAbsolutePath()))
			.allMatch(f -> {
				try {
					Files.delete(f.toPath());
					return true;
				}
				catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			});
	}
	
	private static void recursiveDelete(File file) throws IOException {
		Files.walk(file.toPath())
	      .sorted(Comparator.reverseOrder())
	      .map(Path::toFile)
	      .forEach(File::delete);
	}
	
}

