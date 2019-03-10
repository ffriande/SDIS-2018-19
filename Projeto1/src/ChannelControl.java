import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChannelControl implements Runnable{
	
	private String MCAddress;
	private int port;
	private InetAddress address;

	ChannelControl(String MC_addr, int MCPort){
		
        try {
            MCAddress = MC_addr;
            port = MCPort;
            address = InetAddress.getByName(MCAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
