package server;

import java.net.InetAddress;
import java.security.Key;
import java.util.ArrayList;

import communication.Channel;

/**
 * Represents a user.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class User {
	
	private String username = "";
	private boolean loggedIn = false;
	private ArrayList<String> savedMessages = new ArrayList<String>();
	private Key secretKey;
	private String lastMessage;
	private String address;
	private int port;
	private Channel channel;
	
	private String serverChallenge = null;
	
	public User(String username) {
		this.username = username;
	}
	
	public void logIn() {
		loggedIn = true;
	}
	
	public void logOut() {
		loggedIn = false;
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void addMessage(String message) {
		savedMessages.add(message);
	}
	
	public ArrayList<String> getSavedMessages() {
		return savedMessages;
	}

	public void clearMessages() {
		savedMessages.clear();
		
	}

	public void setKey(Key secretKey) {
		this.secretKey = secretKey;
		
	}
	
	public Key getKey() {
		return secretKey;
	}
	
	public void setLastMessage(String message) {
		this.lastMessage = message;
	}
	
	public String getLastMessage() {
		return lastMessage;
	}
	
	public void setAddress(InetAddress inetAddress) {
		this.address = inetAddress.toString();
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}

	public String getServerChallenge() {
		return serverChallenge;
	}

	public void setServerChallenge(String serverChallenge) {
		this.serverChallenge = serverChallenge;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

}
