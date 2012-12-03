package registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * loads the registry details from properties
 * @author Barbara Schwankl 0852176
 *
 */
public class RegistryReader {
	
	public static final Logger LOG = Logger.getLogger(RegistryReader.class);

	private String registryHost;
	private int registryPort;
	
	public RegistryReader() {
		InputStream registryPropertiesFile = ClassLoader.getSystemResourceAsStream("registry.properties");
		if(registryPropertiesFile != null) {
			Properties registryProperties = new Properties();
			try {
				registryProperties.load(registryPropertiesFile);
			} catch (IOException e) {
				LOG.info("loading properties failed");
			}
			registryHost = registryProperties.getProperty("registry.host");
			registryPort = Integer.parseInt(registryProperties.getProperty("registry.port"));
			try {
				registryPropertiesFile.close();
			} catch (IOException e) {
				LOG.info("closing proberty stream failed");
			}
		} 
	}
	
	public String getHost() {
		return registryHost;
	}
	
	public int getPort() {
		return registryPort;
	}
}
