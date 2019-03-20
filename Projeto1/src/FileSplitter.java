import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

public class FileSplitter {
	
	private int CR = 0xD;
	private int LF = 0xA;
	private ArrayList<Chunk> chunks;
	private int replicationDegree;
	private File file;
	private String identifier;
	
	FileSplitter(String path, int replicationDeg){
		this.file = new File(path);
		this.replicationDegree = replicationDeg;
		this.chunks = new ArrayList<>();
		splitIntoChuncks();
		hashIdentifier();
	}
	
	public ArrayList<Chunk> getChunks(){
		return chunks;
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

	private void splitIntoChuncks() {
		
	}
	
}
