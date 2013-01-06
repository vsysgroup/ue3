package server;

import integrity.IntegrityManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import registry.RegistryReader;
import analyticsServer.AnalyticsServerInterface;
import analyticsServer.AuctionEvent;
import analyticsServer.BidEvent;
import analyticsServer.UserEvent;
import billingServer.IBillingServer;
import billingServer.IBillingServerSecure;

import communication.TCPCommunication;

import exception.WrongParameterCountException;

/**
 * Represents a Server
 * @author Philipp Pfeiffer 0809357
 * @author Barbara Schwankl 0852176
 *
 */
public class Server {
	
	
	
	public static final Logger LOG = Logger.getLogger(Server.class);
	private static String bindingNameAnalytics = "AnalyticsServer";
	private static String bindingNameBilling = "BillingServer";
	
	public static String currentAuctionList = "";

	private int tcpPort;
	private boolean serverStatus = false;
	private ServerTCPListenerThread serverTCPListenerThread = null;
	private AuctionCheckThread auctionCheckThread = null;
	private ServerSocket serverSocket = null;
	private ArrayList<User> users = new ArrayList<User>();
	private static List<Auction> auctions = Collections.synchronizedList(new ArrayList());
	private Scanner in = new Scanner(System.in);

	private static AnalyticsServerInterface analyticsHandler = null;
	private static IBillingServer loginHandler = null;
	private static IBillingServerSecure billingHandler = null;
	
	private String pathToKeyDirectory;
	private String pathToServerKey;
	
	private IntegrityManager integrityManager;

	public static void main(String[] args) {
		
		//init Logger
		BasicConfigurator.configure();

		try {
			new Server(args);
		} catch(NumberFormatException e) {
			howToUse();
		} catch(WrongParameterCountException e) {
			howToUse();
		} 
	}

	/**
	 * 
	 * @param args
	 * @throws WrongParameterCountException
	 */
	public Server(String[] args) throws WrongParameterCountException{
		if(args.length != 5) {
			throw new WrongParameterCountException();
		} else {
			this.tcpPort = Integer.parseInt(args[0]);
			this.bindingNameAnalytics = args[1];
			this.bindingNameBilling = args[2];
			this.pathToServerKey = args[3];
			this.pathToKeyDirectory = args[4];
			
			//load integrityManager
			integrityManager = new IntegrityManager(pathToKeyDirectory);
		}

		System.out.println("Starting Server.");

		try {
			serverSocket = new ServerSocket(tcpPort);
		} catch (IOException e) {
			System.out.println("Could not create socket!");
			exit();
		}



		serverStatus = true;

		serverTCPListenerThread = new ServerTCPListenerThread(serverSocket, this);
		serverTCPListenerThread.start();
		auctionCheckThread = new AuctionCheckThread(this);
		auctionCheckThread.start();
		
		lookupRMI();

		while(serverStatus) {
			while(in.hasNextLine() && serverStatus) {
				exit();
				return;
			}
		}
	}

	/**
	 * Receives a message and a socket and processes the message
	 * @param message
	 * @param socket
	 */
	public void receiveMessage(String message, Socket socket) {
		InetAddress inetAddress = socket.getInetAddress();

		String[] input = message.split(" ");
		TCPCommunication responseTCPCommunication = null;
		try {
			 responseTCPCommunication = new TCPCommunication(socket);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/**
		 * logs the user in
		 * sends the following responses:
		 * login successful <username>
		 * login failed
		 */
		if(input[0].equals("!login")) {
			String username = input[1];
//			int port = Integer.parseInt(input[2]);
			int port = 0;

			if(!userKnown(username)){
				User newUser = new User(username, inetAddress, port);
				users.add(newUser);
				
				//add user's key to newUser
				try {
					newUser.setKey(integrityManager.getSecretKey(username));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				newUser.logIn();

				if(analyticsHandler != null) {
					try {
						analyticsHandler.processEvent(new UserEvent("USER_LOGIN", newUser.getUsername()));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
//				new UDPNotificationThread(inetAddress, port, "login successful" + " " + username).start();
				responseTCPCommunication.send("login successful" + " " + username);
			}
			else{
				User user = findUser(username);
				if(user.loggedIn()) {
//					new UDPNotificationThread(inetAddress, port, "login failed").start();
					responseTCPCommunication.send("login failed");
				}
				else{
					user.logIn();
					if(user.getPort() != port) {
						user.setPort(port);
					}
					if(!user.getInetAddress().equals(inetAddress)) {
						user.setInetAddress(inetAddress);
					}
					
					if(analyticsHandler != null) {
						try {
							analyticsHandler.processEvent(new UserEvent("USER_LOGIN", user.getUsername()));
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					
//					new UDPNotificationThread(inetAddress, port, "login successful" + " " + username).start();
					responseTCPCommunication.send("login successful" + " " + username);
					if(user.getSavedMessages().size() > 0) {
						for(int i = 0; i < user.getSavedMessages().size(); i++) {
//							new UDPNotificationThread(inetAddress, port, user.getSavedMessages().get(i)).start();
							responseTCPCommunication.send(user.getSavedMessages().get(i));
						}
						user.clearMessages();
					}
				}
			}
		}

		/**
		 * logs the user out
		 * sends the following responses:
		 * logout successful <username>
		 */
		if(input[0].equals("!logout")) {
			String username = input[1];
			User user = findUser(username);
			int port = user.getPort();
			if(user.loggedIn()) {
				user.logOut();
				if(analyticsHandler != null) {
					try {
						analyticsHandler.processEvent(new UserEvent("USER_LOGOUT", user.getUsername()));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				
//				new UDPNotificationThread(inetAddress, port, "logout successful" + " " + username).start();
				responseTCPCommunication.send("logout successful" + " " + username);
			}
		}

		/**
		 * creates a new Auction
		 * sends the following responses:
		 * create successful <ID> <date> <time> <timezone> <description> 
		 */
		if(input[0].equals("!create")) {
			String username = input[1];
			User user = findUser(username);
			int port = user.getPort();
			Long seconds = Long.parseLong(input[2]);
			String description = "";
			for(int i = 3; i < input.length; i++) {
				description += input[i];
				description += " ";
			}
			Auction newAuction = createAuction(user, seconds, description);
			String endDate = newAuction.dateToString();
			int ID = newAuction.getID();
			
			if(analyticsHandler != null) {
				try {
					analyticsHandler.processEvent(new AuctionEvent("AUCTION_STARTED", (long) ID));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
			
//			new UDPNotificationThread(inetAddress, port, "create successful" + " " + ID + " " + endDate + " " + description).start();
			responseTCPCommunication.send("create successful" + " " + ID + " " + endDate + " " + description);
		}

		/**
		 * creates a list and sends it to the user
		 * sends the following responses:
		 * list <list>
		 */
		if(input[0].equals("!list") && input.length == 1) {

			String list = buildList();
//			new UDPNotificationThread(returnAddress, port, "list" + " " + list).start();
			responseTCPCommunication.send("list " + list);
			
		}
		/**
		 * creates a list and sends it to the user. The user has to be logged in to receive this version
		 * of the message and will receive a hashed MAC at the end.
		 * sends the following responses:
		 * list <list> <hMAC>
		 */
		if(input[0].equals("!list") && input.length == 2) {
			
			//create a hashed MAC for the specific user
			String username = input[1];
			User user = findUser(username);
			Key key = user.getKey();
		
			
			String list = buildList();
			String returnMessage = "list " + list;
			try {
				byte[] hMAC = integrityManager.createHashMAC(key, returnMessage);				
				byte[] encodedHMAC = Base64.encode(hMAC);  
				String append = new String(encodedHMAC);
				
				returnMessage += " " + append;
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}	
			responseTCPCommunication.send(returnMessage);
		}

		/**
		 * Places a bid on an auction
		 */
		if(input[0].equals("!bid")) {
			String username = input[1];
			User user = findUser(username);
			findAuctionByID(input[2]).newBid(user, Double.parseDouble(input[3]), responseTCPCommunication);
		}
	}

	/**
	 * Finds an auction from the auction list by it's ID
	 * @param IDString
	 * @return Auction
	 */
	private Auction findAuctionByID(String IDString) {
		int ID = Integer.parseInt(IDString);
		for(int i = 0; i < auctions.size(); i++) {
			if(auctions.get(i).getID() == ID) {
				return auctions.get(i);
			}
		}
		return null;
	}

	public static void howToUse() {
		System.out.println("Parameters incorrect. Correct syntax: java Server <tcpPort>");
	}
	/**
	 * returns the server Status
	 * @return boolean
	 */
	public boolean getServerStatus() {
		return serverStatus;
	}

	/**
	 * shuts down the server and frees all resources
	 */
	public void exit() {
		in.close();
		serverStatus = false;
		auctionCheckThread.exit();
		serverTCPListenerThread.exit();
		for(int i = 0; i < users.size(); i++) {
			users.get(i).logOut();
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			//closing socket for shutdown
		}
	}

	/**
	 * Finds out whether a user is known or not
	 * @param name
	 * @return boolean
	 */
	public boolean userKnown(String name){
		Iterator<User> iter = users.iterator();
		User currentUser = null;
		while(iter.hasNext()) {
			currentUser = iter.next();
			if(currentUser.getUsername().equals(name)){
				return true;
			}
		}
		return false;
	}

	/**
	 * finds and returns a user by name
	 * @param name
	 * @return User
	 */
	public User findUser(String name) {
		Iterator<User> iter = users.iterator();
		User currentUser = null;
		while(iter.hasNext()) {
			currentUser = iter.next();
			if(currentUser.getUsername().equals(name)){
				return currentUser;
			}
		}
		return currentUser;
	}

	/**
	 * if a bid was unsuccessful, the bidder will be informed
	 * @param bidder
	 * @param amountBid
	 * @param amountHighestBid
	 * @param description
	 */
	public void bidUnsuccessful(User bidder, double amountBid, double amountHighestBid, String description, TCPCommunication responseTCPCommunication) {
		InetAddress inetAddress = bidder.getInetAddress();
		int port = bidder.getPort();
//		new UDPNotificationThread(inetAddress, port, "bid" + " " + "unsuccessful" + " " + amountBid + " " + amountHighestBid + " " + description).start();
		responseTCPCommunication.send("bid" + " " + "unsuccessful" + " " + amountBid + " " + amountHighestBid + " " + description);
	}

	/**
	 * Creates a new auction and returns it
	 * @param user
	 * @param seconds
	 * @param description
	 * @return Auction
	 */
	synchronized public Auction createAuction(User user, Long seconds, String description) {
		Date date = new Date();
		date.setTime(date.getTime()+(seconds*1000));

		Auction auction = new Auction(date, user, description, this);
		auctions.add(auction);
		return auction;
	}

	/**
	 * returns all currently running auctions
	 * @return
	 */
	public List<Auction> getAuctions() {
		return auctions;
	}

	/**
	 * When an auction ends, this method sends out notifications to all Users who have taken part in the auction
	 * @param currentAuction
	 */
	public void auctionEnded(Auction currentAuction) {
		ArrayList<User> bidders = currentAuction.getBidders();
		User winner = currentAuction.getWinner();
		double winningBid = currentAuction.getWinningBid();
		String description = currentAuction.getDescription();
		
		try {
			if(analyticsHandler != null) {
				analyticsHandler.processEvent(new AuctionEvent("AUCTION_ENDED", (long) currentAuction.getID()));
			}
			//register new bill for auction owner on billingServer
			if(loginHandler != null) {
				billingHandler = loginHandler.login("auctionClientUser", "dslab2012");
				if (billingHandler != null) {
					billingHandler.billAuction(currentAuction.getOwner().getUsername(), currentAuction.getID(), winningBid);
				} else {
					LOG.error("Login to AuctionServer with user " + currentAuction.getOwner().getUsername() + " failed.");
				}
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if(winner != null) {
			try {
				if(analyticsHandler != null) {
					analyticsHandler.processEvent(new BidEvent("BID_WON", winner.getUsername(), (long) currentAuction.getID(), winningBid));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < bidders.size(); i++) {
			if(!bidders.get(i).loggedIn()) {
				if(bidders.get(i).getUsername().equals(winner.getUsername())) {
					bidders.get(i).addMessage("!auction-ended" + " " + "You"  + " " + winningBid  + " " + description);
				}
				else {
					bidders.get(i).addMessage("!auction-ended"  + " " + winner.getUsername()  + " " + winningBid + " " + description);
				}
			}
			else {
				if(bidders.get(i).getUsername().equals(winner.getUsername())) {
//					new UDPNotificationThread(winner.getInetAddress(), winner.getPort(), "!auction-ended" + " " + "You"  + " " + winningBid  + " " + description).start();
				}
				else {
//					new UDPNotificationThread(bidders.get(i).getInetAddress(), bidders.get(i).getPort(), "!auction-ended"  + " " + winner.getUsername()  + " " + winningBid + " " + description).start();
				}
			}
		}
	}

	/**
	 * builds a list of all auctions
	 * @return String
	 */
	public String buildList() {
		String list = "";
		if(auctions.size() == 0) {
			return "no auctions are running";
		}
		List<Auction> auctions = getAuctions();
		for(int i = 0; i < auctions.size(); i++) {
			int ID = auctions.get(i).getID();
			String description = auctions.get(i).getDescription();
			description.trim();
			String owner = auctions.get(i).getOwner().getUsername();
			double highestBid = auctions.get(i).getWinningBid();
			User highestBidder = auctions.get(i).getWinner();
			String highestBidderName = "";
			String endDate = auctions.get(i).dateToString();

			if(highestBidder == null) {
				highestBidderName = "none";
			} else {
				highestBidderName = highestBidder.getUsername();
			}
			list += ID + ". " + "'" + description + "'" + " " + owner + " " + endDate + " " + highestBid + " " + highestBidderName;
			list += " -|- ";
		}
		return list;
	}

	/**
	 * if a bid was successful, this method will notify the bidder
	 * @param bidder
	 * @param amount
	 * @param description
	 */
	public void bidSuccessful(User bidder, double amount, String description, int auctionID, TCPCommunication tcpCommunication) {
		InetAddress inetAddress = bidder.getInetAddress();
		int port = bidder.getPort();
		if(analyticsHandler != null) {
			try {
				analyticsHandler.processEvent(new BidEvent("BID_PLACED",bidder.getUsername(), (long) auctionID, amount));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
//		new UDPNotificationThread(inetAddress, port, "bid" + " " + "successful" + " " + amount + " "  + description).start();
		tcpCommunication.send("bid" + " " + "successful" + " " + amount + " "  + description);
	}

	/**
	 * This notification is sent out whenever a user has been overbid on an auction.
	 * @param bidder
	 * @param description
	 */
	public void userOverbid(User bidder, double amount, String description, int auctionID, TCPCommunication tcpCommunication) {
		InetAddress inetAddress = bidder.getInetAddress();
		int port = bidder.getPort();
		if(analyticsHandler != null) {
			try {
				analyticsHandler.processEvent(new BidEvent("BID_OVERBID",bidder.getUsername(), (long) auctionID, amount));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		if(!bidder.loggedIn()) {
			bidder.addMessage("!new-bid" + description);
		}
		else {
//			new UDPNotificationThread(inetAddress, port, "!new-bid" + " " + description).start();
			tcpCommunication.send("bid" + " " + "successful" + " " + amount + " "  + description);

		}
	}
	
	private static void lookupRMI() {
		RegistryReader registryLocation = new RegistryReader();
		try {
			Registry registry = LocateRegistry.getRegistry(registryLocation.getHost(), registryLocation.getPort());
			try {
				analyticsHandler = (AnalyticsServerInterface) registry.lookup(bindingNameAnalytics);
				LOG.info("AnalyticsServer looked up");
			} catch (NotBoundException e) {
				LOG.info("this remote object doesnt exist / hasnt been bound - AnalyticsServer");
			}
			try {
				loginHandler = (IBillingServer) registry.lookup(bindingNameBilling);
				LOG.info("BillingServer looked up");
			} catch (NotBoundException e) {
				LOG.info("this remote object doesnt exist / hasnt been bound - BillingServer");
			}
		} catch (RemoteException e) {
			LOG.info("problem occurred trying to get registry");
		}
	}
	
	public static String getList() {
		String list = "";

		if(auctions.size() == 0) {
			return "no auctions are running";
		}
		List<Auction> tmpAuctions = auctions;
		for(int i = 0; i < tmpAuctions.size(); i++) {
			int ID = tmpAuctions.get(i).getID();
			String description = tmpAuctions.get(i).getDescription();
			description.trim();
			String owner = tmpAuctions.get(i).getOwner().getUsername();
			double highestBid = tmpAuctions.get(i).getWinningBid();
			User highestBidder = tmpAuctions.get(i).getWinner();
			String highestBidderName = "";
			String endDate = tmpAuctions.get(i).dateToString();

			if(highestBidder == null) {
				highestBidderName = "none";
			} else {
				highestBidderName = highestBidder.getUsername();
			}
			list += ID + ". " + "'" + description + "'" + " " + owner + " " + endDate + " " + highestBid + " " + highestBidderName;
			list += "\n";
		}
		return list;
	}
	
	public static Auction getRandomAuction() {
		if(auctions.size() == 0) {
			return null;
		}
		int amount = auctions.size() - 1;
		Random randomGenerator = new Random();
		int randomNumber = randomGenerator.nextInt(amount);
		Auction randomAuction = auctions.get(randomNumber);
		return randomAuction;
	}
}
