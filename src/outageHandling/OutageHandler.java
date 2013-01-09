package outageHandling;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Iterator;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Base64;

import communication.Channel;

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
			boolean loggedIn = currentUser.isLoggedIn();
			
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
	
	public String signMessage(String message) {
		String signedMessage = "";
		
		
		
		Signature signature;
		try {
			signature = Signature.getInstance("SHA512withRSA");
			try {
				signature.initSign((PrivateKey) client.getOwnPrivateKey());
				signature.update((message).getBytes());
				byte[] signatureInBytes = signature.sign();
				byte[] encodedSignature = Base64.encode(signatureInBytes); 
				String append = new String(encodedSignature);
				signedMessage = message + " " + append;
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
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

	//TODO Implement
	public void receiveMessage(String message, Channel channel) {
		String[] input = message.split(" ");
		
		//!getTimestamp <auctionID> <price>
		if(input[0].equals("!getTimestamp")) {
			int auctionID = Integer.parseInt(input[1]);
			double price = Double.parseDouble(input[2]);
			
			String returnMessage =	"!timestamp" + " " + auctionID + " " + price + " " + getTimeStamp();	
			returnMessage = signMessage(returnMessage);
			
			
		}
		
	}
}
