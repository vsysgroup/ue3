package client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import communication.TCPCommunication;

import exception.WrongParameterCountException;

/**
 * Represents a Client
 * @author Philipp Pfeiffer 0809357
 * 
 */
public class Client {

	private String serverHost;
	private int serverTCPPort;
//	private int udpPort;
	private Socket clientSocket = null;
	private Boolean clientStatus = false;
	private Boolean loggedIn = false;
	private Scanner in = new Scanner(System.in);
	private TCPCommunication tcpCommunication = null;
	private String username = "";
	private DatagramSocket datagramSocket = null;
//	private ClientUDPListenerThread clientUDPListenerThread = null;
	private ClientTCPListenerThread clientTCPListenerThread = null;

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
		//check if parameters are alright
//		if(args.length != 2) {
//			throw new WrongParameterCountException();
//		} else {
		boolean test = false;
			this.serverHost = args[0];
			this.serverTCPPort = Integer.parseInt(args[1]);
			if(args.length >= 4) {
				test = true;
			}
//			this.udpPort = Integer.parseInt(args[2]);
//		}

		clientStatus = true;

		System.out.println("Starting Client.");
		
		establishTCPConnection();
		clientTCPListenerThread = new ClientTCPListenerThread(tcpCommunication,this);
		clientTCPListenerThread.start();

		//!!LAB2: NO UDP!!
//		try {
//			datagramSocket = new DatagramSocket(this.udpPort);
//		} catch (SocketException e) {
//			System.out.println("Could not bind to UDP port! The port may be in use." + " Port: " + udpPort);
//			exitClient();
//		}
//		//		Start the listener threads
//		new ClientTCPListenerThread(tcpCommunication, this).start();
//		clientUDPListenerThread = new ClientUDPListenerThread(datagramSocket, this);
//		clientUDPListenerThread.start();


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
						description.trim();
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
		tcpCommunication.send("!list");
		
	}

	public void login(String username) {
		tcpCommunication.send("!login" + " " + username);
//		tcpCommunication.send("!login" + " " + username + " " + udpPort);
		
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
			tcpCommunication = new TCPCommunication(clientSocket);
		} catch (IOException e) {
			System.out.println("Communications with Server could not be established - IOException");
		}
	}

	/**
	 * Receives a message and processes it
	 * @param message
	 */
	public void receiveResponse(String message) {
		String response = message;
		String[] splitResponse = response.split(" ");

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
		}

		/**
		 * accepts the following responses:
		 * list <list as String>
		 */
		else if(splitResponse[0].equals("list")) {
			System.out.println(buildList(splitResponse));
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
				for(int i = 6; i < splitResponse.length; i++) {
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
				for(int i = 3; i < splitResponse.length; i++) {
					description += splitResponse[i];
					description += " ";
				}
				description.trim();
				System.out.println("You successfully bid with " + splitResponse[2] + " on " + description +".");
			}
			else if(splitResponse[1].equals("unsuccessful")) {
				String description = "";
				for(int i = 4; i < splitResponse.length; i++) {
					description += splitResponse[i];
					description += " ";
				}
				description.trim();
				System.out.println("You unsuccessfully bid with " + splitResponse[2] + " on '" + description +"'."
						+ " Current highest bid is " + splitResponse[3]);
			}
		}

		/**
		 * accepts the following responses:
		 * !new-bid <description>
		 */
		else if(splitResponse[0].equals("!new-bid")) {
			String description = "";
			for(int i = 1; i < splitResponse.length; i++) {
				description += splitResponse[i];
				description += " ";
			}
			description.trim();
			System.out.println("You have been overbid on '" + description + "'.");

		}

		/**
		 * accepts the following responses:
		 * !auction-ended <winnerName> <amount> <description>  
		 */
		else if(splitResponse[0].equals("!auction-ended")) {
			String description = "";
			for(int i = 3; i < splitResponse.length; i++) {
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
		}
	}

	/**
	 * This method is responsible for sending a message to the server to log the user in.
	 * @param username
	 * @return TCPCommunication
	 */
//	public TCPCommunication login(String username) {
//		//create a socket for the connection with the server
//		try {
//			clientSocket = new Socket(serverHost, serverTCPPort);
//		} catch (UnknownHostException e) {
//			System.out.println("Could not resolve host or port!");
//			exitClient();
//		} catch (IOException e) {
//			System.out.println("Could not create socket!");
//			exitClient();
//		}
//		TCPCommunication communication = null;
//
//		try {
//			communication = new TCPCommunication(clientSocket);
//		} catch (IOException e) {
//			System.out.println("Error while creating BufferedReader or PrintWriter!");
//			exitClient();
//		}
//		//		communication.send("!login" + " " + username);
//		communication.send("!login" + " " + username + " " + udpPort);
//		return communication;
//	}

	/**
	 * Sends the !list command to the server
	 */
	public void list() {
		tcpCommunication.send("!list" + " " + username);
	}

	/**
	 * Sends message to the server to create a new auction.
	 * @param seconds
	 * @param description
	 */
	public void createAuction(int seconds, String description) {
		tcpCommunication.send("!create" + " " + username + " "  + seconds + " " + description);

	}

	/**
	 * Sends message to the server to place a bid on an item.
	 * @param ID
	 * @param amount
	 */
	public void placeBid(int ID, double amount) {
		tcpCommunication.send("!bid" + " " + username + " " + ID + " " + amount);
	}

	/**
	 * Sends a message to the server to log the user out.
	 */
	public void logout() {
		tcpCommunication.send("!logout" + " " + username);
	}

	/**
	 * builds a list out of a String array
	 * @param splitString
	 * @return String
	 */
	public String buildList(String[] splitString) {
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


}
