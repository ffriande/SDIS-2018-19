import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
	void backup(String path, int replicationDegree) throws RemoteException;
	void restore(String path) throws RemoteException;
	void delete(String path) throws RemoteException;
	void reclaim(int space) throws RemoteException;
	void retrieveStateInfo() throws RemoteException;
}