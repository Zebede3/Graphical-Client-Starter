package starter.util;

import java.io.File;

public class TribotUtil {

	public static ProcessBuilder setJavaHome(ProcessBuilder pb, String tribotPath) {
		final File jre = new File(tribotPath, "jre");
		if (jre.exists()) {
			pb.environment().put("JAVA_HOME", jre.getAbsolutePath());
		}
		else {
			System.out.println("JRE file does not exist; not overwriting java home with " + jre.toString());
		}
		return pb;
	}
	
}
