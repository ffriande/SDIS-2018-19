import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HandleMessage implements Runnable {

	private byte[] message;
	
	public HandleMessage(byte[] msg) {
		message = msg;
	}
	
	@Override
	public void run() {
        String msg = new String(message, 0, message.length);
      
        String splitMsg = msg.trim();
        String[] msgParts = splitMsg.split(" ");
        
        String fileId = msgParts[3];
        int chunkNumber = Integer.parseInt(msgParts[4]);
        
        String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNumber;
        
        if(msgParts[0].equals("PUTCHUNK")) {
        	Random random = new Random();
        	
        	if(!Peer.getStorage().getChunkOccurences().contains(uniqueChunkIdentifier)) {
        		Peer.getStorage().getChunkOccurences().put(uniqueChunkIdentifier, 0);
        	}
        	
        	Peer.getExecutor().schedule(new HandlePutChunk(message), random.nextInt(400), TimeUnit.MILLISECONDS);
        }
        
        else if(msgParts[0].equals("STORED")) {
        	if(!Peer.getStorage().getChunkOccurences().contains(uniqueChunkIdentifier)) {
        		Peer.getStorage().countStoredOccurence(uniqueChunkIdentifier);
        		System.out.println("Received STORE for chunk " + uniqueChunkIdentifier);
        	}
        }
	}

}
