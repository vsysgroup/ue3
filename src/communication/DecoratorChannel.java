package communication;

import java.io.IOException;

/**
 * provides methods to encode and decode strings
 * @author Barbara Schwankl, 0852176
 *
 */
public abstract class DecoratorChannel implements Channel {

	protected Channel decoratedChannel;

	public DecoratorChannel(Channel channel) {
		this.decoratedChannel = channel;
	}
	
	@Override
	public void send(byte[] msg) {
		decoratedChannel.send(msg);
	}

	@Override
	public byte[] receive() throws IOException {
		return decoratedChannel.receive();
	}
}
