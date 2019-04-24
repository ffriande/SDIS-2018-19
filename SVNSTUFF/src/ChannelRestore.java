import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ChannelRestore implements Runnable {

    private String MDRAddress;
    private int port;
    private InetAddress address;

    ChannelRestore(String MDR_addr, int MDRPort) {

        try {
            MDRAddress = MDR_addr;
            port = MDRPort;
            address = InetAddress.getByName(MDRAddress);
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
            System.out.println("MDR:  " + address.getHostAddress());

            while (true) {
                DatagramPacket msg = new DatagramPacket(buf, buf.length);
                receiver.receive(msg);

                byte[] otherbuf = new byte[65507];

                otherbuf = Arrays.copyOf(buf, msg.getLength());

                Peer.getExecutor().execute(new HandleMessage(otherbuf));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
