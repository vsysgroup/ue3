package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * This thread only runs for a short time in which it sends a datagram to a client.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class UDPNotificationThread extends Thread {
	
	private int port;
	private String message = "";
	private InetAddress inetAddress = null;
	
	
	public UDPNotificationThread(InetAddress inetAddress, int port, String message) {
		this.port = port;
		this.message = message;
		this.inetAddress = inetAddress;
	}
	
	public void run() {
		DatagramSocket datagramSocket = null;
		DatagramPacket datagramPacket = null;
		byte[] carrier = (message).getBytes();
		try {
			datagramSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Failed to create Socket for UDP notification.");
		}
		datagramPacket = new DatagramPacket(carrier, carrier.length, inetAddress, port);
		try {
			datagramSocket.send(datagramPacket);
		} catch (IOException e) {
			System.out.println("Failed to send UDP notification.");
		}
	}

}
