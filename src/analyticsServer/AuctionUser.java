package analyticsServer;

/**
 * This class saves statistics for specific Users.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class AuctionUser {
	
	private AnalyticsServerImpl analyticsServer;
	private String username;
	private long minSessionTime = 0;
	private long maxSessionTime = 0;
	private long avgSessionTime = 0;
	private boolean loggedIn = false;
	private int numberOfSessions = 0;
	private long sessionTimeSum = 0;
	private long sessionStart = 0;
	private long sessionEnd = 0;
	private long lastSessionLength = 0;
	
	public AuctionUser(String username, AnalyticsServerImpl analyticsServer) {
		this.username = username;
		this.analyticsServer = analyticsServer;
	}
	
	public String getUserName() {
		return username;
	}
	
	public void setUsername(String name) {
		this.username = name;
	}
	
	public void setSessionTimes(long time){
		if(time < minSessionTime || minSessionTime == 0) {
			minSessionTime = time;
			analyticsServer.checkIfMinSessionTime(minSessionTime);
		}
		if(time > maxSessionTime) {
			maxSessionTime = time;
			analyticsServer.checkIfMaxSessionTime(maxSessionTime);
		}
		sessionTimeSum += time;
		numberOfSessions += 1;
		avgSessionTime = (sessionTimeSum/numberOfSessions);
		analyticsServer.updateAvgSessionTime(avgSessionTime);
	}
	
	public long getMinSessionTime() {
		return minSessionTime;
	}
	
	public long getMaxSessionTime() {
		return maxSessionTime;
	}
	
	public long getAvgSessionTime() {
		return avgSessionTime;
	}
	
	public void login() {
		loggedIn = true;
		startSession();
	}
	
	public void logout() {
		loggedIn = false;
		endSession();
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public void startSession() {
		this.sessionStart = System.currentTimeMillis();
	}
	
	public void endSession() {
		this.sessionEnd = System.currentTimeMillis();
		lastSessionLength = sessionEnd - sessionStart;
		setSessionTimes(lastSessionLength);
	}

}
