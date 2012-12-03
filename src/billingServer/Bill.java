package billingServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * contains all billing lines for a user
 * @author Barbara Schwankl 0852176
 *
 */
public class Bill implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5840430563803196422L;
	
	private String user;
	private List<BillEntry> allBills = new ArrayList<BillEntry>();

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public void addBillEntry(long auctionID, double price) {
		allBills.add(new BillEntry(auctionID, price));
	}

	public void compute(PriceSteps priceSteps) {
		for(BillEntry entry : allBills) {
			entry.compute(priceSteps);
		}
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("auction_ID" + "\t" + "strike_price" + "\t" + "fee_fixed" + "\t" + "fee_variable" + "\t" + "fee_total\n");
		for(BillEntry entry: allBills) {
			buffer.append(entry.toString() + "\n");
		}
		return buffer.toString();
	}
}
