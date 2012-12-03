package server;

import java.io.IOException;
import java.net.Socket;

import communication.TCPCommunication;

/**
 * This thread is established once contact with a Client has been made. It handles communications
 * with a specific client.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class ServerTCPCommunicationThread extends Thread {
	
	private Socket clientSocket = null;
	private Server server;
	private TCPCommunication tcpCommunication = null;
	
	public ServerTCPCommunicationThread(Socket socket, Server server) {
		this.clientSocket = socket;
		this.server = server;
		
	}
	
	public void run() {
		try {
			this.tcpCommunication = new TCPCommunication(clientSocket);
		} catch(IOException e) {
			exit();
			return;
		}
		while(!interrupted() && clientSocket != null){
			try {
				String receivedMessage = tcpCommunication.receive();
				if(receivedMessage == null) {
					exit();
					break;
				}
				server.receiveMessage(receivedMessage, clientSocket);
			} catch (IOException e) {
				exit();
			}
		}
		//TODO perhaps send disconnect event from here
	}
	

	public void exit() {
		interrupt();
		try {
			clientSocket.close();
		} catch (Exception e) {}
	}

}
