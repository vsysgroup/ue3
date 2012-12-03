package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * This thread listens for incoming UDP messages and starts a ClientUDPCommunication thread for every message it receives.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class ClientUDPListenerThread extends Thread {

	private DatagramSocket datagramSocket = null;
	private DatagramPacket datagramPacket = null;
	private Client client = null;
	
	public ClientUDPListenerThread(DatagramSocket datagramSocket, Client client) {
		this.datagramSocket = datagramSocket;
		this.client = client;
	}
	
	public void run() {
		
		while(client.getClientStatus() && !interrupted()) {
			byte[] carrier = new byte[1000];
			datagramPacket = new DatagramPacket(carrier, 1000);
			try {
				datagramSocket.receive(datagramPacket);
				new ClientUDPCommunicationThread(datagramPacket, client).start();
			} catch(IOException e) {
				exit();
				return;
			}
		}
	}
	
	/**
	 * stops the thread
	 */
	public void exit(){
		if(!client.getClientStatus()) {
			interrupt();
		}
	}
	

}
