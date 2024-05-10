import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void sendMousePosition(int x, int y) throws RemoteException;
    void sendMouseClick(int button3DownMask) throws RemoteException;
}