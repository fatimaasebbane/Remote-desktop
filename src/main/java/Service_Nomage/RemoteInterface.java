package Service_Nomage;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void clickMouse(int x, int y) throws RemoteException;
    boolean checkPassword(String password) throws RemoteException;
    void mousePressed(int button) throws RemoteException;
    void mouseReleased(int button) throws RemoteException;
    void keyPressed(int keyCode) throws RemoteException;
    void keyReleased(int keyCode) throws RemoteException;
    void mouseMoved(int x, int y) throws RemoteException;
    void dragMouse(int x, int y) throws RemoteException;
    void sendFile(byte[] fileData, String fileName) throws RemoteException;
    byte[] receiveFile(String fileName) throws RemoteException;
    Dimension getScreenSize() throws RemoteException;
    void typeKey(char keyChar) throws RemoteException;
    byte[] captureAudioChunk() throws RemoteException;

}