import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChannelBackup implements Runnable {
	
	private String MDBAddress;
	private int port;
	private InetAddress address;

	ChannelBackup(String MDB_addr, int MDBPort){
		
        try {
            MDBAddress = MDB_addr;
            port = MDBPort;
            address = InetAddress.getByName(MDBAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
