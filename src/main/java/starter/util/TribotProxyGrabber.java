package starter.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import starter.models.ProxyDescriptor;

public class TribotProxyGrabber {
	
	private static final Pattern PROXY_PATTERN = Pattern.compile("(.+)\\|\\|(.+)\\:(\\d+)\\|\\|(.+)\\|\\|(.+)");
	
	public static ProxyDescriptor[] getProxies() {
		final File f = FileUtil.getProxyFile();
		if (!f.exists())
			return new ProxyDescriptor[0];
		try {
			return Files.readAllLines(f.toPath())
					.stream()
					.filter(s -> !s.isEmpty())
					.map(TribotProxyGrabber::lineToProxy)
					.filter(Objects::nonNull)
					.toArray(ProxyDescriptor[]::new);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new ProxyDescriptor[0];
		}
	}
	
	private static ProxyDescriptor lineToProxy(String line) {
		final Matcher matcher = PROXY_PATTERN.matcher(line);
		if (!matcher.matches())
			return null;
		final String name = matcher.group(1).trim();
		final String ip = matcher.group(2).trim();
		final int port = Integer.parseInt(matcher.group(3).trim());
		final String username = matcher.group(4).trim();
		final String password = matcher.group(5).trim();
		return new ProxyDescriptor(name, ip, port, username, password);
	}

}
