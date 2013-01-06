package client;

import java.io.IOException;

import communication.Channel;

/**
 * 
 * @author Babz
 *
 */
public class ClientTCPListenerThread extends Thread {

	private Channel channel;
	private Client client;
	
	public ClientTCPListenerThread(Channel channel, Client client) {
		this.channel = channel;
		this.client = client;
	}
	
	public void run() {
		while(client.getClientStatus() && !interrupted()) {
			try {
				String receivedMessage = new String(channel.receive());
				if(receivedMessage == "") {
					exit();
					break;
				}
				
				client.receiveResponse(receivedMessage);
			} catch (IOException e) {
				System.out.println("Connection to Server lost");
				exit();
			}
		}
	}
	
	public void exit() {
		interrupt();
		client.exitClient();
	}
}
