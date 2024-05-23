package Client;

import javax.swing.*;
import java.rmi.RemoteException;

public class TestClient1 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ClientUI().setVisible(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}