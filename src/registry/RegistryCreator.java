package registry;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.Logger;

/**
 * creates registry once;
 * @author Barbara Schwankl 0852176
 *
 */
public class RegistryCreator {

	public static final Logger LOG = Logger.getLogger(RegistryCreator.class);

	private static RegistryCreator instance = null;
	RegistryReader registryLocation = new RegistryReader();

	public static synchronized RegistryCreator getInstance() {
		if(instance == null) {
			instance = new RegistryCreator();
		}
		return instance;
	}

	private RegistryCreator() {
		try {
			LocateRegistry.createRegistry(registryLocation.getPort());
		} catch (RemoteException e) {			
			LOG.info("registry has already been created");
		}
		LOG.info("registry created");
	}
}
