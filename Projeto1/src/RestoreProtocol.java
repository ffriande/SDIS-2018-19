import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class RestoreProtocol implements Runnable {

    private String file_name;
    ArrayList<String> chunksToRestore;

    public RestoreProtocol(String file_name) {
        this.file_name = file_name;
        chunksToRestore = new ArrayList<String>();
    }

    public void addChunkToRestore(String chunk) {
        this.chunksToRestore.add(chunk);
    }

    @Override
    public void run() {
        if (restoreFile())
            System.out.println("File restored");
        else
            System.out.println("ERROR: File not restored");
    }

    private boolean restoreFile() {
        String filePath = "peer" + Peer.getUniqueId() + "/" + "restored" + "/" + this.file_name;
        File file = new File(filePath);

        // byte[] body;

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            

                ArrayList<Chunk> chunksRestored=new ArrayList<Chunk>();
                
                for (int j = 0; j < chunksToRestore.size(); j++) {
                    for (int i = 0; i < Peer.getStorage().getRestoredChunks().size(); i++) {
                        if (chunksToRestore.get(j).equals(Peer.getStorage().getRestoredChunks().get(i).getFileId() + "-"
                                + Peer.getStorage().getRestoredChunks().get(i).getChunkNo())){
                                    chunksRestored.add(Peer.getStorage().getRestoredChunks().get(i));
                                
                                    break;
                                }
                    }
                }

                FileOutputStream fos = new FileOutputStream(file, true);
                // ir buscar ao storage
                for (Chunk c : chunksRestored) {

                //  body = c.getBody();
                    byte[] body = new byte[c.getBody().length-16];
                    System.arraycopy(c.getBody(), 16, body, 0, body.length);
                    String msg = new String(body, 0, body.length);
                    System.out.println(c.getChunkNo()+" -> "+msg);

                    fos.write(body);

                }

                fos.close();
                return true;
            }
        else {
            System.out.println("\nFile has already been restored\n\n");
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}