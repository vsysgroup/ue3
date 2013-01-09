package security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * generates secure numbers and encodes them in base64
 * @author Babz
 *
 */
public class MyRandomGenerator {

	/*
	 *  generates a 32 byte secure random number => challenge
	 */
	public static String createChallenge() {
		SecureRandom secureRandom = new SecureRandom();
		final byte[] number = new byte[32];
		secureRandom.nextBytes(number);
		// encoding challenge separately in base64
		return MyBase64.encode(number);
	}

	/*
	 *  generates a 16 byte initialization vector
	 */
	public static String createIV() {
		SecureRandom secureRandom = new SecureRandom();
		final byte[] number = new byte[16];
		secureRandom.nextBytes(number);
		// encoding iv separately in base64
		return MyBase64.encode(number);
	}

	public static String createSecretKey() throws NoSuchAlgorithmException {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		// KEYSIZE is in bits
		generator.init(256);
		SecretKey key = generator.generateKey();
		String s = new String(MyBase64.encode(key.getEncoded()));
		return s;
	}
	
}
