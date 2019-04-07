import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

	private ArrayList<FileContent> files;
	private ArrayList<Chunk> storedChunks;
	private ConcurrentHashMap<String, Integer> chunkOccurences;
	private int space;
	
	public Storage() {
		storedChunks = new ArrayList<Chunk>();
		files = new ArrayList<FileContent>();
		chunkOccurences = new ConcurrentHashMap<String, Integer>();
		space = 2000000000;
	}
	
	public ArrayList<Chunk> getStoredChunks() {
		return storedChunks;
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
	
	public void removeChunkOcurrence(String fileId, int chunkNo) {
		String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNo;
		this.chunkOccurences.remove(uniqueChunkIdentifier);
	}
	
	public void deleteStoredChunk(String fileId) {
		Chunk chunkToDelete = null;
		boolean delete = false;
		
		for(int i=0; i < this.storedChunks.size(); i++) {
			chunkToDelete = this.storedChunks.get(i);
			
			if(chunkToDelete.getFileId().equals(fileId)) {
				delete = true;
				String path = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + fileId + "/" + "chunk" + chunkToDelete.getChunkNo();
				File fileToDelete = new File(path);
				
				//remove from disk
				fileToDelete.delete();
			}
		}
		
		//remove from stored chunks
		if(delete == true) {
			this.storedChunks.remove(chunkToDelete);
		}
	}
	
	public ArrayList<FileContent> getFiles(){
		return files;
	}

	public void addStoredFile(FileContent file) {
        this.files.add(file);
    }
}
