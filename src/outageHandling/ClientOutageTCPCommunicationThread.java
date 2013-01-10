package outageHandling;

import java.io.IOException;
import java.net.Socket;
import java.security.Key;

import communication.Base64Channel;
import communication.Channel;
import communication.RSAChannel;
import communication.TCPChannel;
import communication.TCPCommunication;

import client.Client;

public class ClientOutageTCPCommunicationThread extends Thread {
	
	private Socket socket;
	private Client client;
	private Key clientPrivateKey;
	private OutageHandler outageHandler;
	private TCPCommunication tcpCommunication;
	
	public ClientOutageTCPCommunicationThread(Socket socket, Client client, Key clientPrivateKey, OutageHandler outageHandler) {
		this.socket = socket;
		this.client = client;
		this.clientPrivateKey = clientPrivateKey;
		this.outageHandler = outageHandler;
	}
	
	public void run() {
		try {
			this.tcpCommunication = new TCPCommunication(socket);
		} catch(IOException e) {
			exit();
			return;
		}
		while(!interrupted() && socket != null){
			System.out.println("entered while loop");
			try {
				String receivedMessage = tcpCommunication.receive();
				outageHandler.receiveMessage(receivedMessage, socket);
			} catch (IOException e) {
				exit();
			}
		}
	}
	public void exit() {
		interrupt();
		try {
			socket.close();
		} catch (Exception e) {}
	}

}
