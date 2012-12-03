package analyticsServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface provides methods available for remote calls.
 * @author Philipp Pfeiffer 0809357
 *
 */
public interface AnalyticsServerInterface extends Remote {
	
	public String subscribe(String filter, Notify n) throws RemoteException;
	
	public void processEvent(Event event) throws RemoteException;
	
	public void unsubscribe(String identifier) throws RemoteException;
	
}
