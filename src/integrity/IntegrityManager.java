package integrity;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

/**
 * This class represents the Integrity Manager. It is used to read Secret Keys from the directory, to create hashed MACs and to verify them.
 * @author 0809357 Philipp Pfeiffer
 *
 */
public class IntegrityManager {

	private String pathToKeyDirectory;
	
	public IntegrityManager(String pathToKeyDirectory) {
		this.pathToKeyDirectory = pathToKeyDirectory;
	}
	
	/**
	 * This method reads the Secret key of the specific user from the key directory.
	 * @param username
	 * @return Key
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	public Key getSecretKey(String username) throws IOException {
		String pathToSecretKey = pathToKeyDirectory + "/" + username + ".key";
		byte[] keyBytes = new byte[1024];
		FileInputStream fis = new FileInputStream(pathToSecretKey);
		fis.read(keyBytes);
		fis.close();
		byte[] input = Hex.decode(keyBytes);
		
		Key key = new SecretKeySpec(input,"HmacSHA256");
		
		return key;
	}
	
	/**
	 * This method receives a secret key and a message. It creates a hashed MAC out of the two.
	 * @param secretKey
	 * @param message
	 * @return byte[]
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public byte[] createHashMAC(Key secretKey, String msg) throws NoSuchAlgorithmException, InvalidKeyException {
		
		byte[] message = msg.getBytes();
		
		Mac hMac = Mac.getInstance("HmacSHA256"); 
		hMac.init(secretKey);
		hMac.update(message);
		byte[] hash = hMac.doFinal();
		
		return hash;
	}
	
	/**
	 * This method is used to verify whether two hashMACs are in fact equal
	 * @param computedHash
	 * @param receivedHash
	 * @return boolean
	 */
	public boolean verifyHashMAC(byte[] computedHash, byte[] receivedHash) {
		return MessageDigest.isEqual(computedHash,receivedHash);	
	}
}
