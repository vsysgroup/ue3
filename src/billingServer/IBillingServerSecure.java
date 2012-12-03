package billingServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * interface for methods accessible by a logged in admin (mgmt client)
 * @author Barbara Schwankl 0852176
 *
 */
public interface IBillingServerSecure extends Remote {

	/**
	 * returns current configuration of price steps
	 * TODO glaub da ist ein fehler in der angabe: diese methode sollte einen String zurückgeben
	 * @return price steps
	 */
	public PriceSteps getPriceSteps() throws RemoteException;
	
	/**
	 * allows to create a price step for a given price interval
	 * @param startPrice
	 * @param endPrice
	 * @param fixedPrice
	 * @param variablePricePercent
	 * @throws RemoteException any of the specified values is negative OR the provided price interval collides (overlaps) with an existing price step
	 */
	public void createPriceStep(double startPrice, double endPrice, double fixedPrice, double variablePricePercent) throws RemoteException;
	
	/**
	 * allows to delete a price step for the pricing curve
	 * @param startPrice
	 * @param endPrice
	 * @throws RemoteException thrown if the specified interval does not match an existing price step interval
	 */
	public void deletePriceStep(double startPrice, double endPrice) throws RemoteException;
	
	/**
	 * called by auction server as soon as auction has ended.
	 * stores the auction result.
	 * uses this information to calculate the bill for a user.
	 * @param user
	 * @param auctionID
	 * @param price
	 */
	public void billAuction(String user, long auctionID, double price) throws RemoteException;
	
	/**
	 * calculates and returns the bill for a given user, based on the price steps stored within the billing server.
	 * @param user
	 * @return
	 */
	public Bill getBill(String user) throws RemoteException;
	
}
