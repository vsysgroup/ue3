package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GroupBidManager {
	
	private ArrayList<GroupBid> potentialGroupBids = new ArrayList<GroupBid>();
	
	private static List<Auction> auctions;	
	private Server server;
	
	
	public GroupBidManager(Server server,  List<Auction> auctions) {
		this.server = server;
		this.auctions = auctions;
	
	}
	
	public void AddNewPotentialGroupBid(User bidder, int auctioinID, Double amount) {
		
		// do checks and create new groupBid - gibt es eine auction dazu, etc. 
		
		// todo: Implement Logic
		System.out.println("New Group Bid");
	}
	
	public void AddConfirmation(User confirmer, int auctionID, Double amount) {
		
		// todo: Implement Logic
		
		// check if potentialGroupBid with same auctiond and amount exists
		
		// add new confirmer to auction
		
		// if 2 confirmers exist: send confirmed
		
		// think about usecases for release
		
		System.out.println("New Confirmation");
	}

}
