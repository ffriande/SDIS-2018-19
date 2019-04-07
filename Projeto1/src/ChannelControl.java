import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

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
	
	public void sendMessage(byte[] msg) {
    
        try (DatagramSocket sender = new DatagramSocket()) {
            DatagramPacket message = new DatagramPacket(msg, msg.length, address, port);
            sender.send(message);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
	}
	
	@Override
	public void run() {
		
        byte[] buf = new byte[65507];

        try {

            MulticastSocket receiver = new MulticastSocket(port);
            receiver.joinGroup(address);

            while (true) {
                DatagramPacket msg = new DatagramPacket(buf, buf.length);
                receiver.receive(msg);

                byte[] otherbuf = new byte[256];
                
                otherbuf = Arrays.copyOf(buf, msg.getLength());
                
                Peer.getExecutor().execute(new HandleMessage(otherbuf));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
	}

}
