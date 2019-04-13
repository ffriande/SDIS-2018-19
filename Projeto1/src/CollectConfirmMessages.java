import java.util.concurrent.TimeUnit;

public class CollectConfirmMessages implements Runnable {
	
	private byte[] message;
	private int waitingTime;
	private String fileId;
	private int chunkNo;
	private int replicationDegree;
	private int counter;

	public CollectConfirmMessages(byte[] message, int waitingTime, String fileId, int chunkNo, int replicationDegree) {
		this.message = message;
		this.waitingTime = waitingTime;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		counter = 0;
	}

	@Override
	public void run() {
        
		String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNo;
		
		int confirmationMessages = Peer.getStorage().getChunkOccurences().get(uniqueChunkIdentifier);
		
		System.out.println("Counted replication for " + uniqueChunkIdentifier + " amount of STORED messages: " + confirmationMessages + " desired replication degree: " + replicationDegree);

        if (confirmationMessages < replicationDegree) { 

            Peer.getMDB().sendMessage(message);
         
            this.waitingTime = 2 * this.waitingTime;
            this.counter++;

            if (this.counter < 5)
                Peer.getExecutor().schedule(this, this.waitingTime, TimeUnit.SECONDS);
        
        }
	}

}
