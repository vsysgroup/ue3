package billingServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * reads the hashes of all admin users (mgmt client)
 * @author Barbara Schwankl 0852176
 *
 */
public class UserPropertyReader {
	
	public static UserPropertyReader instance = null;
	private Map<String, String> permittedUser = new HashMap<String, String>();

	public static synchronized UserPropertyReader getInstance() {
		if(instance == null) {
			instance = new UserPropertyReader(); 
		}
		return instance;
	}
	
	private UserPropertyReader() {
		InputStream is = ClassLoader.getSystemResourceAsStream("user.properties");
		if (is != null) {
			Properties props = new Properties();
			try {
				props.load(is);
				Set<String> user = props.stringPropertyNames();
				Iterator<String> it = user.iterator();
				while(it.hasNext()) {
					String key = it.next();
					String pw = props.getProperty(key);
					permittedUser.put(key, pw);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.err.println("Properties file not found!");
		}
	}

	public Map<String, String> getPermittedUser() {
		return permittedUser;
	}
}
