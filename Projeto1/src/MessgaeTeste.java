
public class MessgaeTeste implements Runnable {

	byte[] message;
	
	public MessgaeTeste(byte[] message) {
		this.message = message;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Peer.getMDB().sendMessage(message);
	}

}
