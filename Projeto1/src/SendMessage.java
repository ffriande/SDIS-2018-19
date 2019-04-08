
public class SendMessage implements Runnable {

	byte[] message;
	String channel;
	
	public SendMessage(byte[] message, String channel) {
		this.message = message;
		this.channel = channel;
	}
	
	@Override
	public void run() {
		if(channel.equals("MDB")) {
			Peer.getMDB().sendMessage(this.message);
		}
		
		if(channel.equals("MC")) {
			Peer.getMC().sendMessage(this.message);
		}

		if(channel.equals("MDR")) {
			Peer.getMDR().sendMessage(this.message);
		}
	}

}
