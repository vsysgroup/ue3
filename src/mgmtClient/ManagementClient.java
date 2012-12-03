package mgmtClient;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import registry.RegistryReader;
import analyticsServer.AnalyticsServerInterface;
import analyticsServer.Notify;
import billingServer.IBillingServer;
import billingServer.IBillingServerSecure;


/**
 * management client connects to analytics server and billing server via rmi
 * @author Barbara Schwankl 0852176
 * @author Philipp Pfeiffer 0809357
 *
 */
public class ManagementClient {

	public static final Logger LOG = Logger.getLogger(ManagementClient.class);
	private static String bindingNameBilling = "BillingServer";
	private static String bindingNameAnalytics = "AnalyticsServer";

	private static IBillingServer loginHandler = null;
	private IBillingServerSecure billingHandler = null;
	private static AnalyticsServerInterface analyticsHandler = null;
	private Scanner in = new Scanner(System.in);

	private boolean automaticPrintingOn = false;

	private ArrayList<String> subscriptions = new ArrayList<String>();
	private ArrayList<String> storedMessages = new ArrayList<String>();
	private boolean loggedIn = false;
	private String currUser = null;
	private Notify notify = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//init logger
		BasicConfigurator.configure();

		new ManagementClient();
	}

	public ManagementClient(String string) {

		lookupRMI();

		notify = new NotificationChecker(this);
		try {
			UnicastRemoteObject.exportObject(notify, 0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}
	public ManagementClient() {
		LOG.info("Starting Management Client");

		lookupRMI();

		notify = new NotificationChecker(this);
		try {
			UnicastRemoteObject.exportObject(notify, 0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		String[] cmd;
		while(in.hasNext()) {
			cmd = in.nextLine().split("\\s");
			if(cmd[0].equals("!login")) {
				if(loggedIn) {
					System.out.println("ERROR: You are already logged in");
				} else if(cmd.length != 3) {
					System.out.println("ERROR: Expected parameters: username, password");
				} else {
					String username = cmd[1];
					String pw = cmd[2];
					try {
						if(loginHandler == null) {
							loggedIn = false;
							LOG.error("Billing Server not available");							
						} else {
							billingHandler = loginHandler.login(username, pw);
							if (billingHandler == null) {
								loggedIn = false;
								LOG.info("user not authorized");
							} else {
								loggedIn = true;
								currUser = username;
								LOG.info(currUser + "successfully logged in");	
							}							
						}
					} catch (RemoteException e) {
						System.out.println("ERROR: login failed");
						LOG.error("remote login of " + username + " failed");
						e.printStackTrace();
					}
				}
			}
			else if(cmd[0].equals("!steps")) {
				if(!loggedIn) {
					System.out.println("ERROR: You have to log in first");
				} else if(cmd.length != 1) {
					System.out.println("ERROR: Expected parameters: none");
				}
				else {
					try {
						System.out.println(billingHandler.getPriceSteps());
					} catch (RemoteException e) {
						System.out.println("ERROR: getting price steps failed");
						LOG.error("couldnt get price steps");
						e.printStackTrace();
					}
					LOG.info("listed price steps");
				}

			}
			else if(cmd[0].equals("!addStep")) {
				if(!loggedIn) {
					System.out.println("ERROR: You have to log in first");
				}
				else {
					if(cmd.length != 5) {
						System.out.println("ERROR: Expected parameters: startPrice, endPrice, fixedPrice, variablePricePercent");
						LOG.error("Wrong no of parameters");
					} else {
						try {
							double startPrice = Double.parseDouble(cmd[1]);
							double endPrice = Double.parseDouble(cmd[2]);
							double fixedPrice = Double.parseDouble(cmd[3]);
							double variablePricePercent = Double.parseDouble(cmd[4]);
							try {
								billingHandler.createPriceStep(startPrice, endPrice, fixedPrice, variablePricePercent);
								if(endPrice == 0) {
									System.out.println("[" + startPrice + " INFINITY] successfully added");
								} else {
									System.out.println("[" + startPrice + " " + endPrice + "] successfully added");
								} 
							} catch (RemoteException e) {
								System.out.println(e.getMessage());
								LOG.error("create price steps failed");
								e.printStackTrace();
							} catch (NumberFormatException e) {
								System.out.println("ERROR: parameters must be floatingpoint values");
								LOG.error("wrong number format - supposed to be double");
								e.printStackTrace();
							}
						} catch (NumberFormatException e) {
							LOG.error("Invalid number format");
						}
					}
				}
			}
			else if(cmd[0].equals("!removeStep")) {
				if(!loggedIn) {
					System.out.println("ERROR: You have to log in first");
				} else {
					if(cmd.length != 3) {
						System.out.println("ERROR: Expected parameters: startPrice, endPrice");
						LOG.error("Wrong parameters");
					} else {
						double startPrice = 0;
						double endPrice = 0;
						try {
							startPrice = Double.parseDouble(cmd[1]);
							endPrice = Double.parseDouble(cmd[2]);
							billingHandler.deletePriceStep(startPrice, endPrice);
							System.out.println("Price step  [" + startPrice + " " + endPrice + "] successfully removed");
						} catch (RemoteException e) {
							System.out.println(e.getMessage());
							LOG.error("delete price step failed");
							e.printStackTrace();
						} catch (NumberFormatException e) {
							System.out.println("ERROR: parameters must be floatingpoint values");
							LOG.error("wrong number format - supposed to be double");
							e.printStackTrace();
						}
					}
				}
			}
			else if(cmd[0].equals("!bill")) {
				if(!loggedIn) {
					System.out.println("ERROR: You are currently not logged in.");
				} else {
					if(cmd.length != 2) {
						System.out.println("ERROR: Expected parameters: username");
						LOG.error("Wrong parameters");
					} else {
						String user = cmd[1];
						try {
							System.out.println(billingHandler.getBill(user));
						} catch (RemoteException e) {
							System.out.println(e.getMessage());
							LOG.error("getting bill failed");
							e.printStackTrace();
						}
					}
				}
			}
			else if(cmd[0].equals("!logout")) {
				if(!loggedIn) {
					System.out.println("ERROR: You have to log in first");
				} else {
					if(cmd.length != 1) {
						System.out.println("ERROR: Expected parameters: none");
						LOG.error("wrong params");
					} else {
						loggedIn = false;
						try {
							UnicastRemoteObject.unexportObject(billingHandler, true);
							billingHandler = null;
						} catch (NoSuchObjectException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println(currUser + "successfully logged out");
						currUser = null;
					}
				}
			}
			else if(cmd[0].equals("!subscribe")) {
				if(cmd.length != 2) {
					System.out.println("ERROR: Expected parameters: filter regex");
					LOG.error("Wrong parameters");
				} else {
					String filterRegex = cmd[1];
					String subscriptionID;
					try {
						subscriptionID = analyticsHandler.subscribe(filterRegex, notify);
						subscriptions.add(subscriptionID);
						LOG.info("Created subscription with ID " + subscriptionID + " for events using filter " + filterRegex);
					} catch (RemoteException e) {
						e.printStackTrace();
					}

				}
			}
			else if(cmd[0].equals("!unsubscribe")) {
				if(cmd.length != 2) {
					System.out.println("ERROR: Expected parameters: subscriptionID");
					LOG.error("Wrong parameters");
				} else {
					String subscriptionID = cmd[1];
					try {
						analyticsHandler.unsubscribe(subscriptionID);
						subscriptions.remove(subscriptions.indexOf(subscriptionID));
						LOG.info("subscription " + subscriptionID + " terminated");
					} catch (RemoteException e) {
						e.printStackTrace();
					}

				}
			}
			else if(cmd[0].equals("!auto")) {
				automaticPrintingOn = true;
				LOG.info("Automatic printing is ON");
			}
			else if(cmd[0].equals("!hide")) {
				automaticPrintingOn = false;
				LOG.info("Automatic printing is OFF");
			}
			else if(cmd[0].equals("!print")) {
				printList();
			}
			else if(cmd[0].equals("!exit")) {
				try {
					if(billingHandler != null) {
						UnicastRemoteObject.unexportObject(billingHandler, true);
						billingHandler = null;
						loggedIn = false;
						currUser = null;
					}
					UnicastRemoteObject.unexportObject(notify, true);
				} catch (NoSuchObjectException e) {
					e.printStackTrace();
				}
				break;
			}
			else {
				System.out.println("command unknown");
			}
		}
		LOG.info("Management Client shutting down");
	}

	private static void lookupRMI() {
		RegistryReader registryLocation = new RegistryReader();
		try {
			Registry registry = LocateRegistry.getRegistry(registryLocation.getHost(), registryLocation.getPort());
			try {
				loginHandler = (IBillingServer) registry.lookup(bindingNameBilling);
				LOG.info("BillingServer looked up");
			} catch (NotBoundException e) {
				LOG.info("this remote object doesnt exist / hasnt been bound - BillingServer");
			}
			try {
				analyticsHandler = (AnalyticsServerInterface) registry.lookup(bindingNameAnalytics);
				LOG.info("AnalyticsServer looked up");
			} catch (NotBoundException e) {
				LOG.info("this remote object doesnt exist / hasnt been bound - AnalyticsServer");
			}
		} catch (RemoteException e) {
			LOG.info("problem occurred trying to get registry");
		}
	}

	private void printList() {
		for(int i = 0; i < storedMessages.size(); i++) {
			System.out.println(storedMessages.get(i));
		}
		storedMessages.clear();
	}

	private void storeMessage(String message) {
		storedMessages.add(message);
	}

	public void inbox(String message) {
		if(automaticPrintingOn) {
			System.out.println(message);
		}
		else {
			storeMessage(message);
		}	
	}
	
	public void setToAuto() {
		automaticPrintingOn = true;
	}
	
	public void subscribe(String regex) {
		String subscriptionID;
		try {
			subscriptionID = analyticsHandler.subscribe(regex, notify);
			subscriptions.add(subscriptionID);
			LOG.info("Created subscription with ID " + subscriptionID + " for events using filter " + regex);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void subscribeAll() {
		String regex = "'(USER_.*)|(BID_.*)|(AUCTION_.*)'";
		String subscriptionID;
		try {
			subscriptionID = analyticsHandler.subscribe(regex, notify);
			subscriptions.add(subscriptionID);
			LOG.info("Created subscription with ID " + subscriptionID + " for events using filter " + regex);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
