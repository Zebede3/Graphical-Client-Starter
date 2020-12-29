package starter.util;

public class OsUtil {

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}
	
}
