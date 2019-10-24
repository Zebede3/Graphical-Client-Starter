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
	
	// gson could produce a descriptor with null fields, which is why this method exists
	public boolean isValid() {
		return this.name != null && !this.name.isEmpty()
				&& this.username != null && !this.username.isEmpty()
				&& this.password != null && !this.password.isEmpty()
				&& this.ip != null && !this.ip.isEmpty()
				&& this.port > 0;
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
