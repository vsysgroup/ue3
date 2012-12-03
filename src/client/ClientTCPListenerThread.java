package client;

import java.io.IOException;
import java.net.Socket;

import communication.TCPCommunication;

public class ClientTCPListenerThread extends Thread {

	private TCPCommunication tcpCommunication;
	private Client client;
	
	public ClientTCPListenerThread(TCPCommunication tcpCommunication, Client client) {
		this.tcpCommunication = tcpCommunication;
		this.client = client;
	}
	
	public void run() {
		while(client.getClientStatus() && !interrupted()) {
			try {
				String receivedMessage = tcpCommunication.receive();
				if(receivedMessage == null) {
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
