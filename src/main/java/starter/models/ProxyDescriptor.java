package starter.models;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class ProxyDescriptor {

	public static final ProxyDescriptor NO_PROXY = new NoProxy();
	
	@SerializedName("name")
	private final String name;
	
	@SerializedName("username")
	private final String username;
	
	@SerializedName("password")
	private final String password;
	
	@SerializedName(value = "ip", alternate = { "host" })
	private final String ip;
	
	@SerializedName("port")
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + port;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProxyDescriptor other = (ProxyDescriptor) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (port != other.port)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	
}
