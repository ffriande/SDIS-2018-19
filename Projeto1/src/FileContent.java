import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class FileContent {
	
	private ArrayList<Chunk> chunks;
	private int replicationDegree;
	private File file;
	private String identifier;
	
	FileContent(String path, int replicationDeg){
		this.file = new File(path);
		this.replicationDegree = replicationDeg;
		this.chunks = new ArrayList<>();
		
		try {
			splitIntoChuncks();
		} catch (IOException e) {
			System.err.println("Couldn't split file " + this.file.getName() + " into chunks!");
			e.printStackTrace();
		}
		
		hashIdentifier();
	}
	
	public ArrayList<Chunk> getChunks(){
		return chunks;
	}
	
	public int getReplicationDegree() {
		return replicationDegree;
	}

	private void hashIdentifier() {
		String fileInfo = file.getName() + "/" + file.getAbsolutePath() + "/" + file.lastModified();
		this.identifier = sha256(fileInfo);
	}

	private String sha256(String originalString) {
		MessageDigest digest = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error trying to digest message!");
			e.printStackTrace();
			System.exit(0);
		}
		
		byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
		return bytesToHex(encodedhash);
	}
	
	private static String bytesToHex(byte[] hash) {
		
	    StringBuffer hexString = new StringBuffer();
	    
	    for (int i = 0; i < hash.length; i++) {
		    String hex = Integer.toHexString(0xff & hash[i]);
		    
		    if(hex.length() == 1) 
		    	hexString.append('0'); 
		    
		    hexString.append(hex);
	    }
	    
	    return hexString.toString();
	}

	private void splitIntoChuncks() throws FileNotFoundException, IOException {
        int chunkNumber = 1, totalBytesRead = 0;
        int file_size = (int) this.file.length();
        
        FileInputStream istream = null;
        try {
            istream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("Can't read file: " + this.file.getName());
            return;
        }
        
        while (totalBytesRead < file_size) {
            int chunkSize = Math.min(64000, (file_size - totalBytesRead));
            
            byte[] chunkBody = new byte[chunkSize]; 
            
            try {
                totalBytesRead += istream.read(chunkBody, 0, chunkSize);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            Chunk chunk = new Chunk(chunkNumber, chunkBody, chunkSize, replicationDegree);
            chunk.setFileId(identifier);
            this.chunks.add(chunk);
            chunkNumber++;
        }
        
        if ((file_size % 64000) == 0) {
        	//"If the file size is a multiple of the chunk size, the last chunk has size 0."
            Chunk chunk = new Chunk(chunkNumber+1, new byte[0], 0, replicationDegree);
            chunk.setFileId(this.getIdentifier());
            this.chunks.add(chunk);
        }  

        try {
            istream.close();
        } catch (IOException exp) {
            exp.printStackTrace();
        }
	}
	
	public File getFile() {
		return file;
	}
	
	public String getIdentifier() {
		return identifier;
	}
}
