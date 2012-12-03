package analyticsServer;

import java.io.Serializable;
import java.util.Date;

/**
 * This abstract class represents and Event. Other event subtypes inherit from it.
 * @author Philipp Pfeiffer 0809357
 *
 */
public abstract class Event implements Serializable {

	private static int newID = 0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String ID = "";
	public String type = "";
	public long timeStamp = 0;
	
	public Event(String type) {
		newID++;
		this.ID = Integer.toString(newID);
		this.type = type;
		Date date = new Date();
		this.timeStamp = date.getTime();
	}
	
	public String getID() {
		return ID;
	}
	
	public String getType() {
		return type;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
}
