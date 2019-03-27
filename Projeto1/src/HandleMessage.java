import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HandleMessage implements Runnable {

	private byte[] message;
	
	public HandleMessage(byte[] msg) {
		message = msg;
	}
	
	@Override
	public void run() {
        String msg = new String(message, 0, message.length);
        
        System.out.println(msg);
      
        String splitMsg = msg.trim();
        String[] msgParts = splitMsg.split(" ");
        
        if(msgParts[0].equals("PUTCHUNK")) {
        	Random random = new Random();
        	Peer.getExecutor().schedule(new HandlePutChunk(message), random.nextInt(400), TimeUnit.MILLISECONDS);
        }
        
        else if(msgParts[0].equals("STORED")) {
        	Peer.getExecutor().execute(new HandleStored(message));
        }
	}

}
