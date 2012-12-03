package client;

import java.net.DatagramPacket;

/**
 * This thread is started by ClientUDPListenerThread whenever a UDP message comes in. It forwards the message to the
 * Client for further processing and then stops.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class ClientUDPCommunicationThread extends Thread {
	private DatagramPacket datagramPacket = null;
	private Client client = null;
	
	public ClientUDPCommunicationThread(DatagramPacket datagramPacket, Client client) {
		this.datagramPacket = datagramPacket;
		this.client = client;
	}
	
	public void run() {
		if(client.getClientStatus()) {
			String message = new String(datagramPacket.getData()).trim();
			client.receiveResponse(message);
		}
		
	}

}
