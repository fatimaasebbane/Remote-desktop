import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void receiveMouseEvent(double[] mouseCoordinates) throws RemoteException;
    void mousePressed(int x, int y, int button) throws RemoteException;
    void mouseReleased(int x, int y, int button) throws RemoteException;
    boolean checkPassword(String password) throws RemoteException;
}