import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RestoreProtocol implements Runnable {

    private String file_name;
    ArrayList<Chunk> chunksToRestore;

    public RestoreProtocol(String file_name) {
        this.file_name = file_name;
        chunksToRestore = new ArrayList<Chunk>();
    }

    public void addChunkToRestore(Chunk chunk){
        this.chunksToRestore.add(chunk);
    }

    @Override
    public void run() {
        if (restoreFile())System.out.println("File restored");
        else System.out.println("ERROR: File not restored");
   }

    private boolean restoreFile() {
        String filePath = Peer.getUniqueId() + "/" + "restore" + "/" + this.file_name;
        File file = new File(filePath);
        byte[] body;

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file, true);
            System.out.println("OI1\n\n");

            for (Chunk c : chunksToRestore) {
                String chunkPath = Peer.getUniqueId() + "/" + "backup" + "/" + c.getFileId() + "/" + "chunk" + c.getChunkNo();

                File chunkFile = new File(chunkPath);

                if (!chunkFile.exists()) {
                    System.out.println("OI2\n\n");
                    System.out.println("chunkPath");
                   
                   
                    return false;

                }

                body = new byte[(int) chunkFile.length()];

                FileInputStream in = new FileInputStream(chunkFile);

                in.read(body);
                fos.write(body);

                chunkFile.delete();
            }

            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}