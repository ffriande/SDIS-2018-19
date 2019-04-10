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
	String msg;

	public HandlePutChunk(byte[] message) {
		
	    this.msg = new String(message, 0, message.length);
        String splitMsg = msg.trim();
        String[] msgParts = splitMsg.split(" ");
        
        fileId = msgParts[3];
        chunkNumber = Integer.parseInt(msgParts[4]);
		replicationDegree = Integer.parseInt(msgParts[5]);
		int header_length=0;

		for(int i=0;i<6;i++){
			header_length+=msgParts[i].length();
			header_length++;//space
		}
		String body = new String(message,header_length, message.length-header_length);
		chunkBody=body.getBytes();
                
	}
	
	@Override
	public void run() {
		
		String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNumber;
		
		// don't save if the peer already owns the chunk
        for(int i=0; i < Peer.getStorage().getFiles().size(); i++){
            if(Peer.getStorage().getFiles().get(i).getIdentifier().equals(fileId)) 
                return;
        }
		
		if(Peer.getStorage().getChunkOccurences().get(uniqueChunkIdentifier) >= replicationDegree) {
			return;
		}
		
		if(Peer.getStorage().getSpace() >= chunkBody.length) {
			Chunk chunk = new Chunk(chunkNumber, chunkBody, chunkBody.length,  replicationDegree);
			chunk.setFileId(fileId);
			
			if(!Peer.getStorage().backupChunk(chunk)) {
				return;
			}
			
            try {
            	
            	String pathToBackup = "peer" + Peer.getUniqueId() + "/" + "backup";
            	
            	File subdirectoryBackup = new File(pathToBackup);
            	
            	if(!subdirectoryBackup.exists()) {
            		subdirectoryBackup.mkdirs();
            	}
            	
        		String pathToBackupFile = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + fileId;
        		File pathToFile = new File(pathToBackupFile);
        		pathToFile.mkdir();

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
