package outageHandling;

import java.util.ArrayList;
import java.util.Iterator;

import server.Server;
import server.User;
import client.Client;

public class OutageHandler {

	private Client client;
	private Server server;
	private String clients;
	private ArrayList<outageUser> clientList = new ArrayList<outageUser>();
	
	public OutageHandler(Client client) {
		this.client = client;
	}
	
	public OutageHandler(Server server) {
		this.server = server;;	
	}
	
	public String buildClientListClientSide(String[] splitString) {
		String output = "";
		
		//TODO: Add each outageUser to clientList
		
		for(int i = 1; i<splitString.length; i++){
			if(splitString[i].equals("-|-")) {
				output += "\n";
			} else {
				output += splitString[i];
				output += " ";
			}
		}
		output.trim();
		
		clients = output;
		return output;
	}
	
	public String buildClientListServerSide() {
		String clientList = "";
		ArrayList<User> users = server.getUsers();
		Iterator<User> iter = users.iterator(); 
		while(iter.hasNext()) {
			User currentUser = iter.next();
			String username = currentUser.getUsername();
			String address = currentUser.getAddress();
			int port = currentUser.getPort();
			boolean loggedIn = currentUser.loggedIn();
			
			clientList += username + " " + address + " " + port + " " + loggedIn;
			clientList += " -|- ";
		}
		return clientList;
	}
	
	public String getClients() {
		return clients;
	}

	public ArrayList<outageUser> getClientList() {
		return clientList;
	}

	public void setClientList(ArrayList<outageUser> clientList) {
		this.clientList = clientList;
	}
	
	public long getTimeStamp() {
		return System.currentTimeMillis();
	}
}
