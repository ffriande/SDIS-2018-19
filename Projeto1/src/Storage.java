import java.util.ArrayList;

public class Storage {
	
	private ArrayList<Chunk> storedChunks;
	private int space;
	
	public Storage() {
		storedChunks = new ArrayList<Chunk>();
		space = 2000000000;
	}
	
	boolean backupChunk(Chunk chunkToStore) {
		for(int i = 0; i < storedChunks.size(); i++) {
			if(storedChunks.get(i).getFileId() == chunkToStore.getFileId() && storedChunks.get(i).getChunkNo() == chunkToStore.getChunkNo()) {
				System.err.println("Chunk already stored!");
				return false;
			}
		}
		
		storedChunks.add(chunkToStore);
		space = space - chunkToStore.getSize();
		return true;
	}
	
	int getSpace() {
		return space;
	}
}
