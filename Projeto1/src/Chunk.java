public class Chunk {
	private int chunkNo;
	private String fieldId;
	private byte[] content;
	private int size;
	
    public Chunk(int nr, byte[] content, int size) {
        this.chunkNo = nr;
        this.content = content;
        this.size = size;
    }
}