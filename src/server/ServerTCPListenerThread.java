package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This thread listens for incoming connections, creates sockets for each new connection and assigns a serverTCPCommunicationThread
 * to every client.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class ServerTCPListenerThread extends Thread {
	private ServerSocket serverSocket = null;
	private Server server = null;
	private ArrayList<Socket> sockets = new ArrayList<Socket>();
	
	public ServerTCPListenerThread(ServerSocket serverSocket, Server server) {
		this.serverSocket = serverSocket;
		this.server = server;
	}
	
	public void run() {
		Socket socket = null;
		
		while(!interrupted() && server.getServerStatus()) {
			//establish connection by accept
			try {
				socket = serverSocket.accept();
				new ServerTCPCommunicationThread(socket, server).start();
				sockets.add(socket);
			} catch (IOException e) {
				exit();
			}
		}
	}
	
	public void exit() {
		if(!server.getServerStatus()) {
			interrupt();
			try {
				Iterator<Socket> iter = this.sockets.iterator();
				while(iter.hasNext()) {
					Socket socket = iter.next();
					socket.close();
				}
			} catch (IOException e) {}
		}
		return;
	}

}
