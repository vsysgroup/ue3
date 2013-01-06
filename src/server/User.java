package server;

import java.net.InetAddress;
import java.security.Key;
import java.util.ArrayList;

/**
 * Represents a user.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class User {
	
	private String username = "";
	private boolean loggedIn = false;
	private InetAddress inetAddress = null;
	private int port;
	private ArrayList<String> savedMessages = new ArrayList<String>();
	private Key secretKey;
	private String lastMessage;
	
	public User(String username, InetAddress inetAddress, int port) {
		this.username = username;
		this.inetAddress = inetAddress;
		this.port = port;
	}
	
	public void logIn() {
		loggedIn = true;
	}
	
	public void logOut() {
		loggedIn = false;
	}
	
	public boolean loggedIn() {
		return loggedIn;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}
	
	public InetAddress getInetAddress() {
		return inetAddress;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
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

}
