package communication;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;

import security.KeyReader;

public class RSAChannel extends DecoratorChannel {

	public static final Logger LOG = Logger.getLogger(RSAChannel.class);
	private Key key;
	
	public RSAChannel(Channel channel, Key key) {
		super(channel);
		this.key = key;
	}

	@Override
	public void send(byte[] out) {
		decoratedChannel.send(encrypt(out));
	}

	@Override
	public byte[] receive() throws IOException {
		decoratedChannel.receive();
		return null;
	}

	private byte[] encrypt(byte[] msg) {
		Cipher crypt;
		byte[] encryptedMsg = null;
		try {
			crypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			// MODE is the encryption/decryption mode
			// KEY is either a private, public or secret key
			// IV is an init vector, needed for AES
			crypt.init(Cipher.ENCRYPT_MODE, key);
			encryptedMsg = crypt.doFinal(msg.getBytes());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encryptedMsg;
	}
}
