import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HandleGetChunk implements Runnable {

    private String fileId;
    private int chunkNr;

    public HandleGetChunk(String fileId, int chunkNr) {
        this.fileId = fileId;
        this.chunkNr = chunkNr;
    }

    @Override
    public void run() {
        
        for (int i = 0; i < Peer.getStorage().getStoredChunks().size(); i++) {
            if (isSameChunk(Peer.getStorage().getStoredChunks().get(i).getFileId(), Peer.getStorage().getStoredChunks().get(i).getChunkNo())) {
                String header = "CHUNK " + "1.0" + " " + Peer.getUniqueId() + " " + this.fileId + " " + this.chunkNr + " \r\n\r\n";
                
                try {
                    byte[] asciiHeader = header.getBytes("US-ASCII");
                    String chunkPath = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + fileId + "/" + "chunk"+ chunkNr;
                
                    try {  
                    File file = new File(chunkPath);
                    if (!file.exists()) {
                        return;
                    }
                 
                    byte[] body = new byte[(int) file.length()];
                    FileInputStream in = new FileInputStream(file);
                    in.read(body);

                    byte[] message = new byte[asciiHeader.length + body.length];
                    
                    System.arraycopy(asciiHeader, 0, message, 0, asciiHeader.length);
                    System.arraycopy(body, 0, message, asciiHeader.length, body.length);

                    SendMessage sendThread = new SendMessage(message, "MDR");
                    
                    System.out.println("Sent " + "CHUNK " + "1.0" + " " + Peer.getUniqueId() + " " + this.fileId + " " + this.chunkNr);
                    Random random = new Random();

                    Peer.getExecutor().schedule(sendThread, random.nextInt(401), TimeUnit.MILLISECONDS);
                    in.close();
                            
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            }
        }
        
        
    }

    private boolean isSameChunk(String fileId, int chunkNr) {
        return fileId.equals(this.fileId) && chunkNr == this.chunkNr;
    }

}