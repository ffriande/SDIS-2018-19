import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements RemoteInterface {
	
	private static int unique_id;
	private static double protocol_version;
	private static ScheduledThreadPoolExecutor threadPool;
	
    private ChannelControl MC;
    private ChannelBackup MDB;
    private ChannelRestore MDR;

    private int CR = 0xD;   
	private int LF = 0xA;
	
	public Peer(String MC_address, int MC_port, String MDB_address, int MDB_port, String MDR_address, int MDR_port) {
        MC = new ChannelControl(MC_address, MC_port);
        MDB = new ChannelBackup(MDB_address, MDB_port);
        MDR = new ChannelRestore(MDR_address, MDR_port);

        threadPool = new ScheduledThreadPoolExecutor(100);
	}

	public static void main(String[] args) {
		
		if(args.length != 9) { 
			System.out.println("Invalid use, peer must be called like so: Peer  <protocol_version> <unique_id> <peer_ap> <MC_address> <MC_port> <MDB_address> <MDB_port> <MDR_address> <MDR_port>");
			return;
		}
		
		protocol_version = Double.parseDouble(args[0]);
		unique_id = Integer.parseInt(args[1]);
		String peer_ap = args[2];
		
		String MC_address = args[3];
		int MC_port = Integer.parseInt(args[4]);
		
		String MDB_address = args[5];
		int MDB_port = Integer.parseInt(args[6]);
		
		String MDR_address = args[7];
		int MDR_port = Integer.parseInt(args[8]);
		
        try {
            Peer server = new Peer(MC_address, MC_port, MDB_address, MDB_port, MDR_address, MDR_port);
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(server, 0);
            
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(peer_ap, stub);
            
            System.err.println("Peer ready");
        } catch (RemoteException ex) {
            System.out.println("RMI error: " + ex.getMessage());
        } catch(AlreadyBoundException ex) {
        	System.out.println("RMI error: " + ex.getMessage());
        }
	}
	
    @Override
	public synchronized void backup(String path, int replicationDegree) {
        FileSplitter file = new FileSplitter(path, replicationDegree);
        ArrayList<Chunk> chunks = file.getChunks();
        
        for(int i=0;i< chunks.size();i++){
            String header = "PUTCHUNK " + protocol_version + " " + unique_id + " " + file.getIdentifier() + " " + chunks.get(i).getChunkNo() + " " + replicationDegree+ " " 
            + CR + LF + CR + LF + chunks.get(i).getBody();

            System.out.println(header);
            
            threadPool.schedule(MDB, randomDelay(), TimeUnit.MILLISECONDS);
        }   
        
	}
	
    @Override
    public void restore(String path) throws RemoteException {
    	
	}
    
    @Override
    public void delete(String path) throws RemoteException {
    	
    }
    
    @Override
    public void reclaim(int space) throws RemoteException {
    	
    }
    
    @Override
    public void retrieveStateInfo() throws RemoteException {
    	
    }

    public int randomDelay(){ 
        Random rand = new Random();
        //[0-400]ms delay
        return rand.nextInt(401);
    }
}