package analyticsServer;

/**
 * This class represents an AuctionEvent.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class AuctionEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long auctionID = 0;

	public AuctionEvent(String type, long auctionID) {
		super(type);
		this.auctionID = auctionID;
		
	}

	public long getAuctionID() {
		return auctionID;
	}

}
