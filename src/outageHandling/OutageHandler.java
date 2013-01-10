package outageHandling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
import server.ServerTCPListenerThread;
import server.User;
import client.Client;

public class OutageHandler {

	private Client client;
	private Server server;
	private String clients;
	private ArrayList<OutageUser> clientList = new ArrayList<OutageUser>();
	private ArrayList<OutageUser> chosenClients = new ArrayList<OutageUser>();
	private ArrayList<Long> timeStamps = new ArrayList<Long>();
	
	private Socket socket1;
	private Socket socket2;
	
	private ServerSocket serverSocket;
	
	private BufferedReader in1 = null;
	private PrintWriter out1 = null;
	
	private BufferedReader in2 = null;
	private PrintWriter out2 = null;
	
	private ClientOutageTCPListenerThread clientOutageTCPListenerThread;
	
	public OutageHandler(Client client) {
		this.client = client;
		try {
			this.serverSocket = new ServerSocket(client.getClientPort());
		} catch (IOException e) {
			System.out.println("creation of client's serverSocket failed");
			e.printStackTrace();
		}
	}
	
	public OutageHandler(Server server) {
		this.server = server;	
	}
	
	public void buildClientListClientSide(String[] splitString) {
		
		String output = "";
				
		for(int i = 1; i<splitString.length-1; i++){
			if(splitString[i].equals("EndLinE")) {
				output += "\n ";
			} else {
				output += splitString[i];
				output += " ";
			}
		}
		output.trim();
		
		clientList = new ArrayList<OutageUser>();
		
		String[] tmpSplitString = output.split(" ");
		for(int i = 0; i < tmpSplitString.length-2; i+=5) {
			String username = tmpSplitString[i];
			String address = tmpSplitString[i+1];
			int port = Integer.parseInt( tmpSplitString[i+2]);
			boolean loggedIn = Boolean.parseBoolean( tmpSplitString[i+3]);
			OutageUser knownUser = findUser(username);
			
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
			clientList += " EndLinE ";
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
			printableList += "\n";
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
		chooseClients();
		clientOutageTCPListenerThread = new ClientOutageTCPListenerThread(serverSocket, client, client.getOwnPrivateKey(), this);
		clientOutageTCPListenerThread.start();
	}

	public void receiveMessage(String message, Socket returnSocket) {
		String[] input = message.split(" ");
		
		//!getTimestamp <auctionID> <price>
		if(input[0].equals("!getTimestamp")) {
			int auctionID = Integer.parseInt(input[1]);
			double price = Double.parseDouble(input[2]);
			
			String returnMessage =	"!timestamp" + " " + auctionID + " " + price + " " + getTimeStamp();	
			returnMessage = signMessage(returnMessage);
			
			try {
				PrintWriter tmpOut = new PrintWriter(returnSocket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(input[0].equals("!timestamp")) {
			//TODO react to !timestamp response, extract signatues and save them
		}
		
	}
	
	public void chooseClients() {
		Iterator<OutageUser> iter = clientList.iterator();
		int chosen = 0;
		while(iter.hasNext() && chosen < 2) {
			OutageUser currentUser = iter.next();
			if(currentUser.isLoggedIn()) {
				chosenClients.add(currentUser);
				chosen++;
			}
		}
	}
	
	public void sendTimestampRequest(String message) {
		try {
			socket1 = new Socket(chosenClients.get(0).getAddress(), chosenClients.get(0).getPort());
			in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
			out1 = new PrintWriter(socket1.getOutputStream(), true);
			
		} catch (UnknownHostException e) {
			System.out.println("Connection to Client 1 could not be established - Unknown Host");
		} catch (IOException e) {
			System.out.println("Connection to Client 1 could not be established - IOException");
		}
		try {
			socket2 = new Socket(chosenClients.get(1).getAddress(), chosenClients.get(1).getPort());
			in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
			out1 = new PrintWriter(socket1.getOutputStream(), true);
		} catch (UnknownHostException e) {
			System.out.println("Connection to Client 2 could not be established - Unknown Host");
		} catch (IOException e) {
			System.out.println("Connection to Client 2 could not be established - IOException");
		}
		
		out1.println(message);
		out2.println(message);
		
	}
	
	public OutageUser findUser(String name) {
		Iterator<OutageUser> iter = clientList.iterator();
		OutageUser currentUser = null;
		while(iter.hasNext()) {
			currentUser = iter.next();
			if(currentUser.getUsername().equals(name)){
				return currentUser;
			}
		}
		return currentUser;
	}
}
