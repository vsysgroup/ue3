package testing;

import org.apache.log4j.Logger;

import client.Client;

/**
 * test clients that creates a bid for a given number of times a minute
 * @author Barbara Schwankl 0852176
 *
 */
public class AuctionCreator implements Runnable {

	public static final Logger LOG = Logger.getLogger(AuctionCreator.class);
	private static int auctionNo = 0;
	
	private int sleepDurationCreation;
	private int auctionDuration;
	private Client client;

	public AuctionCreator(Client client, int auctionDuration,
			int sleepDurationCreation) {
		this.client = client;
		this.auctionDuration = auctionDuration;
		this.sleepDurationCreation = sleepDurationCreation;
	}

	@Override
	public void run() {

		while(true) {
			client.createAuction(auctionDuration, "test");
			LOG.info("test auction created with no: " + auctionNo);
			
			try {
				Thread.sleep(sleepDurationCreation);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
