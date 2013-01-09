package communication;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;

public class RSAChannel extends DecoratorChannel {

	public static final Logger LOG = Logger.getLogger(RSAChannel.class);

	private Cipher cipherEncrypt = null;
	private Cipher cipherDecrypt = null;

	public RSAChannel(Channel channel) {
		super(channel);		
	}

	public void setDecryptKey(Key decryptKey) {
		try {
			this.cipherDecrypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");

			// MODE is the encryption/decryption mode
			// KEY is either a private, public or secret key
			// IV is an init vector, needed for AES
			this.cipherDecrypt.init(Cipher.DECRYPT_MODE, decryptKey);			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setDecryptKeyAES(Key secretKey, AlgorithmParameterSpec iv) {
		try {
			this.cipherDecrypt = Cipher.getInstance("AES/CTR/NoPadding");

			// MODE is the encryption/decryption mode
			// KEY is either a private, public or secret key
			// IV is an init vector, needed for AES
			this.cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKey, iv);			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setEncryptKey(Key encryptKey) {
		try {
			this.cipherEncrypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			// MODE is the encryption/decryption mode
			// KEY is either a private, public or secret key
			// IV is an init vector, needed for AES
			this.cipherEncrypt.init(Cipher.ENCRYPT_MODE, encryptKey);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setEncryptKeyAES(Key secretKey, AlgorithmParameterSpec iv) {
		try {
			this.cipherEncrypt = Cipher.getInstance("AES/CTR/NoPadding");
			// MODE is the encryption/decryption mode
			// KEY is either a private, public or secret key
			// IV is an init vector, needed for AES
			this.cipherEncrypt.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void send(byte[] out) {
		decoratedChannel.send(encrypt(out));
	}

	@Override
	public byte[] receive() throws IOException {
		return decrypt(decoratedChannel.receive());
	}

	private byte[] encrypt(byte[] msg) {	
		if (cipherEncrypt == null) {
			return msg;
		}
		byte[] encryptedMsg = null;
		try {			
			encryptedMsg = cipherEncrypt.doFinal(msg);
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encryptedMsg;
	}

	private byte[] decrypt(byte[] msg) {	
		if (cipherDecrypt == null) {
			return msg;
		}
		byte[] decryptedMsg = null;
		try {			
			decryptedMsg = cipherDecrypt.doFinal(msg);
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decryptedMsg;
	}

	@Override
	public Channel getDecoratedChannel() {
		return decoratedChannel;
	}

}
