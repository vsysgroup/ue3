package server;

import java.util.ArrayList;

/**
 * Represents a user.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class User {
	
	private String username = "";
	private boolean loggedIn = false;
	private ArrayList<String> savedMessages = new ArrayList<String>();
	
	public User(String username) {
		this.username = username;
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
	
	public void addMessage(String message) {
		savedMessages.add(message);
	}
	
	public ArrayList<String> getSavedMessages() {
		return savedMessages;
	}

	public void clearMessages() {
		savedMessages.clear();
		
	}

}
