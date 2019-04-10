import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class HandleMessage implements Runnable {

	private byte[] message;
    private int CR = 0xD;   
	private int LF = 0xA;
	
	public HandleMessage(byte[] msg) {
		message = msg;
	}
	
	@Override
	public void run() {
        String msg = new String(message, StandardCharsets.UTF_8);
      
		String splitMsg = msg.trim();
        String[] msgParts = splitMsg.split(" ");

		Double version = Double.parseDouble(msgParts[1].trim());
        int senderPeerID = Integer.parseInt(msgParts[2]);
        String fileId = msgParts[3];
        int chunkNumber = Integer.parseInt(msgParts[4]);
        
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
        
        else if(msgParts[0].equals("REMOVED")) {
        	
        	if(Peer.getUniqueId() != senderPeerID) {
        		if(Peer.getStorage().getSpecificChunk(fileId, chunkNumber) != null) {
        			
        			Chunk chunk = Peer.getStorage().getSpecificChunk(fileId, chunkNumber); 
        			
            		//"a peer that has a local copy of the chunk shall update its local count of this chunk"
            		Peer.getStorage().decStoredOccurence(uniqueChunkIdentifier);
            		System.out.println("Received REMOVED " + version + " " + senderPeerID + " " + fileId + " " + chunkNumber);
            		
            		//"If this count drops below the desired replication degree of that chunk, it shall initiate the chunk backup subprotocol"
            		int localCount =  Peer.getStorage().getChunkOccurences().get(uniqueChunkIdentifier);
            		
            		System.out.println("Local count: " + localCount + " " + "Rep degree: " + chunk.getReplicationDegree());
            		
            		if(localCount < chunk.getReplicationDegree()) {	
            			Random random = new Random();
            			Peer.getExecutor().schedule(new BackupSubProtocol(fileId, chunkNumber, chunk.getReplicationDegree(), chunk.getBody()), random.nextInt(400), TimeUnit.MILLISECONDS);
            		}
        		}
        	}
        	
        }
	}

	/*private void backupSubProtocol(String fileId, int chunkNumber, int replicationDegree, byte[] chunkBody) {
        
        String header = "PUTCHUNK " + "1.0" + " " + Peer.getUniqueId() + " " + fileId + " " + chunkNumber + " " + replicationDegree+ " " 
        + CR + LF + CR + LF + chunkBody;
        
        System.out.println("PUTCHUNK " + "1.0" + " " + Peer.getUniqueId() + " " + fileId + " " + chunkNumber + " " + replicationDegree);

        String key = fileId + "/" + "chunk" + chunkNumber;
        
        if (!Peer.getStorage().getChunkOccurences().containsKey(key)) { //if this chunk ins't in the current replication degrees table
            Peer.getStorage().getChunkOccurences().put(key, 0); //the chunk is added to that table
        }
        
        byte[] asciiHead = header.getBytes();
        byte[] body = chunkBody;
        byte[] message = new byte[asciiHead.length + body.length];
        
        System.arraycopy(asciiHead, 0, message, 0, asciiHead.length);
        System.arraycopy(body, 0, message, asciiHead.length, body.length);
        
        SendMessage messageSenderThread = new SendMessage(message, "MDB");
         
        Peer.getExecutor().execute(messageSenderThread);
        
        Peer.getExecutor().schedule(new CollectConfirmMessages(message, 1, fileId, chunkNumber, replicationDegree), 1, TimeUnit.SECONDS);
	}*/

}
