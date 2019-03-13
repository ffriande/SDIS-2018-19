import java.io.File;
import java.util.ArrayList;

public class MessageCreator {
	
	private int CR = 0xD;
	private int LF = 0xA;
	private ArrayList<Chunk> chunks;
	private int replicationDegree;
	private File file;
	
	MessageCreator(String path, int replicationDeg){
		this.file = new File(path);
		this.replicationDegree = replicationDeg;
		this.chunks = new ArrayList<>();
	}
	
}
