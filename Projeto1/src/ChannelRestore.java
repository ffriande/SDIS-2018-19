import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChannelRestore implements Runnable {
	
	private String MDRAddress;
	private int port;
	private InetAddress address;

	ChannelRestore(String MDR_addr, int MDRPort){
		
        try {
            MDRAddress = MDR_addr;
            port = MDRPort;
            address = InetAddress.getByName(MDR_addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
