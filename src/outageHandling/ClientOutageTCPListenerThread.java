package outageHandling;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;

import client.Client;


public class ClientOutageTCPListenerThread extends Thread{
	
	private ServerSocket serverSocket = null;
	private Client client;
	private Key clientPrivateKey;
	private ArrayList<Socket> otherClientSockets = new ArrayList<Socket>();
	private OutageHandler outageHandler;
	
	public ClientOutageTCPListenerThread(ServerSocket serverSocket, Client client, Key clientPrivateKey, OutageHandler outageHandler) {
		this.serverSocket = serverSocket;
		this.client = client;
		this.clientPrivateKey = clientPrivateKey;
		this.outageHandler = outageHandler;
	}
	
	public void run() {
		Socket socket = null;
		while(!interrupted() && client.getClientStatus()) {
			try {
				socket = serverSocket.accept();
				new ClientOutageTCPCommunicationThread(socket, client, clientPrivateKey, outageHandler).start();
				otherClientSockets.add(socket);
			} catch(IOException e) {
				exit();
			}
		
		}
	}
	
	public void exit() {
		if(!client.getClientStatus()) {
			interrupt();
			try {
				Iterator<Socket> iter = this.otherClientSockets.iterator();
				while(iter.hasNext()) {
					Socket socket = iter.next();
					socket.close();
				}
			} catch (IOException e) {}
		}
		return;
	}

}
