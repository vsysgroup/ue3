package communication;

import java.io.IOException;

import org.bouncycastle.util.encoders.Base64;

/**
 * provides methods to encode and decode strings in base64
 * @author Barbara Schwankl, 0852176
 *
 */
public class Base64Channel extends DecoratorChannel {

	public Base64Channel(Channel channel) {
		super(channel);
	}
	
	@Override
	public void send(String msg) {
		decoratedChannel.send(encode(msg));
	}
	
	@Override
	public String receive() throws IOException {
		return decode(decoratedChannel.receive());
	}
	
	/**
	 * encode into Base64 format
	 * @param encrypted message
	 * @return encoded encrypted message
	 */
	private String encode(String msg) {
		byte[] base64Message = Base64.encode(msg.getBytes());
		return base64Message.toString();
	}
	
	/**
	 * decode from Base64 format
	 * @param encoded and encrypted message
	 * @return decoded but still encrypted message
	 */
	private String decode(String msg) {
		byte[] encryptedMessage = Base64.decode(msg); 
		return encryptedMessage.toString();
	}

	
}
