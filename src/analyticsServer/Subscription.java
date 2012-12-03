package analyticsServer;

import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * This class represents a subscription to the notification service of the AnalyticsServer. It handles the callback object and
 * filters of the subscription.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class Subscription {

	private String ID;
	private String filter;
	private Notify notify;
	private int numberOfFilters;
	private String [] filters;
	
	public Subscription(String ID, String filter, Notify notify) {
		this.ID = ID;
		this.filter = filter;
		this.notify = notify;
		String cleanFilter =filter.replaceAll("'", "");
		cleanFilter = cleanFilter.replaceAll("\\(", "");
		cleanFilter = cleanFilter.replaceAll("\\)", "");
		String[] filters = cleanFilter.split("\\|");
		this.filters = filters;
		this.numberOfFilters = filters.length;
	}
	
	public void notify(Event event) {
		try {
			notify.notify(event);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public String getID() {
		return ID;
	}
	
	public String getFilter() {
		return filter;
	}
	
	public int getNumberOfFilters() {
		return numberOfFilters;
	}
	
	public String[] getFilters() {
		return filters;
	}
	
	public boolean containsFilter(String filter) {
		if(Arrays.asList(filters).contains(filter)) {
			return true;
		} else {
			return false;
		}
	}
}
