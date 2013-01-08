package outageHandling;

public class OutageUser {

	private String username;
	private String address;
	private int port;
	private boolean loggedIn;
	
	public OutageUser(String username, String address, int port, boolean loggedIn) {
		this.setUsername(username);
		this.setAddress(address);
		this.setPort(port);
		this.setLoggedIn(loggedIn);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	public String toString() {
		String toString = address + ":" + port + " - " + username;
		return toString;
	}
	
	
}
