package outageHandling;

public class outageUser {

	private String username;
	private String address;
	private int port;
	
	public outageUser(String username, String address, int port) {
		this.setUsername(username);
		this.setAddress(address);
		this.setPort(port);
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
	
	
}
