package security;

import org.bouncycastle.util.encoders.Base64;

/**
 * encodes and decodes base64 with strings
 * @author Barbara Schwankl 0852176
 *
 */
public class MyBase64 {

	public static String encode(byte[] msg) {
		byte[] encodedMsg = Base64.encode(msg);
		return new String(encodedMsg);
	}
	
	public static byte[] decode(String msg) {
		return Base64.decode(msg);
	}
}
