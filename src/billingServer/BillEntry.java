package billingServer;

import java.io.Serializable;
import java.text.DecimalFormat;


/**
 * contains components of a bill
 * @author Barbara Schwankl 0852176
 *
 */
public class BillEntry implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -231708186719453986L;
	
	
	private long auctionID;
	private double price;
	private double feeFixed;
	private double feeVariable;
	private double feeTotal;

	public BillEntry(long auctionID, double price) {
		this.auctionID = auctionID;
		this.price = price;
	}

	public long getAuctionID() {
		return auctionID;
	}

	public void setAuctionID(long auctionID) {
		this.auctionID = auctionID;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getFeeFixed() {
		return feeFixed;
	}

	public void setFeeFixed(double feeFixed) {
		this.feeFixed = feeFixed;
	}

	public double getFeeVariable() {
		return feeVariable;
	}

	public void setFeeVariable(double feeVariable) {
		this.feeVariable = feeVariable;
	}

	public double getFeeTotal() {
		return feeTotal;
	}

	public void setFeeTotal(double feeTotal) {
		this.feeTotal = feeTotal;
	}

	public void compute(PriceSteps priceSteps) {
		boolean found = false;
		for (Step step : priceSteps.getSteps()) {
			if (price >= step.getStartPrice() && price < step.getEndPrice()) {
				feeFixed = step.getFixedPrice();
				feeVariable = price * step.getVariablePricePercent() / 100.0;
				feeTotal = feeFixed + feeVariable;
				found = true;
				break;
			}
		}
		if (!found) {
			feeFixed = feeVariable = feeTotal = 0.0;
		}
		
	}
	
	public String toString() {
		DecimalFormat customFormat = new DecimalFormat("#.##");		
		return 	auctionID + "\t" + "\t" +
			customFormat.format(price) + "\t" + "\t" +
			customFormat.format(feeFixed) + "\t" + "\t" +
			customFormat.format(feeVariable) + "\t" + "\t" +
			customFormat.format(feeTotal);
	}

}
