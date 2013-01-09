package communication;

import java.net.Socket;
import java.io.*;

/**
 * This class enables basic communication via TCP.
 * @author Philipp Pfeiffer 0809357
 * @author Barbara Schwankl 0852176
 *
 */
public class TCPChannel implements Channel {

	private BufferedReader in = null;
	private PrintWriter out = null;
	@SuppressWarnings("unused")
	private Socket socket = null;
	
	public TCPChannel(Socket socket) throws IOException{
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.socket = socket;
	}
	
	/**
	 * Sends a message via TCP. It returns a boolean variable to signal that the message has been sent.
	 * @param message
	 * @return boolean
	 */
	public void send(byte[] message) {
		String tmp = new String(message);
		out.println(tmp);
//		return true;
	}
	
	/**
	 * Receives messages via TCP.
	 * @return received message as String
	 * @throws IOException
	 */
	public byte[] receive() throws IOException {
		String message = in.readLine();
		byte[] tmp = message.getBytes();
		return tmp;
	}

	@Override
	public Channel getDecoratedChannel() {
		return null;
	}
}
