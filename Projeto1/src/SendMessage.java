
public class SendMessage implements Runnable {

	byte[] message;
	
	public SendMessage(byte[] message) {
		this.message = message;
	}
	
	@Override
	public void run() {
		Peer.getMDB().sendMessage(this.message);
	}

}
