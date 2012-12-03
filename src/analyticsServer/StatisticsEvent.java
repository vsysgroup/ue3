package analyticsServer;

/**
 * This class represents a StatisticsEvent.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class StatisticsEvent extends Event {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double value = 0;

	public StatisticsEvent(String type, double value) {
		super(type);
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
}
