package billingServer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import registry.RegistryCreator;
import registry.RegistryReader;

/**
 * responsible for billing;
 * addressed by Management Clients and Auction Server via RMI
 * @author Barbara Schwankl 0852176
 *
 */
public class BillingServer {

	public static final Logger LOG = Logger.getLogger(BillingServer.class);
	private static String bindingName;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//init logger
		BasicConfigurator.configure();
		
		new BillingServer(args);
		
		setupRMI();
	}
	
	public BillingServer(String[] args) {
		bindingName = args[0];
	}

	/**
	 * http://docs.oracle.com/javase/tutorial/rmi/implementing.html
	 */
	private static void setupRMI() {
		RegistryReader registryLocation = new RegistryReader();
		IBillingServer login = new BillingServerImpl();
		try {
			RegistryCreator.getInstance();
			//jdoc: if port is zero, an anonymous port is chosen
			IBillingServer stub = (IBillingServer) UnicastRemoteObject.exportObject(login, 0);
			Registry registry = LocateRegistry.getRegistry(registryLocation.getHost(), registryLocation.getPort());
			registry.bind(bindingName, stub);
			LOG.info("registry bound");
		} catch (RemoteException e) {
			LOG.info("error getting registry");
		} catch (AlreadyBoundException e) {
			LOG.info("object already bound");
		}
	}
}
