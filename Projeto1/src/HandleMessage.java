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
				byte[] chunkBody;
				int header_length=0;
				for(int i=0;i<5;i++){
					header_length+=msgParts[i].length();
					header_length++;//space
				}
				String body = new String(message,header_length, message.length-header_length);
				chunkBody=body.getBytes();
				Chunk chunk = new Chunk(chunkNumber, chunkBody, chunkBody.length);
				chunk.setFileId(fileId);
				Peer.getStorage().getRestoredChunks().add(chunk);
				System.out.println("Received CHUNK " + version + " " + senderPeerID + " " + fileId + " " + chunkNumber);
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
            			Peer.getExecutor().schedule(new BackupSubProtocol(Peer.getUniqueId(), fileId, chunkNumber, chunk.getReplicationDegree(), chunk.getBody()), random.nextInt(400), TimeUnit.MILLISECONDS);
            		}
        		}
        	}
        }
        
        else if(msgParts[0].equals("PUTCHUNKREMOVED")) {
        	
        	//havera uma lista de chunks blacklisted que e feita a nivel do peer que foi reclaimed a medida que ele apaga chunks, por cada putchunk especial que ele
        	//recebe tera de verificar se esse chunk esta na lista, nao fara nada se estiver, mas vai remover da lista para que depois ela possa ficar a zeros.
        	
        	if(!Peer.getStorage().getBlackListedChunks().contains(uniqueChunkIdentifier)) {
            	if(!Peer.getStorage().getChunkOccurences().contains(uniqueChunkIdentifier)) {
            		Peer.getStorage().getChunkOccurences().put(uniqueChunkIdentifier, 0);
            	}
            	
            	if(Peer.getUniqueId() != senderPeerID) {
	            	Random random = new Random();
	            	Peer.getExecutor().schedule(new HandlePutChunk(message), random.nextInt(400), TimeUnit.MILLISECONDS);
            	}
        	}
        	
        }
	}

}
