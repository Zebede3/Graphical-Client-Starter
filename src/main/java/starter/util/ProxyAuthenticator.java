package starter.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyAuthenticator extends Authenticator {
	
	private static final Map<String, PasswordAuthentication> map = new ConcurrentHashMap<>();
	
	static {
		Authenticator.setDefault(new ProxyAuthenticator());
	}

	public static void register(String host, String user, String pass) {
		map.put(host, new PasswordAuthentication(user, pass.toCharArray()));
	}
	
	public static void remove(String host) {
		map.remove(host);
	}
	
    protected PasswordAuthentication getPasswordAuthentication() {
    	if (this.getRequestingHost() == null) {
    		return null;
    	}
        return map.getOrDefault(this.getRequestingHost(), null);
    }
    
}
