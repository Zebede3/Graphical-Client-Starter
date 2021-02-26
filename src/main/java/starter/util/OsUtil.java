package starter.util;

import java.util.Locale;

public class OsUtil {

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
	}
	
	public static boolean isMac() {
		return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("mac") || System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("darwin");
	}
	
}
