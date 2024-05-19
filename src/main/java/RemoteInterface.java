import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void receiveMouseEvent(int[] mouseCoordinates) throws RemoteException;
    void clickMouse(int x, int y) throws RemoteException;
    boolean checkPassword(String password) throws RemoteException;
    int getScreenWidth() throws RemoteException;
    int getScreenHeight() throws RemoteException;
    void mousePressed(int x, int y, int button) throws RemoteException;
    void mouseReleased(int x, int y, int button) throws RemoteException;
    void keyPressed(int keyCode) throws RemoteException;
    void keyReleased(int keyCode) throws RemoteException;
    void sendFile(byte[] fileData, String fileName) throws RemoteException;
    byte[] receiveFile(String fileName) throws RemoteException;

}