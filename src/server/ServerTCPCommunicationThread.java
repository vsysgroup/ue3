package server;

import java.io.IOException;
import java.net.Socket;

import security.KeyReader;
import security.KeyReader.KeyOwner;

import communication.Base64Channel;
import communication.Channel;
import communication.RSAChannel;
import communication.TCPChannel;

/**
 * This thread is established once contact with a Client has been made. It handles communications
 * with a specific client.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class ServerTCPCommunicationThread extends Thread {
	
	private Socket clientSocket = null;
	private Server server;
	private Channel channel = null;
	
	public ServerTCPCommunicationThread(Socket socket, Server server) {
		this.clientSocket = socket;
		this.server = server;
		
	}
	
	public void run() {
		try {
			this.channel = new RSAChannel(new Base64Channel(new TCPChannel(clientSocket)), KeyReader.getPublicKey(KeyOwner.ALICE));
		} catch(IOException e) {
			exit();
			return;
		}
		while(!interrupted() && clientSocket != null){
			try {
				byte[] tmp = channel.receive();
				String receivedMessage = new String(tmp);
				if(receivedMessage == "") {
					exit();
					break;
				}
				server.receiveMessage(receivedMessage, channel);
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
