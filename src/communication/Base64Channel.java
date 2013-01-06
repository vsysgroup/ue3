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
	public void send(byte[] msg) {
		decoratedChannel.send(encode(msg));
	}
	
	@Override
	public byte[] receive() throws IOException {
		return decode(decoratedChannel.receive());
	}
	
	/**
	 * encode into Base64 format
	 * @param encrypted message
	 * @return encoded encrypted message
	 */
	private byte[] encode(byte[] msg) {
		return Base64.encode(msg);
	}
	
	/**
	 * decode from Base64 format
	 * @param encoded and encrypted message
	 * @return decoded but still encrypted message
	 */
	private byte[] decode(byte[] msg) {
		return Base64.decode(msg); 
	}

	
}
