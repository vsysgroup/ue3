package client;

import integrity.IntegrityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import outageHandling.OutageHandler;
import security.KeyReader;
import security.MyRandomGenerator;

import communication.Base64Channel;
import communication.Channel;
import communication.RSAChannel;
import communication.TCPChannel;

import exception.WrongParameterCountException;

/**
 * Represents a Client
 * @author Philipp Pfeiffer 0809357
 * @author Barbara Schwankl 0852176
 * 
 */
public class Client {

	public static final Logger LOG = Logger.getLogger(Client.class);

	private String serverHost;
	private int serverTCPPort;
	private Socket clientSocket = null;
	private Boolean clientStatus = false;
	private Boolean loggedIn = false;
	private Scanner in = new Scanner(System.in);
	private Channel channel = null;
	private String username = "";
	private DatagramSocket datagramSocket = null;
	private ClientTCPListenerThread clientTCPListenerThread = null;
	private int clientPort;

	private String pathToPublicServerKey;
	private String pathToKeyDirectory;

	private IntegrityManager integrityManager;
	private Key clientSecretKey;
	private Key clientPrivateKey = null;

	private Boolean secondAttemptRequested = false;
	private KeyReader keyReader;
	private String clientChallenge;

	private OutageHandler outageHandler;
	private boolean outageMode = false;

	public static void main(String[] args) {

		try {
			new Client(args);
		} catch(NumberFormatException e ) {
			howToUse();
		}catch (WrongParameterCountException e) {
			howToUse();
		} 
	}

	/**
	 * Creates a new instance of Client
	 * @param args
	 * @throws WrongParameterCountException
	 */
	public Client(String[] args) throws WrongParameterCountException {
		BasicConfigurator.configure();
		
		boolean test = false;
		//check if parameters are alright
		if(args.length != 5) {
			throw new WrongParameterCountException();
		} else {
			this.serverHost = args[0];
			this.serverTCPPort = Integer.parseInt(args[1]);
			this.clientPort = Integer.parseInt(args[2]);
			this.pathToPublicServerKey = args[3];
			this.pathToKeyDirectory = args[4];

			//load OutageHandler
			outageHandler = new OutageHandler(this);

		}

		//key reader for reading out private and public keys
		keyReader = new KeyReader(pathToKeyDirectory);

		clientStatus = true;

		System.out.println("Starting Client.");

		//channel is set
		establishTCPConnection();
		clientTCPListenerThread = new ClientTCPListenerThread(channel, this);
		clientTCPListenerThread.start();

		//loop checking for input
		while(clientStatus && !test) {
			try {
				while(!loggedIn && in.hasNext() && clientStatus) {
					if(loggedIn) {
						break;
					}
					String[] input = in.nextLine().split(" ");

					//possible commands before login
					if(input[0].equals("!end")) {
						exitClient();
						return;
					} else if(input[0].equals("!login") && input.length == 2) {
						login(input[1]);
					} else if(input[0].equals("!list")) {
						listWhileNotLoggedIn();
						continue;
					} else {
						System.out.println("Please log in first. The only commands available while not logged in are:");
						System.out.println("!login <username>");
						System.out.println("!end");
					}


				}
			} catch(Exception e) {
				System.out.println("Connection to the server failed!");
				e.printStackTrace();
				exitClient();
			}



			while(clientStatus && getLoggedIn() && in.hasNext()){
				if(!loggedIn) {
					break;
				}
				String[] input = in.nextLine().split(" ");

				//possible commands after login
				if(input[0].equals("!end")) {
					exitClient();
					break;
				}
				else if(input[0].equals("!login")) {
					System.out.println("You are already logged in. Please log out first, if you want to change the user.");
					continue;
				}
				else if(input[0].equals("!logout")) {
					logout();
					break;
				}
				else if(input[0].equals("!bid") && input.length == 3) {
					try {
						placeBid(Integer.parseInt(input[1]), Double.valueOf(input[2]));
					} catch(NumberFormatException e) {
						System.out.println("One of the parameters is wrong.");
					}
					continue;
				}
				else if(input[0].equals("!create") && input.length >= 3) {
					try {
						String description = "";
						for(int i = 2; i < input.length; i++) {
							description += input[i];
							description += " ";
						}
						description = description.trim();
						createAuction(Integer.parseInt(input[1]),description);
					} catch(NumberFormatException e) {
						System.out.println("One of the parameters is wrong.");
					}
					continue;
				}
				else if(input[0].equals("!list")) {
					list();
					continue;
				}
				else if(input[0].equals("!getClientList")) {
					requestClientList();
					continue;
				}
				else {
					System.out.println("Wrong command or wrong parameters. Only the following commands are allowed:");
					allCommands();
					continue;
				}
			}
		}
		if(!test) {
			exitClient();
		}
	}



	private void listWhileNotLoggedIn() {
		channel.send("!list".getBytes());

	}

	/*
	 * syntax: !login <username> <tcpPort> <client-challenge>
	 */
	public void login(String username) {
		boolean pwCorrect = false;
		while(!pwCorrect) {
			try {
				clientPrivateKey = keyReader.getPrivateKeyClient(username);
				pwCorrect = true;
			} catch (IOException e1) {
				LOG.error("private key couldn't be read");
				System.out.println("wrong pw - to try again press 'y' ");
				char stdIn = 0;
				try {
					stdIn = (char) new BufferedReader(new InputStreamReader(System.in)).read();
					if(stdIn != 'y') {
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		((RSAChannel)channel).setDecryptKey(clientPrivateKey);

		clientChallenge = MyRandomGenerator.createChallenge();
		//		byte[] testNumber = MyBase64.decode(clientChallenge);
		String msg = "!login" + " " + username + " " + serverTCPPort + " " + clientChallenge;
		// message encrypted using RSA initialized with the public key of the auction server
		// encode overall msg in base64
		channel.send(msg.getBytes());	

		//load IntegrityManger and client's secret key
		integrityManager = new IntegrityManager(pathToKeyDirectory);
		try {
			clientSecretKey = integrityManager.getSecretKey(username);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void establishTCPConnection() {
		try {
			clientSocket = new Socket(serverHost, serverTCPPort);
		} catch (UnknownHostException e) {
			System.out.println("Connection to Server could not be established - Unknown Host");
		} catch (IOException e) {
			System.out.println("Connection to Server could not be established - IOException");
		}
		try {
			channel = new RSAChannel(new Base64Channel(new TCPChannel(clientSocket)));
			((RSAChannel)channel).setEncryptKey(keyReader.getPublicKeyServer(pathToPublicServerKey));
		} catch (IOException e) {
			System.out.println("Communications with Server could not be established - IOException");
		}
	}

	/**
	 * Receives a message and processes it
	 * @param message
	 */
	public void receiveResponse(String message) throws IOException {
		String response = message;
		String[] splitResponse = response.split(" ");

		//part of handshake
		if(splitResponse[0].equals("!ok")) {
			//syntax: !ok <client-challenge> <server-challenge> <secret-key> <iv-parameter>
			String responseChallenge = splitResponse[1];
			String serverChallenge = splitResponse[2];
			Key secretKey = MyRandomGenerator.convertSecretKey(splitResponse[3]);
			AlgorithmParameterSpec iv = MyRandomGenerator.convertIV(splitResponse[4]);

			if(!responseChallenge.equals(clientChallenge)) {
				System.out.println("Access denied - server couldn't read your challenge; LOGGING OUT");
				LOG.info("server couldn't identify client");
				exitClient();
			} else {
				// initialize AES channel
				((RSAChannel) channel).setEncryptKeyAES(secretKey, iv);
				((RSAChannel) channel).setDecryptKeyAES(secretKey, iv);
				// return server challenge
				channel.send(serverChallenge.getBytes());
			}
		}

		/**
		 * accepts the following responses:
		 * login failed
		 * login successful <username>
		 */
		if(splitResponse[0].equals("login")) {

			if(splitResponse[1].equals("failed")) {
				System.out.println("Somebody is already logged in with that name. Please choose a different name and try again.");
			} 
			else if(splitResponse[1].equals("successful")) {
				System.out.println("successfully logged in as " + splitResponse[2] + "!");
				setUsername(splitResponse[2]);
				setLoggedIn();
				//TODO save private key in field
			}
			//verify Message
			boolean	verified = verifyMessage(message, splitResponse);
			if(!verified) {
				requestRepetition();
			}
		}

		/**
		 * accepts the following responses:
		 * logout failed
		 * logout successful <username>
		 */
		else if(splitResponse[0].equals("logout")) {
			if(splitResponse[1].equals("failed")) {
				System.out.println("logout failed");
			}
			else if(splitResponse[1].equals("successful")) {
				System.out.println("successfully logged out as " + splitResponse[2] + "!");
				setUsername("");
				setLoggedOut();
			}
			//verify Message
			boolean	verified = verifyMessage(message, splitResponse);
			if(!verified) {
				requestRepetition();
			}
		}

		/**
		 * accepts the following responses:
		 * list <list as String>
		 */
		else if(splitResponse[0].equals("list")) {
			if(!loggedIn) {
				System.out.println(buildList(splitResponse));
			}
			else {
				String removeFromMessage1 = splitResponse[splitResponse.length-1];
				String messageWithoutMAC = message.replace(removeFromMessage1, ""); 
				messageWithoutMAC = messageWithoutMAC.trim();

				try {

					// create an own hMAC
					byte[] ownMAC = integrityManager.createHashMAC(clientSecretKey, messageWithoutMAC);
					// read the hMAC from the message
					byte[] decodedHMAC = Base64.decode(splitResponse[splitResponse.length-1]); 

					//compare the two hMACs
					boolean match = integrityManager.verifyHashMAC(ownMAC, decodedHMAC);

					if(match) {
						String [] newSplitResponse = new String[splitResponse.length-1];
						for(int i = 0; i < splitResponse.length-1; i++) {
							newSplitResponse[i] = splitResponse[i];
						}
						System.out.println(buildList(newSplitResponse));
					}
					else {
						String [] newSplitResponse = new String[splitResponse.length-1];
						for(int i = 0; i < splitResponse.length-1; i++) {
							newSplitResponse[i] = splitResponse[i];
						}
						System.out.println(buildList(newSplitResponse));
						if(!secondAttemptRequested) {
							list();
							secondAttemptRequested = true;
						} else {
							secondAttemptRequested = false;
						}
					}

				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}

		}

		/**
		 * accepts the following responses:
		 * create failed
		 * create successful <ID> <date> <time> <timezone> <description> 
		 */
		else if(splitResponse[0].equals("create")) {
			if(splitResponse[1].equals("failed")) {
				System.out.println("creation of new auction failed");
			}
			else if(splitResponse[1].equals("successful")) {
				String description = "";
				for(int i = 6; i < splitResponse.length-1; i++) {
					description += splitResponse[i];
					description += " ";
				}
				description.trim();
				System.out.println("An auction '" + description + "' with id '" + splitResponse[2]
						+ "' has been created and will end on " 
						+ splitResponse[3] + " "
						+ splitResponse[4] + " "
						+ splitResponse[5] + ".");
			}
			//verify Message
			boolean	verified = verifyMessage(message, splitResponse);
			if(!verified) {
				requestRepetition();
			}
		}

		/**
		 * accepts the following responses:
		 * bid failed
		 * bid successful <amount> <description>
		 * bid unsuccessful <amount> <highestBid> <description>
		 */
		else if (splitResponse[0].equals("bid")) {
			if(splitResponse[1].equals("failed")) {
				System.out.println("placing of new bid failed");
			}
			else if(splitResponse[1].equals("successful")) {
				String description = "";
				for(int i = 3; i < splitResponse.length-1; i++) {
					description += splitResponse[i];
					description += " ";
				}
				description.trim();
				System.out.println("You successfully bid with " + splitResponse[2] + " on " + description +".");
			}
			else if(splitResponse[1].equals("unsuccessful")) {
				String description = "";
				for(int i = 4; i < splitResponse.length-1; i++) {
					description += splitResponse[i];
					description += " ";
				}
				description.trim();
				System.out.println("You unsuccessfully bid with " + splitResponse[2] + " on '" + description +"'."
						+ " Current highest bid is " + splitResponse[3]);
			}
			//verify Message
			boolean	verified = verifyMessage(message, splitResponse);
			if(!verified) {
				requestRepetition();
			}
		}

		/**
		 * accepts the following responses:
		 * !new-bid <description>
		 */
		else if(splitResponse[0].equals("!new-bid")) {
			String description = "";
			for(int i = 1; i < splitResponse.length-1; i++) {
				description += splitResponse[i];
				description += " ";
			}
			description.trim();
			System.out.println("You have been overbid on '" + description + "'.");
			//verify Message
			boolean	verified = verifyMessage(message, splitResponse);
			if(!verified) {
				requestRepetition();
			}
		}

		/**
		 * accepts the following responses:
		 * !auction-ended <winnerName> <amount> <description>  
		 */
		else if(splitResponse[0].equals("!auction-ended")) {
			String description = "";
			for(int i = 3; i < splitResponse.length-1; i++) {
				description += splitResponse[i];
				description += " ";
			}
			description.trim();
			if(!splitResponse[1].equals(this.username)) {
				System.out.println("The auction '" + description + "' has ended. " + splitResponse[1] + " won with "
						+ splitResponse[2] + ".");
			}
			else {
				System.out.println("The auction '" + description + "' has ended. You won with "
						+ splitResponse[2] + "!");
			}
			//verify Message
			boolean	verified = verifyMessage(message, splitResponse);
			if(!verified) {
				requestRepetition();
			}
		}

		/**
		 * accepts the following responses:
		 * !clientList <clientList>
		 */
		else if(splitResponse[0].equals("!clientList")) {
			outageHandler.buildClientListClientSide(splitResponse);
			System.out.println(outageHandler.getPrintableClientList());
		}


	}


	private void requestRepetition() {
		if(!secondAttemptRequested) {
			channel.send(("!repeat" + " " + username).getBytes());
			secondAttemptRequested = true;
		} else {
			secondAttemptRequested = false;
		}

	}

	/**
	 * Sends the !list command to the server
	 */
	public void list() {
		channel.send(("!list" + " " + username).getBytes());
	}

	/**
	 * Sends message to the server to create a new auction.
	 * @param seconds
	 * @param description
	 */
	public void createAuction(int seconds, String description) {
		channel.send(("!create" + " " + username + " "  + seconds + " " + description).getBytes());

	}

	/**
	 * Sends message to the server to request the list of all clients
	 */
	public void requestClientList() {
		channel.send(("!getClientList" + " " + username).getBytes());
	}

	/**
	 * Sends message to the server to place a bid on an item.
	 * @param ID
	 * @param amount
	 */
	public void placeBid(int ID, double amount) {
		if(outageMode) {
			outageHandler.sendTimestampRequest("!getTimeStamp" + " " + ID + " " + amount);
		} else {
			channel.send(("!bid" + " " + username + " " + ID + " " + amount).getBytes());
		}
		
	}

	/**
	 * Sends a message to the server to log the user out.
	 */
	public void logout() {
		channel.send(("!logout" + " " + username).getBytes());
	}

	/**
	 * builds a list out of a String array
	 * @param splitString
	 * @return String
	 */
	public String buildList(String[] splitString) {
		String output = "";

		for(int i = 1; i<splitString.length; i++){
			if(splitString[i].equals("EndLinE")) {
				output += "\n";
			} else {
				output += splitString[i];
				output += " ";
			}

		}
		output.trim();

		return output;
	}

	/**
	 * Prints out a list and explanation of all usable commands.
	 */
	public void allCommands() {
		System.out.println("!login <username> - Logs in the user <username> to the auction system. ");
		System.out.println("!logout - This command logs out the currently logged in user. ");
		System.out.println("!list - Lists all the currently active auctions.");
		System.out.println("!create <duration> <description> - Creates a new auction. The duration is given in seconds.");
		System.out.println("!bid <auction-id> <amount> - Bids the set amount on a specific auction.");
		System.out.println("!end - Shuts down the client.");

	}

	/**
	 * sets the username
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * returns the status of the client
	 * @return boolean
	 */
	public boolean getClientStatus() {
		return clientStatus;
	}

	/**
	 * Prints out information on how to properly start the client.
	 */
	public static void howToUse() {
		System.out.println("Parameters incorrect. Correct syntax: java Client <ServerHostname> <ServerTCPPort> <UDPPort>");
	}

	/**
	 * Sets the client as logged in
	 */
	public void setLoggedIn() {
		loggedIn = true;
	}

	/**
	 * Sets the client as logged out
	 */
	public void setLoggedOut() {
		loggedIn = false;
	}

	/**
	 * Getter for loggedIn
	 * @return boolean
	 */
	public boolean getLoggedIn() {
		return loggedIn;
	}
	
	public boolean getOutageMode() {
		return outageMode;
	}
	
	public void setOutageMode(boolean outageMode) {
		this.outageMode = outageMode;
	}


	/**
	 * Closes the client and logs the user out. Also closes the socket and all communications.
	 */
	public void exitClient() {
		//		clientUDPListenerThread.exit();
		clientStatus = false;
		if(getLoggedIn()) {
			logout();
		}
		in.close();
		try {
			if(clientSocket!=null) {
				clientSocket.close();
			}
			if(datagramSocket!=null) {
				datagramSocket.close();
			}
		} catch(IOException e) {
			System.out.println("Error while closing Socket!");
		}
	}
	public boolean verifyMessage(String message, String[] splitResponse) {

		String removeFromMessage1 = splitResponse[splitResponse.length-1];
		String messageWithoutMAC = message.replace(removeFromMessage1, ""); 
		messageWithoutMAC = messageWithoutMAC.trim();
		boolean match = true;
		try {

			// create an own hMAC
			byte[] ownMAC = integrityManager.createHashMAC(clientSecretKey, messageWithoutMAC);
			// read the hMAC from the message
			byte[] decodedHMAC = Base64.decode(splitResponse[splitResponse.length-1]); 

			//compare the two hMACs
			match = integrityManager.verifyHashMAC(ownMAC, decodedHMAC);

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return match;
	}

	public String getUserName() {
		return username;
	}

	/**
	 * 
	 * @return null if client is not known yet, private key otherwise
	 * @throws IOException
	 */
	public Key getOwnPrivateKey() {
		return clientPrivateKey;
	}

	public void startOutageMode() {
		setOutageMode(true);
		outageHandler.startOutageMode();

	}
	
	public int getClientPort() {
		return clientPort;
	}
}
