package communication;

import java.net.Socket;
import java.io.*;

/**
 * This class enables basic communication via TCP.
 * @author Philipp Pfeiffer 0809357
 *
 */
public class TCPCommunication {

	private BufferedReader in = null;
	private PrintWriter out = null;
	@SuppressWarnings("unused")
	private Socket socket = null;
	
	public TCPCommunication(Socket socket) throws IOException{
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.socket = socket;
	}
	
	/**
	 * Sends a message via TCP. It returns a boolean variable to signal that the message has been sent.
	 * @param message
	 * @return boolean
	 */
	public boolean send(String message) {
		out.println(message);
		return true;
	}
	
	/**
	 * Receives messages via TCP.
	 * @return received message as String
	 * @throws IOException
	 */
	public String receive() throws IOException {
		String message = in.readLine();
		return message;
	}
}
