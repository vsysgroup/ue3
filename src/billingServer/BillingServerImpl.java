package billingServer;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Implementation of Interface Billing Server
 * @author Barbara Schwankl 0852176
 *
 */
public class BillingServerImpl implements IBillingServer, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public IBillingServerSecure login(String username, String password) throws RemoteException {

		if(!checkAuthentification(username, password)) {
			return null;
		}

		//export remote
		BillingServerSecureImpl billingServerAccess = BillingServerSecureImpl.getInstance(); 
		
		return billingServerAccess;
	}

	private boolean checkAuthentification(String name, String pw) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("no such algorithm");
		}

		byte[] pwHash = md.digest(pw.getBytes()); 
		String pwHex = byteArrayToHex(pwHash);
		
		Map<String, String> user = UserPropertyReader.getInstance().getPermittedUser();

		for(Entry <String, String> entry: user.entrySet()) {
			if(entry.getKey().equals(name)) {
				if(entry.getValue().equals(pwHex)) { 
					return true;
				}
			}
		}
		return false;
	}
	
	private String byteArrayToHex(byte[] digest) {
		StringBuilder sb = new StringBuilder();
		for(byte b: digest) {
			sb.append(String.format("%02x", b&0xff));
		}
		return sb.toString();
	}

	//create hash for static user passwords; never used by program
	@SuppressWarnings("unused")
	private void getPasswordHash() {
		String password = "alice123";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] pwHash = md.digest(password.getBytes()); 
			String pwHex = byteArrayToHex(pwHash);
			System.out.println(pwHex);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
