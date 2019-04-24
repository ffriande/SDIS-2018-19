import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class HandlePutChunk implements Runnable {
	
	private int chunkNumber;
	private byte[] chunkBody;
	private int bodyLength;
	private String fileId;
	private int replicationDegree;
	
	private String endHeader = " \r\n\r\n";
	String msg;

	public HandlePutChunk(byte[] message) {
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		String header = "";
		int header_length=0;
		try {
			header = reader.readLine();
			header_length=header.length();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String splitMsg = header.trim();
		String[] msgParts= splitMsg.split(" ");
		fileId = msgParts[3].trim();		
		chunkNumber = Integer.parseInt(msgParts[4]);
		replicationDegree = Integer.parseInt(msgParts[5]);

		header_length+=4;
		chunkBody=new byte[message.length-header_length];	
		System.arraycopy(message, header_length, chunkBody, 0, message.length-header_length);
		bodyLength=chunkBody.length;

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

		if(Peer.getStorage().getSpace() >= bodyLength) {
			Chunk chunk = new Chunk(chunkNumber, chunkBody, bodyLength,  replicationDegree);
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
            
            String header = "STORED " + "1.0" + " " + Peer.getUniqueId() + " " + fileId + " " + chunkNumber + endHeader;
            System.out.println("STORED " + "1.0" + " " + Peer.getUniqueId() + " " + fileId + " " + chunkNumber);
            
            Peer.getStorage().countStoredOccurence(uniqueChunkIdentifier);
            try {
				Peer.getMC().sendMessage(header.getBytes("US-ASCII"));}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		}
		
		else {
			System.out.println("Error: There is not enough space to store chunks!");
		}
	}

}
