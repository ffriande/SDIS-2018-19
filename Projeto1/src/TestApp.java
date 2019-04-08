import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

	public static void main(String[] args) {
		if (args.length != 3 && args.length != 4 && args.length != 5) {
            System.out.println("Syntax: TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2> ");
            return;
        }//TODO: tem  argumentos a mais isto. 
        //java TestApp 1923 BACKUP test1.pdf 3 - enunciad
		
        String peer_ap = args[1];
        String sub_protocol  = args[2];
        
        try {
        	
            Registry registry = LocateRegistry.getRegistry("localhost");
            RemoteInterface stub = (RemoteInterface) registry.lookup(peer_ap);
            
            if(sub_protocol.equals("BACKUP") && args.length == 5) {
            	
            	String filepath = args[3];
            	int replicationDegree = Integer.parseInt(args[4]);
            	
            	stub.backup(filepath, replicationDegree);
            	
                return;
            }
            
            else if(sub_protocol.equals("RESTORE") && args.length == 4) {
            	
            	String filepath = args[3];
            	
            	stub.restore(filepath);

                return;
            }
            
            else if(sub_protocol.equals("DELETE") && args.length == 4) {
            	
            	String filepath = args[3];
            	
            	stub.delete(filepath);
            	
            	return;
            }
            
            else if(sub_protocol.equals("RECLAIM") && args.length == 4) {
            	
            	int space = Integer.parseInt(args[3]);
            	
            	stub.reclaim(space);
            	
            	return;
            }
            
            else if(sub_protocol.equals("STATE") && args.length == 3) {
            	
            	stub.retrieveStateInfo();
            	
            	return;
            }
            
            else {
            	System.out.println("Invalid arguments. Gave operation " + sub_protocol + " with " + args.length + " arguments.");
            }
            
        } catch (IOException ex) {
            System.out.println("TestApp error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (NotBoundException e) {
        	System.out.println("RMI error: " + e.getMessage());
		}
        
    }
}