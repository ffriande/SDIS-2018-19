
public class test implements Runnable {

	byte[] message;
	
	public test(byte[] message) {
		this.message = message;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Peer.getMDB().sendMessage(message);
	}

}
