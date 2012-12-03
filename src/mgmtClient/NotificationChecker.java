package mgmtClient;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import analyticsServer.AuctionEvent;
import analyticsServer.BidEvent;
import analyticsServer.Event;
import analyticsServer.Notify;
import analyticsServer.StatisticsEvent;
import analyticsServer.UserEvent;

/**
 * This class implements the Notify interface and acts as a callback object, used by the ManagementClient to receive and
 * process notifications from the AnalyticsServer.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class NotificationChecker implements Notify, Serializable {
	public static final Logger LOG = Logger.getLogger(NotificationChecker.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ManagementClient managementClient = null;
	
	private ArrayList<String> knownAuctionEvents = new ArrayList<String>();
	private ArrayList<String> knownUserEvents = new ArrayList<String>();
	private ArrayList<String> knownBidEvents = new ArrayList<String>();
	private ArrayList<String> knownStatisticsEvents = new ArrayList<String>();
	
	public NotificationChecker(ManagementClient managementClient) {
		this.managementClient = managementClient;
	}

	@Override
	public void notify(Event event) throws RemoteException {
		
		checkForSpace();

		if(event instanceof AuctionEvent) {
			if(event.getType().equals("AUCTION_STARTED")) {
				AuctionEvent auctionEvent = (AuctionEvent) event;
				String message = buildMessage(auctionEvent.getType(), timeStampToString(auctionEvent.getTimeStamp()), "Auction with ID " + auctionEvent.getAuctionID() + " started");
				if(!knownAuctionEvents.contains(event.getID())) {
					knownAuctionEvents.add(event.getID());
					managementClient.inbox(message);
				}
				
			}
			else if(event.getType().equals("AUCTION_ENDED")) {
				AuctionEvent auctionEvent = (AuctionEvent) event;
				String message = buildMessage(auctionEvent.getType(), timeStampToString(auctionEvent.getTimeStamp()), "Auction with ID " + auctionEvent.getAuctionID() + " ended");
				if(!knownAuctionEvents.contains(event.getID())) {
					knownAuctionEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
		}
		
		//BidEvent
		else if(event instanceof BidEvent) {
			if(event.getType().equals("BID_PLACED")) {
				BidEvent bidEvent = (BidEvent) event;
				String message = buildMessage(bidEvent.getType(), timeStampToString(bidEvent.getTimeStamp()), "User " + bidEvent.getUserName() + " placed bid " + bidEvent.getPrice() + " on Auction " + bidEvent.getAuctionID());
				if(!knownBidEvents.contains(event.getID())) {
					knownBidEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("BID_OVERBID")) {
				BidEvent bidEvent = (BidEvent) event;
				String message = buildMessage(bidEvent.getType(), timeStampToString(bidEvent.getTimeStamp()), "User " + bidEvent.getUserName() + " has been overbid on Auction " + bidEvent.getAuctionID());
				if(!knownBidEvents.contains(event.getID())) {
					knownBidEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("BID_WON")) {
				BidEvent bidEvent = (BidEvent) event;
				String message = buildMessage(bidEvent.getType(), timeStampToString(bidEvent.getTimeStamp()), "User " + bidEvent.getUserName() + " won Auction " + bidEvent.getAuctionID() + " with " + bidEvent.getPrice());
				if(!knownBidEvents.contains(event.getID())) {
					knownBidEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
		}
		
		//StatisticsEvent
		else if(event instanceof StatisticsEvent) {
			if(event.getType().equals("USER_SESSIONTIME_MIN")) {
				StatisticsEvent statisticsEvent = (StatisticsEvent) event;
				String message = buildMessage(statisticsEvent.getType(), timeStampToString(statisticsEvent.getTimeStamp()), "minimum session time is " + statisticsEvent.getValue()/1000 + " seconds");
				if(!knownStatisticsEvents.contains(event.getID())) {
					knownStatisticsEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("USER_SESSIONTIME_MAX")) {
				StatisticsEvent statisticsEvent = (StatisticsEvent) event;
				String message = buildMessage(statisticsEvent.getType(), timeStampToString(statisticsEvent.getTimeStamp()), "maximum session time is " + statisticsEvent.getValue()/1000 + " seconds");
				if(!knownStatisticsEvents.contains(event.getID())) {
					knownStatisticsEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("USER_SESSIONTIME_AVG")) {
				StatisticsEvent statisticsEvent = (StatisticsEvent) event;
				String message = buildMessage(statisticsEvent.getType(), timeStampToString(statisticsEvent.getTimeStamp()), "average session time is " + statisticsEvent.getValue()/1000 + " seconds");
				if(!knownStatisticsEvents.contains(event.getID())) {
					knownStatisticsEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("BID_PRICE_MAX")) {
				StatisticsEvent statisticsEvent = (StatisticsEvent) event;
				String message = buildMessage(statisticsEvent.getType(), timeStampToString(statisticsEvent.getTimeStamp()), "maximum price seen so far is " + statisticsEvent.getValue());
				if(!knownStatisticsEvents.contains(event.getID())) {
					knownStatisticsEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("BID_COUNT_PER_MINUTE")) {
				StatisticsEvent statisticsEvent = (StatisticsEvent) event;
				String message = buildMessage(statisticsEvent.getType(), timeStampToString(statisticsEvent.getTimeStamp()), "current bids per minute is " + statisticsEvent.getValue());
				if(!knownStatisticsEvents.contains(event.getID())) {
					knownStatisticsEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("AUCTION_TIME_AVG")) {
				StatisticsEvent statisticsEvent = (StatisticsEvent) event;
				String message = buildMessage(statisticsEvent.getType(), timeStampToString(statisticsEvent.getTimeStamp()), "average auction time is " + statisticsEvent.getValue()/1000 + " seconds");
				if(!knownStatisticsEvents.contains(event.getID())) {
					knownStatisticsEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
			else if(event.getType().equals("AUCTION_SUCCESS_RATIO")) {
				StatisticsEvent statisticsEvent = (StatisticsEvent) event;
				String message = buildMessage(statisticsEvent.getType(), timeStampToString(statisticsEvent.getTimeStamp()), "current auction success ratio is " + statisticsEvent.getValue());
				if(!knownStatisticsEvents.contains(event.getID())) {
					knownStatisticsEvents.add(event.getID());
					managementClient.inbox(message);
				}
			}
		}
		
		//UserEvent
		else if(event instanceof UserEvent) {
			if(event.getType().equals("USER_LOGIN")) {
				UserEvent userEvent = (UserEvent) event;
				String message = buildMessage(userEvent.getType(), timeStampToString(userEvent.getTimeStamp()), "user " + userEvent.getUserName() + " has logged in");
				if(!knownUserEvents.contains(event.getID())) {
					knownUserEvents.add(event.getID());
					managementClient.inbox(message);
				}			}
			else if(event.getType().equals("USER_LOGOUT")) {
				UserEvent userEvent = (UserEvent) event;
				String message = buildMessage(userEvent.getType(), timeStampToString(userEvent.getTimeStamp()), "user " + userEvent.getUserName() + " has logged out");
				if(!knownUserEvents.contains(event.getID())) {
					knownUserEvents.add(event.getID());
					managementClient.inbox(message);
				}			}
			else if(event.getType().equals("USER_DISCONNECTED")) {
				UserEvent userEvent = (UserEvent) event;
				String message = buildMessage(userEvent.getType(), timeStampToString(userEvent.getTimeStamp()), "user " + userEvent.getUserName() + " has disconnected");
				if(!knownUserEvents.contains(event.getID())) {
					knownUserEvents.add(event.getID());
					managementClient.inbox(message);
				}			}
		}
	}
	
	private void checkForSpace() {
		if(knownAuctionEvents.size()>1000) {
			for(int i = 0; i < 500; i++) {
				knownAuctionEvents.remove(0);
			}
		}
		if(knownUserEvents.size()>1000) {
			for(int i = 0; i < 500; i++) {
				knownUserEvents.remove(0);
			}
		}
		if(knownBidEvents.size()>1000) {
			for(int i = 0; i < 500; i++) {
				knownBidEvents.remove(0);
			}
		}
		if(knownStatisticsEvents.size()>1000) {
			for(int i = 0; i < 500; i++) {
				knownStatisticsEvents.remove(0);
			}
		}
	}

	private String timeStampToString(long timeStamp) {
		String returnString = "";
		
		Date date = new Date();
		date.setTime(timeStamp);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int day = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		
		int hours = calendar.get(Calendar.HOUR);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		
		String timezone = "CET";
		
		returnString = day + "." + month + "." + year + " " + hours + ":" + minutes + ":" + seconds + " " + timezone;
		
		return returnString;
	}
	
	private String buildMessage(String type, String time, String message) {
		String returnString = "";
		
		returnString += type + ": " + time + " - " + message;
		
		return returnString;
	}

}
