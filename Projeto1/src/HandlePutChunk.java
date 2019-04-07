import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HandlePutChunk implements Runnable {
	
	private int chunkNumber;
	private byte[] chunkBody;
	private String fileId;
	private int replicationDegree;
	
    private int CR = 0xD;   
	private int LF = 0xA;

	public HandlePutChunk(byte[] message) {
		
	    String msg = new String(message, 0, message.length);
        String splitMsg = msg.trim();
        String[] msgParts = splitMsg.split(" ");
        
        fileId = msgParts[3];
        chunkNumber = Integer.parseInt(msgParts[4]);
        replicationDegree = Integer.parseInt(msgParts[5]);
        chunkBody = msgParts[6].getBytes();
	}
	
	@Override
	public void run() {
		
		String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNumber;
		
		if(Peer.getStorage().getChunkOccurences().get(uniqueChunkIdentifier) >= replicationDegree) {
			return;
		}
		
		if(Peer.getStorage().getSpace() >= chunkBody.length) {
			Chunk chunk = new Chunk(chunkNumber, chunkBody, chunkBody.length);
			
			if(!Peer.getStorage().backupChunk(chunk)) {
				return;
			}
			
            try {
            	
            	String pathToBackup = "peer" + Peer.getUniqueId() + "/" + "backup";
            	
            	String pathToRestore = "peer" + Peer.getUniqueId() + "/" + "restore";
            	
            	File subdirectoryBackup = new File(pathToBackup);
            	
            	if(!subdirectoryBackup.exists()) {
                	subdirectoryBackup.mkdirs();
            	}
            	
            	File subdirectoryRestore = new File(pathToRestore);
            	
            	if(!subdirectoryRestore.exists()) {
                	subdirectoryRestore.mkdir();
            	}
            	
            	String actualPathToPutChunk = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + fileId;
            	
            	File actualPathToChunk = new File (actualPathToPutChunk);
            	
            	if(!actualPathToChunk.exists()) {
            		actualPathToChunk.mkdir();
            	}
            	
                String filename = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + fileId + "/" + "chunk" + chunkNumber;

                try (FileOutputStream fos = new FileOutputStream(filename)) {
                    fos.write(chunkBody);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            
            String header = "STORED " + "1.0" + " " + Peer.getUniqueId() + " " + fileId + " " + chunkNumber + " " + CR + LF + CR + LF;
            
            System.out.println("STORED " + "1.0" + " " + Peer.getUniqueId() + " " + fileId + " " + chunkNumber);
            
            Peer.getStorage().countStoredOccurence(uniqueChunkIdentifier);
            
            Peer.getMC().sendMessage(header.getBytes());
		}
	}

}
