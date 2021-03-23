import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerCommands extends Remote {
    void backupFile(String fileName, byte[] fileContents, int replicationDegree) throws RemoteException;
    byte[] restoreFile(String fileName) throws RemoteException;
    void deleteFile(String fileName) throws RemoteException;
    void reclaimSpace(int space) throws RemoteException;
    String state() throws RemoteException;
}
