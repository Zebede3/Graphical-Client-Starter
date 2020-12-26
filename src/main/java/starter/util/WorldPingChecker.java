package starter.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import starter.models.ProxyDescriptor;

public class WorldPingChecker {
	
	private static final Map<ProxyDescriptor, Map<Integer, Integer>> cache = new ConcurrentHashMap<>();
	
	private static final int TIMEOUT = 2000;
	private static final int PORT = 43594;
	
	private static final String PATH = "oldschool%d.runescape.com";
	
	static {
		Scheduler.executor().scheduleWithFixedDelay(() -> {
			cache.clear();
		}, 30L, 30L, TimeUnit.MINUTES);
	}
	
	public static int getPing(ProxyDescriptor proxy, int world) {
		world = world % 300;
		return cache.computeIfAbsent(proxy, key -> new ConcurrentHashMap<>())
				.computeIfAbsent(world, key -> {
					final String url = String.format(PATH, key);
					ProxyAuthenticator.register(proxy.getIp(), proxy.getUsername(), proxy.getPassword());
					try (final Socket socket = (proxy != ProxyDescriptor.NO_PROXY ? new Socket(proxy.toProxy()) : new Socket())) {
						socket.setSoTimeout(TIMEOUT);
						final InetAddress inetAddress = InetAddress.getByName(url);
						final long start = System.nanoTime();
						socket.connect(new InetSocketAddress(inetAddress, PORT), TIMEOUT);
						final long end = System.nanoTime();
						final int ping = (int) ((end - start) / 1000000L);
						return ping;
					} 
					catch (IOException e) {
						//e.printStackTrace();
						return -1;
					}
					finally {
						ProxyAuthenticator.remove(proxy.getIp());
					}
				});
	}
	
}
