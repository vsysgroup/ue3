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

	public enum KeyOwner {SERVER, ALICE}

	private String pathToServerPublicKey = null;
	private String pathToKeyDirectory;

	public KeyReader(String pathToKeyDirectory) {
		this.pathToKeyDirectory = pathToKeyDirectory;
	}
	
	public KeyReader(String pathToServerPublicKey, String pathToKeyDirectory) {
		this.pathToServerPublicKey = pathToServerPublicKey;
		this.pathToKeyDirectory = pathToKeyDirectory;
	}
	
	public Key getPublicKey(KeyOwner owner) throws IOException {
		String pathToPublicKey = "";
		switch(owner) {
			case ALICE: pathToPublicKey = "keys/" + "alice.pub.pem";
				break;
			case SERVER: pathToPublicKey = pathToServerPublicKey;
				break;
		}

		PEMReader in = new PEMReader(new FileReader(pathToPublicKey));
		PublicKey publicKey = (PublicKey) in.readObject(); 
		return publicKey;
	}

	public Key getPrivateKey(KeyOwner owner) throws IOException {
		String pathToPrivateKey = "";
		switch(owner) {
			case ALICE: pathToPrivateKey = pathToKeyDirectory + "alice.pem";
				break;
			case SERVER: pathToPrivateKey = pathToKeyDirectory + "auction-server.pem";
				break;
		}
		
		PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
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
