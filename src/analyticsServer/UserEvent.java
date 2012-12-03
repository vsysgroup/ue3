package analyticsServer;

/**
 * This class represents a UserEvent
 * @author Philipp Pfeiffer 0809357
 *
 */
public class UserEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String userName = "";

	public UserEvent(String type, String userName) {
		super(type);
		this.userName = userName;
	}
	
	public String getUserName() {
		return userName;
	}
}
