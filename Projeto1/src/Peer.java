import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements RemoteInterface {
	//TODO: ver o getchunk!!, se um peer estiver a enviar ou ja tiver enviado, o outro não envia. concurrent hashmap, provavelmente
	private static int unique_id;
	private static double protocol_version;
	private static ScheduledThreadPoolExecutor threadPool;
	private static Storage storage;
	private static RestoreProtocol restore;
	
	int STORAGE_MAX_SIZE = 2000000000;
	
    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;

	private String endHeader = " \r\n\r\n";
	
	public Peer(String MC_address, int MC_port, String MDB_address, int MDB_port, String MDR_address, int MDR_port) {
		storage = new Storage();
		threadPool = new ScheduledThreadPoolExecutor(100);
		MC = new ChannelControl(MC_address, MC_port);
        MDB = new ChannelBackup(MDB_address, MDB_port);
        MDR = new ChannelRestore(MDR_address, MDR_port);
	}
	
	public static ChannelBackup getMDB() {
		return MDB;
	}
	
	public static ChannelControl getMC() {
		return MC;
	}

	public static ChannelRestore getMDR() {
		return MDR;
	}
	
	static ScheduledThreadPoolExecutor getExecutor() {
		return threadPool;
	}
	
	public static Storage getStorage() {
		return storage;
	}

	public static boolean isRestoring() {
		if(restore!=null)
			return !restore.getChunksToRestore().isEmpty();
		return false;
	}
	
	public static int getUniqueId() {
		return unique_id;
	}

	public static void main(String[] args) throws RemoteException {
		
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
        
        threadPool.execute(MC);
        threadPool.execute(MDB);
        threadPool.execute(MDR);
	}
	
    @Override
	public synchronized void backup(String path, int replicationDegree) {
        FileContent file = new FileContent(path, replicationDegree);
        ArrayList<Chunk> chunks = file.getChunks();
        storage.addStoredFile(file);
        
        for(int i=0;i<chunks.size();i++){
			String header = "PUTCHUNK " + protocol_version + " " + unique_id + " " + file.getIdentifier() + " " + chunks.get(i).getChunkNo() + " " + replicationDegree + endHeader ;
            
            String uniqueChunkIdentifier = file.getIdentifier() + "/" + "chunk" + chunks.get(i).getChunkNo();
            
            if (!storage.getChunkOccurences().containsKey(uniqueChunkIdentifier)) {
                Peer.getStorage().getChunkOccurences().put(uniqueChunkIdentifier, 0);
            }

            System.out.println("PUTCHUNK " + protocol_version + " " + unique_id + " " + file.getIdentifier() + " " + chunks.get(i).getChunkNo() + " " + replicationDegree);
			try {
				byte[] asciiHead = header.getBytes("US-ASCII");
				byte[] body = chunks.get(i).getBody();
				byte[] message = new byte[asciiHead.length + body.length];
				
				System.arraycopy(asciiHead, 0, message, 0, asciiHead.length);
				System.arraycopy(body, 0, message, asciiHead.length, body.length);
				
				SendMessage messageSenderThread = new SendMessage(message, "MDB");
				
				threadPool.execute(messageSenderThread);

				Peer.getExecutor().schedule(new CollectConfirmMessages(message, 1, file.getIdentifier(), chunks.get(i).getChunkNo(), replicationDegree), 1, TimeUnit.SECONDS);
			} catch (UnsupportedEncodingException e) {
				System.out.println("BACKUP ENCODING ERROR ON MESSAGE SENDER");
			}
     
        }   
	}
	
    @Override
    public synchronized void restore(String path) throws RemoteException {
    	String fileName = null;
		FileContent f=storage.constainsFile(path);
		restore = new RestoreProtocol(path);
            if (f!=null) {
                for (int i = 0; i < f.getChunks().size(); i++) {

                    String header = "GETCHUNK " + protocol_version + " " + unique_id + " " + f.getIdentifier() + " " + f.getChunks().get(i).getChunkNo() + endHeader;
                    System.out.println("Sent "+ "GETCHUNK " + protocol_version + " " + unique_id + " " + f.getIdentifier() + " " + f.getChunks().get(i).getChunkNo());
				
					try {
                    fileName = f.getFile().getName();

					restore.addChunkToRestore(f.getIdentifier() + "-" + f.getChunks().get(i).getChunkNo());

                    SendMessage sendThread = new SendMessage(header.getBytes("US-ASCII"), "MC");
					threadPool.execute(sendThread);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
                }
            Peer.getExecutor().schedule(restore, 1, TimeUnit.SECONDS);
        } else System.out.println("ERROR: File was never backed up.");
    }
	
    
    @Override
    public void delete(String path) throws RemoteException {
    	for(int i=0;i<storage.getFiles().size();i++) {
    		
    		FileContent file = storage.getFiles().get(i);
    		
    		if(file.getFile().getPath().equals(path)) {
    			
    			//sending an arbitrary amount of times to ensure all space used is deleted
    			for(int z=0; z<200; z++) {
        			String header = "DELETE " + protocol_version + " " + unique_id + " " + file.getIdentifier() + endHeader;
        			System.out.println("DELETE " + protocol_version + " " + unique_id + " " + file.getIdentifier());

					SendMessage sender = new SendMessage(header.getBytes(), "MC");
					threadPool.execute(sender);
    			}
    			
        		for(int j=0; j<file.getChunks().size(); j++) {
        			Chunk chunkToDelete = file.getChunks().get(j);
        			storage.removeChunkOcurrence(file.getIdentifier(), chunkToDelete.getChunkNo());
        		}
        		
        		storage.getFiles().remove(i);
        		break;
    		}
    	}
    }
    
    @Override
    public void reclaim(int space) throws RemoteException {
    	
    	int kByteSpaceToByte = space * 1000;
    	
    	System.out.println("OLD STORAGE SPACE " + storage.getSpace());
    	
    	// difference between available space and space to reclaim
    	int storageSpace = storage.getSpace();
    	int deltaSpace = storageSpace - kByteSpaceToByte;
    	
    	// there is more space to reclaim than what is available
    	if(deltaSpace < 0) {
    		System.err.println("ERROR: Specified more space to reclaim than what is possible.");
    		return;
    	}
    	
    	else if(deltaSpace > 0) {
    		int deletedSpaceSoFar = 0;
    		
    		Iterator<Chunk> chunkIter = storage.getStoredChunks().iterator();
    		
    		int cap = 0;
    		
    		if(deltaSpace == storageSpace) cap = storageSpace;
    		else cap = kByteSpaceToByte;
    		
    		Chunk chunk = null;
    		
    		while(deletedSpaceSoFar < cap && chunkIter.hasNext()) {
    		    chunk = chunkIter.next();
    			deletedSpaceSoFar += chunk.getSize();
		
    			reclaim(chunk, storageSpace, deletedSpaceSoFar);
                
                chunkIter.remove();
    		}	
    	}
    	
    	System.out.println("NEW STORAGE SPACE " + storage.getSpace());
    }
    
    @Override
    public void retrieveStateInfo() throws RemoteException {
    	System.out.println("RETRIEVING INFO FOR PEER " + unique_id);
    	
    	System.out.println("FILES WHOSE BACKUP IT INITIATED:");
    	
    	for(int i=0; i < storage.getFiles().size(); i++) {
    		FileContent file = storage.getFiles().get(i);
    		
    		System.out.println("-------------------------");
    		
    		System.out.println("FILE PATHNAME: " + file.getFile().getPath());
    		System.out.println("BACKUP SERVICE ID OF THE FILE: " + file.getIdentifier());
    		System.out.println("DESIRED REPLICATION DEGREE: " + file.getReplicationDegree());
    		
    		for(int j = 0; j < file.getChunks().size(); j++) {
    			Chunk chunk = file.getChunks().get(j);			
                System.out.println("CHUNK ID: " + chunk.getFileId() + " " + chunk.getChunkNo());
                String uniqueChunkIdentifier = file.getIdentifier() + "/" + "chunk" + chunk.getChunkNo();
                System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + storage.getChunkOccurences().get(uniqueChunkIdentifier));
    		}
    		
    		System.out.println("-------------------------");
    	}
    	
    	System.out.println("FOR EACH CHUNK IT STORES:");
		
		for(int j = 0; j < storage.getStoredChunks().size(); j++) {
			
			System.out.println("-------------------------");
			
			Chunk chunk = storage.getStoredChunks().get(j);			
            System.out.println("CHUNK ID: " + chunk.getFileId() + " " + chunk.getChunkNo());
            
            System.out.println("CHUNK SIZE (KB): " + convertKByte(chunk.getSize()));
            
            String uniqueChunkIdentifier = chunk.getFileId() + "/" + "chunk" + chunk.getChunkNo();
            System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + storage.getChunkOccurences().get(uniqueChunkIdentifier));
            
            System.out.println("-------------------------");
		}
			
		System.out.println("STORAGE CAPACITY (KB): " + convertKByte(storage.getSpace()));
		System.out.println("AMOUNT OF STORAGE USED TO BACKUP CHUNKS (KB): " + convertKByte(storage.occupiedChunkSpace()));
    }
    
    public double convertKByte(int num) {
    	return  (double) num/1000;
    }
    
    public void reclaim(Chunk chunk, int spaceStorage, int deletedSpace) {
		String fileId = chunk.getFileId();
		int chunkNo = chunk.getChunkNo();
		
		String uniqueChunkIdentifier = fileId + "/" + "chunk" + chunkNo;
		
		storage.getBlackListedChunks().put(uniqueChunkIdentifier, unique_id);
		
		String header = "REMOVED " + protocol_version + " " + unique_id + " " + fileId + " " + chunkNo + endHeader;
        System.out.println("Sent " + "REMOVED " + protocol_version + " " + unique_id + " " + fileId + " " + chunkNo);
        
        byte[] headerASCII = header.getBytes();
        SendMessage message = new SendMessage(headerASCII, "MC");
        threadPool.execute(message);
        
        // delete on disk
        String filename = "peer" + Peer.getUniqueId() + "/" + "backup" + "/" + fileId + "/" + "chunk" + chunkNo;
        File chunkFile = new File(filename);
        chunkFile.delete();
        
        // delete on storage
        Peer.getStorage().removeChunkOcurrence(fileId, chunkNo);
        
        if(storage.getSpace() + deletedSpace <= STORAGE_MAX_SIZE) {        	
            storage.setSpace(spaceStorage + deletedSpace);
        }
    }

}