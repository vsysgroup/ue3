package outageHandling;

import java.io.IOException;
import java.net.Socket;
import java.security.Key;

import communication.Base64Channel;
import communication.Channel;
import communication.RSAChannel;
import communication.TCPChannel;

import client.Client;

public class ClientOutageTCPCommunicationThread extends Thread {
	
	private Socket socket;
	private Client client;
	private Key clientPrivateKey;
	private OutageHandler outageHandler;
	private Channel channel = null;
	
	public ClientOutageTCPCommunicationThread(Socket socket, Client client, Key clientPrivateKey, OutageHandler outageHandler) {
		this.socket = socket;
		this.client = client;
		this.clientPrivateKey = clientPrivateKey;
		this.outageHandler = outageHandler;
	}
	
	public void run() {
		try {
			this.channel = new RSAChannel(new Base64Channel(new TCPChannel(socket)));
			((RSAChannel) this.channel).setDecryptKey(clientPrivateKey);
		} catch(IOException e) {
			exit();
			return;
		}
		while(!interrupted() && socket != null){
			try {
				byte[] tmp = channel.receive();
				String receivedMessage = new String(tmp);
				if(receivedMessage == "") {
					exit();
					break;
				}
				outageHandler.receiveMessage(receivedMessage, channel);
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
