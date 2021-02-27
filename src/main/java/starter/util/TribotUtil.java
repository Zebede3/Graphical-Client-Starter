package starter.util;

import java.io.File;

public class TribotUtil {

	public static ProcessBuilder setJavaHome(ProcessBuilder pb, String tribotPath) {
		final File jre = new File(tribotPath, "jre");
		if (jre.exists()) {
			pb.environment().put("JAVA_HOME", jre.getAbsolutePath());
		}
		else {
			boolean overwrote = false;
			if (OsUtil.isMac()) {
				final File f = new File(tribotPath, "TRiBot.app");
				if (f.exists()) {
					final String s = f.getAbsolutePath() + File.separator
							+ "Contents" + File.separator
							+ "Resources" + File.separator
							+ "jre.bundle" + File.separator
							+ "Contents" + File.separator 
							+ "Home";
					System.out.println("Using a mac; setting java home for the new process to: " + s);
					pb.environment().put("JAVA_HOME", s);
					overwrote = true;
				}
			}
			if (!overwrote) {
				System.out.println("JRE file does not exist; not overwriting java home with " + jre.toString());
			}
		}
		return pb;
	}
	
}
