package analyticsServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interfaces is used as for a remote object that enables Callbacks via it's notify() method.
 * @author Philipp Pfeiffer 0809357
 *
 */
public interface Notify extends Remote{
	
	public void notify(Event event) throws RemoteException;

}
