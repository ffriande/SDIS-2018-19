public class Chunk {
	private int chunkNo;
	private String fileId;
	private byte[] body;
	private int size;
	private int replicationDegree;
	
    public Chunk(int nr, byte[] body, int size, int replicationDegree) {
        this.chunkNo = nr;
        this.body = body;
        this.size=size;
        this.replicationDegree = replicationDegree;
    }
    
    public Chunk(int nr, byte[] body, int size) {
        this.chunkNo = nr;
        this.body = body;
        this.size=size;
    }

    public String getFileId() {
    	return fileId;
    }
    
    public void setFileId(String msg) {
    	fileId = msg;
    }
    
    public int getSize() {
    	return size;
    }

    public int getChunkNo(){
        return this.chunkNo;
    }

    public byte[] getBody() {
        return this.body;
    }

    public int getReplicationDegree() {
    	return replicationDegree;
    }
}