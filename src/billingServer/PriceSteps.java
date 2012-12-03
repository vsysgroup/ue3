package billingServer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * represents configuration of price steps
 * @author Barbara Schwankl 0852176
 *
 */
public class PriceSteps implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static PriceSteps instance = null;
	private Set<Step> allPriceSteps = Collections.synchronizedSet(new HashSet<Step>());

	public static synchronized PriceSteps getInstance() {
		if(instance == null) {
			instance = new PriceSteps();
		}
		return instance;
	}

	public Set<Step> getSteps() { 
		return allPriceSteps; 
	}

	private PriceSteps() {}

	/**
	 * creates a new price step
	 * @param startPrice
	 * @param endPrice
	 * @param fixedPrice
	 * @param variablePricePercent
	 * @return false if step collides with existing step
	 */
	public boolean createStep(double startPrice, double endPrice, double fixedPrice, double variablePricePercent) {
		boolean collision = false;
		for(Step s: allPriceSteps) {
			if( ( (startPrice <= s.getStartPrice()) && (endPrice > s.getStartPrice()) ) || ( (startPrice < s.getEndPrice()) && (endPrice >= s.getEndPrice()) ) ) {
				collision = true;
				break;
			}
		}
		if(collision) {
			return false;
		} else {
			Step newStep = new Step(startPrice, endPrice, fixedPrice, variablePricePercent);
			allPriceSteps.add(newStep);
			return true;
		}
	}

	/**
	 * deletes a price step
	 * @param startPrice
	 * @param endPrice
	 * @return false if step does not exist
	 */
	public synchronized boolean deleteStep(double startPrice, double endPrice) {
		boolean stepExists = false;
		for(Step s: allPriceSteps) {
			if((s.getStartPrice() == startPrice) && (s.getEndPrice() == endPrice)) {
				stepExists = true;
				allPriceSteps.remove(s);
				break;
			}
		}
		return stepExists;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Min_Price	Max_Price	Fee_Fixed	Fee_Variable" + "\n");
		Iterator<Step> it = allPriceSteps.iterator();
		while(it.hasNext()) {
			buffer.append(it.next().toString() + "\n");
		}
		return buffer.toString();
	}

}
