public class Chunk {
	private int chunkNo;
	private String fieldId;
	private byte[] body;
	private int size;
	
    public Chunk(int nr, byte[] body, int size) {
        this.chunkNo = nr;
        this.body = body;
        this.size=size;
    }


    public int getChunkNo(){
        return this.chunkNo;
    }

    public byte[] getBody() {
        return this.body;
    }

}