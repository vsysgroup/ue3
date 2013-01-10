package server;

import java.util.ArrayList;
import java.util.List;

import communication.Channel;

import client.Client;

public class GroupBidManager {

	private ArrayList<GroupBid> potentialGroupBids = new ArrayList<GroupBid>();

	private static List<Auction> auctions;
	private Server server;


	public GroupBidManager(Server server,  List<Auction> auctions) {
		this.server = server;
		this.auctions = auctions;

	}

	public void AddNewPotentialGroupBid(User bidder, int auctionID, Double amount) {

		// do checks and create new groupBid - gibt es eine auction dazu, etc. 
		if(getAuctionByID(auctionID) != null) {
			GroupBid bid = new GroupBid(bidder, auctionID, amount);
			if(getAuctionByID(auctionID).getHighestBid() >= amount) {
				bidder.getChannel().send("!rejected Please use higher bid price".getBytes());
			} else {
				boolean addSuccess = potentialGroupBids.add(bid);
				bidder.getChannel().send("groupBidAccepted: create group bid successful; 2 others need to confirm: !confirm <auctionID> <amount> <ownerName>".getBytes());
			}
		} else {
			bidder.getChannel().send("create group bid not successful".getBytes());
		}

		// todo: Implement Logic
		System.out.println("New Group Bid");
	}

	public void AddConfirmation(User confirmer, int auctionID, Double amount) {

		boolean success = false;
		// check if potentialGroupBid with same auctionId and amount exists
		for(GroupBid b: potentialGroupBids) {
			int i = b.getAuctionID();
			double d = b.getAmount();
			if((i == auctionID) && (d == amount)) {

				if(confirmer.getUsername().equals(b.getBidder().getUsername())) {
					confirmer.getChannel().send("group bid creator mustn't confirm!".getBytes());
				}
				else {
					success = true;

					//groupBid exists
					// add new confirmer to auction
					b.addConfirmer(confirmer);
					confirmer.getChannel().send("confirm sent; wait for second response".getBytes());
					if(b.getConfirmers().size() == 2) {
						b.getConfirmers().get(0).getChannel().send("!confirmed".getBytes());
						b.getConfirmers().get(1).getChannel().send("!confirmed".getBytes());
						//add auction bid
						confirmGroupBid(auctionID, amount, b.getBidder(), b.getBidder().getChannel());
					}
				}
			}
		}
		if(!success) {
			String fail = "confirmation failed";
			confirmer.getChannel().send(fail.getBytes());
		}

		// todo: Implement Logic
		// think about usecases for release

		System.out.println("New Confirmation");
	}

	private void confirmGroupBid(int id, Double amount, User bidder, Channel channel) {
		server.findAuctionByID(id).newBid(bidder, amount, channel, true);

	}

	private GroupBid getGroupBidById(int auctionID) {
		for(GroupBid b: potentialGroupBids) {
			if(b.getAuctionID() == auctionID) {
				return b;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param id
	 * @return null if auction id doesn't exist
	 */
	private Auction getAuctionByID(int id) {

		for(Auction a: auctions) {
			if(a.getID() == id) {
				return a;
			}
		}
		return null;
	}

}
