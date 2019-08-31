package starter.models;

public class ProxyDescriptor {

	private final String name;
	
	private final String username;
	
	private final String password;
	
	private final String ip;
	
	private final int port;
	
	public ProxyDescriptor(String name, String ip, int port, String username, String password) {
		this.name = name;
		this.username = username;
		this.password = password;
		this.ip = ip;
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
	
}
