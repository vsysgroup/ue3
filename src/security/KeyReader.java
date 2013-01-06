package security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class KeyReader {

	public static Key getPublicKey() throws IOException {
		//public key
		String pathToPublicKey = "auction-server.pub.pem";
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey));
		PublicKey publicKey = (PublicKey) in.readObject(); 
		return publicKey;
	}

	public Key getPrivateKey() throws IOException {
		String pathToPrivateKey = "auction-server.pem";
		PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {

			@Override
			public char[] getPassword() {
				char[] password = null;
				// reads the password from standard input for decrypting the private key
				System.out.println("Enter pass phrase:");
				try {
					password = new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return password;
			} 

		});

		KeyPair keyPair = (KeyPair) in.readObject();
		PrivateKey privateKey = keyPair.getPrivate();
		return privateKey;
	}
}
