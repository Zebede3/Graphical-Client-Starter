package starter.models;

import java.util.Objects;

public class ProxyDescriptor {

	public static final ProxyDescriptor NO_PROXY = new NoProxy();
	
	private final String name;
	
	private final String username;
	
	private final String password;
	
	private final String ip;
	
	private final int port;
	
	public ProxyDescriptor(String name, String ip, int port, String username, String password) {
		this.name = Objects.requireNonNull(name);
		this.username = Objects.requireNonNull(username);
		this.password = Objects.requireNonNull(password);
		this.ip = Objects.requireNonNull(ip);
		this.port = port;
	}

	public String getName() {
		return this.name;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public String getIp() {
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public String toString() {
		return this.name + " (" + this.ip + ":" + this.port + ")";
	}
	
	private static class NoProxy extends ProxyDescriptor {

		private NoProxy() {
			super("", "", 0, "", "");
		}
		
		@Override
		public String toString() {
			return "No Proxy";
		}
		
	}
	
}
