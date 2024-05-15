import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void receiveMouseEvent(double[] mouseCoordinates) throws RemoteException;
}