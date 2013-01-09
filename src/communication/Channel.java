package communication;

import java.io.IOException;


public interface Channel {

	public void send(byte[] out);
	
	public byte[] receive() throws IOException;
	
	public Channel getDecoratedChannel();
}
