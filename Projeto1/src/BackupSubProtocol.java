import java.util.concurrent.TimeUnit;

public class BackupSubProtocol implements Runnable {

	String fileId;
	int chunkNumber;
	int replicationDegree;
	byte[] chunkBody;
    private int CR = 0xD;   
	private int LF = 0xA;
	
	public BackupSubProtocol(String fileId, int chunkNumber, int replicationDegree, byte[] chunkBody) {
		this.fileId = fileId;
		this.chunkNumber = chunkNumber;
		this.replicationDegree = replicationDegree;
		this.chunkBody = chunkBody;
	}
	
	@Override
	public void run() {
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
	}

}
