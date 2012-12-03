package billingServer;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * represents a single price step
 * @author Barbara Schwankl 0852176
 *
 */
public class Step implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1546868358861038337L;
	
	private double startPrice, endPrice, fixedPrice, variablePricePercent;
	
	public Step(double startPrice, double endPrice, double fixedPrice, double variablePricePercent) {
		this.startPrice = startPrice;
		this.endPrice = endPrice;
		this.fixedPrice = fixedPrice;
		this.variablePricePercent = variablePricePercent;
	}

	public double getStartPrice() {
		return startPrice;
	}

	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}

	public double getEndPrice() {
		return endPrice;
	}

	public void setEndPrice(double endPrice) {
		this.endPrice = endPrice;
	}

	public double getFixedPrice() {
		return fixedPrice;
	}

	public void setFixedPrice(double fixedPrice) {
		this.fixedPrice = fixedPrice;
	}

	public double getVariablePricePercent() {
		return variablePricePercent;
	}

	public void setVariablePricePercent(double variablePricePercent) {
		this.variablePricePercent = variablePricePercent;
	}

	@Override
	public String toString() {
		String strEndPrice = new String();
		DecimalFormat customFormat = new DecimalFormat("#.#");	
		if (Double.isInfinite(endPrice)) {
			strEndPrice = "INFINITY" + "\t";
		} else {
			strEndPrice = customFormat.format(endPrice) + "\t" + "\t";
		}
		return 	customFormat.format(startPrice) + "\t" + "\t" +
			strEndPrice +
			customFormat.format(fixedPrice) + "\t" + "\t" +
			customFormat.format(variablePricePercent) + "%";		
	}
	
}
