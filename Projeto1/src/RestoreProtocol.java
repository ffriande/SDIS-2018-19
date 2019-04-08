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
        System.out.println(file.getAbsolutePath()+" -> filePath");

        byte[] body;

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file, true);

            for (Chunk c : chunksToRestore) {
                // String chunkPath = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + c.getFileId() + "/" + "chunk"
                //         + c.getChunkNo();

                File chunkFile = new File(chunkPath);

                System.out.println(chunkPath);
                if (!chunkFile.exists()) {
                    System.out.println("YOOOO\n\n");

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