package analyticsServer;

/**
 * This class represents different Auction statistics.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class AuctionStats {

	private long auctionID;
	private long startingTime;
	private long endingTime;
	
	public AuctionStats(long auctionID) {
		this.auctionID = auctionID;
	}

	public long getAuctionID() {
		return auctionID;
	}

	public void startAuction(long startingTime) {
		this.startingTime = startingTime;
		
	}

	public void endAuction(long endingTime) {
		this.endingTime = endingTime;
	}
	
	public long getStartingTime() {
		return startingTime;
	}
	
	public long getEndingTime() {
		return endingTime;
	}
	
	public long getTotalTime() {
		return (endingTime - startingTime);
	}
}
