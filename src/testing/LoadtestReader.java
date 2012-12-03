package testing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * reads out parameters for testing component
 * @author Barbara Schwankl 0852176
 *
 */
public class LoadtestReader {
	public static final Logger LOG = Logger.getLogger(LoadtestReader.class);

	private int noOfClients, auctionsPerMin, auctionDuration, updateIntervalSec, bidsPerMin;
	
	public LoadtestReader() {
		InputStream registryPropertiesFile = ClassLoader.getSystemResourceAsStream("loadtest.properties");
		if(registryPropertiesFile != null) {
			Properties registryProperties = new Properties();
			try {
				registryProperties.load(registryPropertiesFile);
			} catch (IOException e) {
				LOG.info("loading properties failed");
			}
			noOfClients = Integer.parseInt(registryProperties.getProperty("clients"));
			auctionsPerMin = Integer.parseInt(registryProperties.getProperty("auctionsPerMin"));
			
			String auctionDurationUnformatted = registryProperties.getProperty("auctionDuration");
			String[] tmp = auctionDurationUnformatted.split("\\*");
			auctionDuration = Integer.parseInt(tmp[0]) * Integer.parseInt(tmp[1]);
			
			updateIntervalSec = Integer.parseInt(registryProperties.getProperty("updateIntervalSec"));
			bidsPerMin = Integer.parseInt(registryProperties.getProperty("bidsPerMin"));
			
			try {
				registryPropertiesFile.close();
			} catch (IOException e) {
				LOG.info("closing proberty stream failed");
			}
		} 
	}

	public int getNoOfClients() {
		return noOfClients;
	}

	public int getAuctionsPerMin() {
		return auctionsPerMin;
	}

	public int getAuctionDuration() {
		return auctionDuration;
	}

	public int getUpdateIntervalSec() {
		return updateIntervalSec;
	}

	public int getBidsPerMin() {
		return bidsPerMin;
	}
	
}
