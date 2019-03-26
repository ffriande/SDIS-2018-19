import java.util.concurrent.TimeUnit;

public class CollectConfirmMessages implements Runnable {
	
	private byte[] message;
	private int waitingTime;
	private String identifier;
	private int chunkNo;
	private int replicationDegree;
	private int counter;

	public CollectConfirmMessages(byte[] message, int waitingTime, String identifier, int chunkNo, int replicationDegree) {
		this.message = message;
		this.waitingTime = waitingTime;
		this.identifier = identifier;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		counter = 1;
	}

	@Override
	public void run() {
        int confirmationMessages = 0; // how to count confirmation messages????

        if (confirmationMessages < replicationDegree) { 

            Peer.getMDB().sendMessage(message);
         
            this.waitingTime = 2 * this.waitingTime;
            this.counter++;

            if (this.counter < 5)
                Peer.getExecutor().schedule(this, this.waitingTime, TimeUnit.SECONDS);
        }
	}

}
