package testing;

import org.apache.log4j.Logger;

import server.Server;

public class AuctionListUpdater implements Runnable {

	public static final Logger LOG = Logger.getLogger(AuctionListUpdater.class);
	
	private int updateInterval;
	private TestComponent testComponent;

	public AuctionListUpdater(TestComponent testComponent, int updateInterval) {
		this.testComponent = testComponent;
		this.updateInterval = updateInterval;
	}

	@Override
	public void run() {

		while (true) {
			testComponent.randomAuction = Server.getRandomAuction();
			LOG.info("list update");
			try {
				Thread.sleep(updateInterval*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
