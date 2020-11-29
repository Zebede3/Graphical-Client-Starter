package starter.util;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

import com.google.gson.Gson;

public class TribotBreakGrabber {
	
	private static volatile String[] cached;
	
	public static String[] getBreakNames() {
		if (cached != null) {
			return cached;
		}
		final File f = FileUtil.getBreaksJsonFile();
		if (!f.exists())
			return new String[0];
		try {
			final byte[] contents = Files.readAllBytes(f.toPath());
			final String s = new String(contents);
			final BreakHolder breaks = new Gson().fromJson(s, BreakHolder.class);
			if (breaks.breakProfiles == null) {
				return new String[0];
			}
			cached = Arrays.stream(breaks.breakProfiles).filter(Objects::nonNull).map(b -> b.name).filter(Objects::nonNull).filter(str -> !str.isEmpty()).toArray(String[]::new);
			return cached;
		}
		catch (Exception e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	private static class BreakHolder {
		
		private BreakProfile[] breakProfiles;
		
	}
	
	private static class BreakProfile {
		
		private String name;
		
	}

}
