package billingServer;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * contains implementation of methods accessible by an admin (mgmt client)
 * @author Barbara Schwankl 0852176
 *
 */
public class BillingServerSecureImpl implements IBillingServerSecure, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static BillingServerSecureImpl instance = null;
	public static final Logger LOG = Logger.getLogger(BillingServerSecureImpl.class);
	private PriceSteps priceSteps = PriceSteps.getInstance();
	private Map<String, Bill> userBills = new HashMap<String, Bill>();

	public static synchronized BillingServerSecureImpl getInstance() throws RemoteException {
		if(instance == null) {
			instance = new BillingServerSecureImpl();
			UnicastRemoteObject.exportObject(instance, 0);
		}
		return instance;
	}

	private BillingServerSecureImpl() {
		//		LOG.info("i was called YIHAA");
	}

	public PriceSteps getPriceSteps() throws RemoteException {
		return priceSteps;
	}


	public void createPriceStep(double startPrice, double endPrice, double fixedPrice, double variablePricePercent) throws RemoteException {
		if(endPrice == 0) {
			endPrice = Double.POSITIVE_INFINITY;
		} else if((startPrice < 0) || (endPrice < 0) || (fixedPrice < 0) || (variablePricePercent < 0)) {
			throw new RemoteException("ERROR: One of the parameters is negative");
		}
		boolean success = priceSteps.createStep(startPrice, endPrice, fixedPrice, variablePricePercent);
		if(!success) {
			throw new RemoteException("ERROR: Your price interval collides with existing price step. Please delete the existing one first.");
		}
	}


	public void deletePriceStep(double startPrice, double endPrice) throws RemoteException {
		if(endPrice == 0) {
			endPrice = Double.POSITIVE_INFINITY;
		}
		boolean success = priceSteps.deleteStep(startPrice, endPrice);
		if(!success) {
			throw new RemoteException("ERROR: Price step [" + startPrice + " " + endPrice + "] does not exist");
		}
	}


	public void billAuction(String user, long auctionID, double price) {
		Bill bill = userBills.get(user);
		if(bill == null) {
			bill = new Bill();
			bill.setUser(user);
			userBills.put(user, bill);
		}
		bill.addBillEntry(auctionID, price);		
	}


	public Bill getBill(String user) throws RemoteException {
		if(!userBills.containsKey(user)) {
			throw new RemoteException("ERROR: user does not exist");
		} else {
			Bill bill = userBills.get(user);
			bill.compute(priceSteps);
			return bill;
		}
	}
}
