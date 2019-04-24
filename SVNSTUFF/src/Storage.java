import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

	private ArrayList<FileContent> files;
	private ArrayList<Chunk> storedChunks;
	private ArrayList<Chunk> restoredChunks;
	private HashMap<String, Integer> blackListedChunks;
	private ConcurrentHashMap<String, Integer> chunkOccurences;
	private int space;
	
	public Storage() {
		storedChunks = new ArrayList<Chunk>();
		restoredChunks = new ArrayList<Chunk>();
		files = new ArrayList<FileContent>();
		blackListedChunks = new HashMap<String, Integer>();
		chunkOccurences = new ConcurrentHashMap<String, Integer>();
		space = 2000000000;
	}
	
	public ArrayList<Chunk> getStoredChunks() {
		return storedChunks;
	}
	
	public HashMap<String, Integer> getBlackListedChunks() {
		return blackListedChunks;
	}
	
	public int occupiedChunkSpace() {
		
		int amountOccupied = 0;
		
		for(int i=0; i<this.storedChunks.size(); i++) {
			amountOccupied+=storedChunks.get(i).getSize();
		}
		
		return amountOccupied;
	}
	
	public ArrayList<Chunk> getRestoredChunks() {
		return restoredChunks;
	}
	public void setSpace(int num) {
		space = num;
	}
	
	public Chunk getSpecificChunk(String fileId, int chunkNo) {
		for(int i=0; i < storedChunks.size(); i++) {
			if(storedChunks.get(i).getFileId().equals(fileId) && storedChunks.get(i).getChunkNo() == chunkNo) {
				return storedChunks.get(i);
			}
		}
		
		return null;
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
	
	public void decStoredOccurence(String uniqueChunkIdentifier) {

        if (Peer.getStorage().getChunkOccurences().containsKey(uniqueChunkIdentifier)) {
            int total = this.chunkOccurences.get(uniqueChunkIdentifier) - 1;
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
        for (Iterator<Chunk> iter = this.storedChunks.iterator(); iter.hasNext(); ) {
            Chunk chunk = iter.next();
            
            if (chunk.getFileId().equals(fileId)) {
                String filename = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + fileId + "/" + "chunk" + chunk.getChunkNo();
                File file = new File(filename);
                file.delete();
                removeChunkOcurrence(fileId, chunk.getChunkNo());
                
                if(space + chunk.getSize() <= 2000000000) {
                	space += chunk.getSize();
                }
                
                iter.remove();
            }
            
        }
	}
	
	public ArrayList<FileContent> getFiles(){
		return files;
	}

	public void addStoredFile(FileContent file) {
        this.files.add(file);
	}
	
	public FileContent constainsFile(String path){
		for (int i = 0; i < this.files.size(); i++) {
			if (files.get(i).getFile().getPath().equals(path)) 
				return files.get(i);
		}
		
		return null;
	}
}
