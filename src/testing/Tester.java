package testing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.BasicConfigurator;

import mgmtClient.ManagementClient;


/**
 * creates a new thread for each testing client
 * @author Barbara Schwankl 0852176
 *
 */
public class Tester {

	private final static ExecutorService threadpool = Executors.newCachedThreadPool();

	public static void main(String[] args) {

		BasicConfigurator.configure();
		
		LoadtestReader testParameters = new LoadtestReader();
		
		//number of concurrent bidding client
		int noOfClients = testParameters.getNoOfClients();
		//number of started auctions per client per minute
		int auctionsPerMin = testParameters.getAuctionsPerMin();
		//duration of the auctions in seconds 
		int auctionDuration = testParameters.getAuctionDuration();
		//number of seconds that have to pass before the clients repeatedly update the current list of active auctions
		int updateIntervalSec = testParameters.getUpdateIntervalSec();
		//number of bids placed on random auctions per client per minute
		int bidsPerMin = testParameters.getBidsPerMin();

		//only one mgmt client
		//eventSubscription on any event type (filter ".*") & auto mode
		//auto mode
		ManagementClient admin = new ManagementClient("test");
		admin.subscribeAll();
		admin.setToAuto();
		
		//several clients
		for(int i = 0; i < noOfClients; i++) {

			threadpool.execute(new TestComponent(auctionsPerMin, auctionDuration, updateIntervalSec, bidsPerMin));
		}
	}
}
