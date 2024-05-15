import java.awt.event.MouseEvent;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    int[] sendMouseEvent() throws RemoteException;
}