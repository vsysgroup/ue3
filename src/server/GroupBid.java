package server;

import java.util.ArrayList;
import java.util.Date;

public class GroupBid {
	
	User bidder;
	
	int auctionID;
	
	Double amount;
	
	private ArrayList<User> confirmers = new ArrayList<User>();

	
	
	
	
	public GroupBid(User bidder, int auctionID, Double amount) {
		super();
		this.bidder = bidder;
		this.auctionID = auctionID;
		this.amount = amount;
	}
	
	

	public User getBidder() {
		return bidder;
	}

	public void setBidder(User bidder) {
		this.bidder = bidder;
	}

	public int getAuctionID() {
		return auctionID;
	}

	public void setAuctionID(int auctionID) {
		this.auctionID = auctionID;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public ArrayList<User> getConfirmers() {
		return confirmers;
	}

	public void setConfirmers(ArrayList<User> confirmers) {
		this.confirmers = confirmers;
	}
	
	
	
	
}
