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

	private String pathToKeyDirectory;

	public KeyReader(String pathToKeyDirectory) {
		this.pathToKeyDirectory = pathToKeyDirectory;
	}
	
	public Key getPublicKeyServer(String pathToServerPublicKey) throws IOException {
		PEMReader in = new PEMReader(new FileReader(pathToServerPublicKey));
		PublicKey publicKey = (PublicKey) in.readObject(); 
		return publicKey;
	}
	
	public Key getPublicKeyClient(String username) throws IOException {
		String path = pathToKeyDirectory + username + ".pub.pem";
		PEMReader in = new PEMReader(new FileReader(path));
		PublicKey publicKey = (PublicKey) in.readObject(); 
		return publicKey;
	}
	
	public Key getPrivateKeyServer(String pathToPrivateKey) throws IOException {
		
		PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
			//passphrase for server key: 23456
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

		KeyPair keypair =  (KeyPair) in.readObject();;
		PrivateKey privateKey = keypair.getPrivate();
		return privateKey;
	}

	public Key getPrivateKeyClient(String username) throws IOException {
		String path = pathToKeyDirectory + username + ".pem";
		
		PEMReader in = new PEMReader(new FileReader(path), new PasswordFinder() {
			//passphrase for all private keys: 12345
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
