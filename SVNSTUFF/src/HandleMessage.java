import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HandleMessage implements Runnable {

	private byte[] message;

	public HandleMessage(byte[] msg) {
		message = msg;
	}

	@Override
	public void run() {
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String header = "";
		try {
			header = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
			int header_length=header.length();

			String splitMsg = header.trim();
			String[] msgParts = splitMsg.split(" ");

			Double version = Double.parseDouble(msgParts[1].trim());
			int senderPeerID = Integer.parseInt(msgParts[2]);
			String fileId = msgParts[3];

			int chunkNumber = 0;
			if(msgParts.length>=5)
				chunkNumber = Integer.parseInt(msgParts[4]);

			String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNumber;

			if (msgParts[0].equals("PUTCHUNK")) {
				if (!Peer.getStorage().getChunkOccurences().contains(uniqueChunkIdentifier)) {
					Peer.getStorage().getChunkOccurences().put(uniqueChunkIdentifier, 0);
				}

				if (Peer.getUniqueId() != senderPeerID) {
					Random random = new Random();
					Peer.getExecutor().schedule(new HandlePutChunk(message), random.nextInt(401),
							TimeUnit.MILLISECONDS);
				}
			}

			else if (msgParts[0].equals("STORED")) {
				if (Peer.getUniqueId() != senderPeerID) {
					Peer.getStorage().countStoredOccurence(uniqueChunkIdentifier);
					System.out.println("Received STORE for chunk " + uniqueChunkIdentifier);
				}
			}

			else if (msgParts[0].equals("GETCHUNK")) {
				if (Peer.getUniqueId() != senderPeerID) {
					Random random = new Random();
					System.out.println("Received GETCHUNK " + version + " " + senderPeerID + " " + fileId + " " + chunkNumber);
					Peer.getExecutor().schedule(new HandleGetChunk(fileId, chunkNumber), random.nextInt(401),
							TimeUnit.MILLISECONDS);
				}
			}
			else if (msgParts[0].equals("DELETE")) {

				if (Peer.getUniqueId() != senderPeerID) {
					Peer.getStorage().deleteStoredChunk(fileId);
					System.out.println("Received DELETE for file " + fileId);
				}
			}

			else if (msgParts[0].equals("CHUNK")) {
					header_length+=4;
				if (Peer.getUniqueId() != senderPeerID && Peer.isRestoring()) {
					byte[] chunkBody=new byte[message.length-header_length];
					System.arraycopy(message, header_length, chunkBody, 0, message.length-header_length);
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
            		
            		if(localCount < chunk.getReplicationDegree()) {	
            			Random random = new Random();
            			Peer.getExecutor().schedule(new BackupSubProtocol(Peer.getUniqueId(), fileId, chunkNumber, chunk.getReplicationDegree(), chunk.getBody()), random.nextInt(401), TimeUnit.MILLISECONDS);
            		}
        		}
        	}
        }
        
        else if(msgParts[0].equals("PUTCHUNKREMOVED")) {
        	
        	//the peer will access its own blacklisted chunks to ensure it does not receive the same chunks it just got rid of when disk space was reclaimed.
        	
        	if(!Peer.getStorage().getBlackListedChunks().containsKey(uniqueChunkIdentifier) || Peer.getStorage().getBlackListedChunks().get(uniqueChunkIdentifier) != Peer.getUniqueId()) {
            	if(!Peer.getStorage().getChunkOccurences().contains(uniqueChunkIdentifier)) {
            		Peer.getStorage().getChunkOccurences().put(uniqueChunkIdentifier, 0);
            	}
            	
            	if(Peer.getUniqueId() != senderPeerID) {
	            	Random random = new Random();
	            	Peer.getExecutor().schedule(new HandlePutChunk(message), random.nextInt(401), TimeUnit.MILLISECONDS);
            	}
        	}
        	
        }
	}

}
