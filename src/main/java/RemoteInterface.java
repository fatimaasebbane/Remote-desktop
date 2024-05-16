import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void receiveMouseEvent(int[] mouseCoordinates) throws RemoteException;
    void clickMouse(int x, int y) throws RemoteException;
    boolean checkPassword(String password) throws RemoteException;
    int getScreenWidth() throws RemoteException;
    int getScreenHeight() throws RemoteException;
}