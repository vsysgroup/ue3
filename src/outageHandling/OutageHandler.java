package outageHandling;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Iterator;

import org.bouncycastle.openssl.PEMReader;

import server.Server;
import server.User;
import client.Client;

public class OutageHandler {

	private Client client;
	private Server server;
	private String clients;
	private ArrayList<OutageUser> clientList = new ArrayList<OutageUser>();
	
	public OutageHandler(Client client) {
		this.client = client;
	}
	
	public OutageHandler(Server server) {
		this.server = server;;	
	}
	
	public void buildClientListClientSide(String[] splitString) {
		String output = "";
				
		for(int i = 1; i<splitString.length; i++){
			if(splitString[i].equals("-|-")) {
				output += "\n";
			} else {
				output += splitString[i];
				output += " ";
			}
		}
		output.trim();
		
		String[] tmpSplitString = output.split(" ");
		for(int i = 0; i < tmpSplitString.length; i+=5) {
			String username = tmpSplitString[i];
			String address = tmpSplitString[i+1];
			int port = Integer.parseInt( tmpSplitString[i+2]);
			boolean loggedIn = Boolean.parseBoolean( tmpSplitString[i+3]);
			OutageUser newOutageUser = new OutageUser(username, address, port, loggedIn);
			clientList.add(newOutageUser);
		}
		
		clients = output;
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

	public ArrayList<OutageUser> getClientList() {
		return clientList;
	}

	public void setClientList(ArrayList<OutageUser> clientList) {
		this.clientList = clientList;
	}
	
	public long getTimeStamp() {
		return System.currentTimeMillis();
	}
	
	public String getPrintableClientList() {
		String printableList = ""; 
		Iterator<OutageUser> iter = clientList.iterator(); 
		while(iter.hasNext()) {
			OutageUser currentUser = iter.next();
			printableList += currentUser.toString();
		}
		return printableList;
	}
	
	//TODO implement
	public String signMessage(String message) {
		String signedMessage = "";
		
		
		
		Signature signature;
//		try {
//			signature = Signature.getInstance("SHA512withRSA");
//			signature.initSign(client.getSecretKey());
//			signature.update((message).getBytes());
//			byte[] signatureInBytes = instance.sign();
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
		return signedMessage;
	}
	
	//TODO implement
	public boolean verifySignature() throws IOException {
		String pathToPublicKey = "keys/" + client.getUserName() + ".pub.pem";

		PEMReader in = new PEMReader(new FileReader(pathToPublicKey));
		PublicKey publicKey = (PublicKey) in.readObject(); 
		
		return false;
	}
	
	public long getMeanValueOfTimeStamps(long[] timeStamps) {
		long mean = 0;
		for(int i = 0; i < timeStamps.length; i++) {
			mean += timeStamps[i];
		}
		if( timeStamps.length != 0) {
			mean /= timeStamps.length;
		}
		return mean;
	}

	public void startOutageMode() {
		// TODO Auto-generated method stub
		
	}
}
