import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

	private ArrayList<FileContent> files;
	private ArrayList<Chunk> storedChunks;
	private ConcurrentHashMap<String, Integer> chunkOccurences;
	private int space;
	
	public Storage() {
		storedChunks = new ArrayList<Chunk>();
		chunkOccurences = new ConcurrentHashMap<String, Integer>();
		space = 2000000000;
	}
	
	boolean backupChunk(Chunk chunkToStore) {
		for(int i = 0; i < storedChunks.size(); i++) {
			if(storedChunks.get(i).getFileId().equals(chunkToStore.getFileId()) && storedChunks.get(i).getChunkNo() == chunkToStore.getChunkNo()) {
				System.err.println("Chunk already stored! " + storedChunks.get(i).getFileId() + " " + storedChunks.get(i).getChunkNo());
				return false;
			}
		}
		
		storedChunks.add(chunkToStore);
		
		space = space - chunkToStore.getSize();
		return true;
	}
	
	public ConcurrentHashMap<String, Integer> getChunkOccurences(){
		return chunkOccurences;
	}
	
	public void countStoredOccurence(String uniqueChunkIdentifier) {

        if (!Peer.getStorage().getChunkOccurences().containsKey(uniqueChunkIdentifier)) {
            Peer.getStorage().getChunkOccurences().put(uniqueChunkIdentifier, 1);
        } else {
            int total = this.chunkOccurences.get(uniqueChunkIdentifier) + 1;
            this.chunkOccurences.replace(uniqueChunkIdentifier, total);
        }

	}
	
	int getSpace() {
		return space;
	}

	public void addStoredFile(FileContent file) {
        this.files.add(file);
    }
}
