package starter.util;

import java.io.File;

public class TribotUtil {

	public static ProcessBuilder setJavaHome(ProcessBuilder pb, String tribotPath) {
		final File jre = new File(tribotPath, "jre");
		pb.environment().put("JAVA_HOME", jre.getAbsolutePath());
		return pb;
	}
	
}
