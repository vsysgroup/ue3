package analyticsServer;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

import org.apache.log4j.Logger;

/**
 * This class is the implementation of AnalyticsServerInterface. It also contains all methods used to calculate statistics and
 * processes all incoming events
 * @author Philipp Pfeiffer 0809357
 *
 */
public class AnalyticsServerImpl implements AnalyticsServerInterface, Serializable{
	
	public static final Logger LOG = Logger.getLogger(AnalyticsServerImpl.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, AuctionUser> userStats = Collections.synchronizedMap(new ConcurrentHashMap<String, AuctionUser>());
	private Map<Long, AuctionStats> auctionStats = Collections.synchronizedMap(new ConcurrentHashMap<Long, AuctionStats>());
	private Map<String, Subscription> subscriptions = Collections.synchronizedMap(new ConcurrentHashMap<String, Subscription>());
	
	
	private long totalMinSessionTime = 0;
	private long totalMaxSessionTime = 0;
	private long totalAvgSessionTime = 0;
	private long totalSessionTime = 0;
	private int totalSessionAmount = 0;
	
	private double totalMaxBidPrice = 0;
	private int totalAmountOfBids = 0;
	private double totalBidsPerMinute = 0;
	
	private int totalNumberOfAuctions = 0;
	private int totalNumberOfSuccessfulAuctions = 0;
	private double totalSuccessfulAuctionRatio = 0;
	private long totalAuctionTime = 0;
	private long totalAuctionTimeAvg = 0;
	
	private long serverStartingTime = 0;
	private long serverCurrentTime = 0;
	private long serverTimePassed = 0;
	private int serverMinutesPassed = 0;
	
	private static int newSubscriptionID = 0;
	
	private boolean status = true;
		
	public AnalyticsServerImpl() {
		Date date = new Date();
		serverStartingTime = date.getTime();
	}
	
	@Override
	public String subscribe(String filter, Notify notify) {
		newSubscriptionID++;
		String ID = Integer.toString(newSubscriptionID);
		Subscription newSubscription = new Subscription(ID, filter, notify);
		subscriptions.put(ID, newSubscription);
		return ID;
	}

	@Override
	public void processEvent(Event event) {
		
		//AuctionEvent
		if(event instanceof AuctionEvent) {
			String filter = "(AUCTION_.*)";
			if(event.getType().equals("AUCTION_STARTED")) {
				sendThroughFilter(event, filter);
				startAuction((AuctionEvent) event);
			}
			else if(event.getType().equals("AUCTION_ENDED")) {
				sendThroughFilter(event, filter);
				endAuction((AuctionEvent) event);
				auctionSuccessRatio();
			}
		}
		
		//BidEvent
		else if(event instanceof BidEvent) {
			String filter = "(BID_.*)";
			if(event.getType().equals("BID_PLACED")) {
				sendThroughFilter(event, filter);
				bidPlaced((BidEvent) event);
			}
			else if(event.getType().equals("BID_OVERBID")) {
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("BID_WON")) {
				bidWon((BidEvent) event);
				sendThroughFilter(event, filter);
			}
		}
		
		//StatisticsEvent
		else if(event instanceof StatisticsEvent) {
			String filter = "(STATISTICS_.*)";
			if(event.getType().equals("USER_SESSIONTIME_MIN")) {
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("USER_SESSIONTIME_MAX")) {
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("USER_SESSIONTIME_AVG")) {
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("BID_PRICE_MAX")) {
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("BID_COUNT_PER_MINUTE")) {
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("AUCTION_TIME_AVG")) {
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("AUCTION_SUCCESS_RATIO")) {
				sendThroughFilter(event, filter);
			}
		}
		
		//UserEvent
		else if(event instanceof UserEvent) {
			String filter = "(USER_.*)";
			if(event.getType().equals("USER_LOGIN")) {
				loginUser((UserEvent) event);
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("USER_LOGOUT")) {
				logoutUser((UserEvent) event);
				sendThroughFilter(event, filter);
			}
			else if(event.getType().equals("USER_DISCONNECTED")) {
				logoutUser((UserEvent) event);
				sendThroughFilter(event, filter);
			}
		}
	}

	private void bidPlaced(BidEvent event) {
		totalAmountOfBids++;
		if(event.getPrice() > totalMaxBidPrice) {
			totalMaxBidPrice = event.getPrice();
			processEvent(new StatisticsEvent("BID_PRICE_MAX", totalMaxBidPrice));
		}
		Date date = new Date();
		serverCurrentTime = date.getTime();
		serverTimePassed = serverCurrentTime - serverStartingTime;
		serverMinutesPassed = (int) (serverTimePassed/60000);
		if(serverMinutesPassed == 0) {
			totalBidsPerMinute = (double) totalAmountOfBids;
		}else {
			totalBidsPerMinute = (double) totalAmountOfBids/ (double) serverMinutesPassed;
		}
		processEvent(new StatisticsEvent("BID_COUNT_PER_MINUTE", totalBidsPerMinute));
	}

	private void auctionSuccessRatio() {
		if(totalNumberOfSuccessfulAuctions == 0) {
			processEvent(new StatisticsEvent("AUCTION_SUCCESS_RATIO", 0));
		} else {
			totalSuccessfulAuctionRatio = (double) totalNumberOfSuccessfulAuctions/ (double) totalNumberOfAuctions;
			processEvent(new StatisticsEvent("AUCTION_SUCCESS_RATIO", totalSuccessfulAuctionRatio));
		}
	}

	private void bidWon(BidEvent event) {
		totalNumberOfSuccessfulAuctions++;
	}

	@Override
	public void unsubscribe(String identifier) {
		subscriptions.remove(identifier);
	}
	
	public void checkIfMinSessionTime(long time) {
		if(time < totalMinSessionTime || totalMinSessionTime == 0) {
			totalMinSessionTime = time;
			processEvent(new StatisticsEvent("USER_SESSIONTIME_MIN", time));
		}
	}
	
	public void checkIfMaxSessionTime(long time) {
		if(time > totalMaxSessionTime) {
			totalMaxSessionTime = time;
			processEvent(new StatisticsEvent("USER_SESSIONTIME_MAX", time));
		}
	}
	
	public void updateAvgSessionTime(long time) {
		totalSessionTime += time;
		totalSessionAmount ++;
		totalAvgSessionTime = (totalSessionTime / totalSessionAmount);
		processEvent(new StatisticsEvent("USER_SESSIONTIME_AVG", totalAvgSessionTime));
	}
	
	public void sendThroughFilter(Event event, String filter) {		
		Iterator<?> iter = subscriptions.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<?,?> pair = (Entry<?, ?>) iter.next();
			Subscription currentSubscription = (Subscription) pair.getValue();
			String[] currentFilters = currentSubscription.getFilters();
			for(int i = 0; i < currentFilters.length; i++) {
				Pattern pattern = Pattern.compile(currentFilters[i]);
				Matcher matcher = pattern.matcher(event.getType());
				if(matcher.find()) {
					currentSubscription.notify(event);
				}
			}
		}
	}
	
	public void loginUser(UserEvent event) {
		if(!userStats.containsKey(event.getUserName())) {
			AuctionUser newUser = new AuctionUser(event.getUserName(),this);
			userStats.put(event.getUserName(), newUser);
			newUser.login();
		}
		else {
			AuctionUser knownUser = userStats.get(event.getUserName());
			knownUser.login();
		}
	}
	
	public void logoutUser(UserEvent event) {
		if(userStats.containsKey(event.getUserName())) {
			AuctionUser knownUser = userStats.get(event.getUserName());
			knownUser.logout();
		}
	}
	
	public void startAuction(AuctionEvent event) {
		AuctionStats newAuction = new AuctionStats(event.getAuctionID());
		auctionStats.put(event.getAuctionID(), newAuction);
		totalNumberOfAuctions++;
		newAuction.startAuction(event.getTimeStamp());
	}
	
	public void endAuction(AuctionEvent event) {
		AuctionStats knownAuction = auctionStats.get(event.getAuctionID());
		knownAuction.endAuction(event.getTimeStamp());
		long totalTime = knownAuction.getTotalTime();
		totalAuctionTime += totalTime;
		totalAuctionTimeAvg = totalAuctionTime/totalNumberOfAuctions;
		processEvent(new StatisticsEvent("AUCTION_TIME_AVG", totalAuctionTimeAvg));
	}
	
	public boolean getStatus() {
		return status;
	}


}
