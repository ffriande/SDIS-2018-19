import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HandleMessage implements Runnable {

	private byte[] message;
	
	public HandleMessage(byte[] msg) {
		message = msg;
	}
	
	@Override
	public void run() {
        String msg = new String(message, StandardCharsets.UTF_8);
      
		String splitMsg = msg.trim();
        String[] msgParts = splitMsg.split(" ");
        
        String fileId = msgParts[3];
        int chunkNumber = Integer.parseInt(msgParts[4]);
        
        int senderPeerID = Integer.parseInt(msgParts[2]);

		Double version = Double.parseDouble(msgParts[1].trim());
        
		String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNumber;
	        
        if(msgParts[0].equals("PUTCHUNK")) {
        	
        	if(!Peer.getStorage().getChunkOccurences().contains(uniqueChunkIdentifier)) {
        		Peer.getStorage().getChunkOccurences().put(uniqueChunkIdentifier, 0);
        	}
        	
        	if(Peer.getUniqueId() != senderPeerID) {
            	Random random = new Random();
            	Peer.getExecutor().schedule(new HandlePutChunk(message), random.nextInt(400), TimeUnit.MILLISECONDS);
        	}
        }
        
        else if(msgParts[0].equals("STORED")) {
        	if(Peer.getUniqueId() != senderPeerID) {
        		Peer.getStorage().countStoredOccurence(uniqueChunkIdentifier);
        		System.out.println("Received STORE for chunk " + uniqueChunkIdentifier);
        	}
        }
		
		else if(msgParts[0].equals("GETCHUNK")) {
			if(Peer.getUniqueId() != senderPeerID) {	
				Random random = new Random();
				System.out.println("Received GETCHUNK " + version + " " + senderPeerID + " " + fileId + " " + chunkNumber);
				Peer.getExecutor().schedule(new HandleGetChunk(fileId, chunkNumber), random.nextInt(400), TimeUnit.MILLISECONDS);
			}
		}

        else if(msgParts[0].equals("DELETE")) {
        	if(Peer.getUniqueId() != senderPeerID) {
        		Peer.getStorage().deleteStoredChunk(fileId);
        		System.out.println("Received DELETE for chunk " + uniqueChunkIdentifier);
        	}
		}
		
		else if(msgParts[0].equals("CHUNK")) {
			if(Peer.getUniqueId() != senderPeerID) {
				byte[] chunkBody = msgParts[5].getBytes();
				Chunk chunk = new Chunk(chunkNumber, chunkBody, chunkBody.length);
				chunk.setFileId(fileId);
				Peer.getStorage().getRestoredChunks().add(chunk);
				System.out.println("Received CHUNK " + version + " " + senderPeerID + " " + fileId + " " + chunkNumber);
			}
			
		}
	}

}
