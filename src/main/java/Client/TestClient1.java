package Client;

import javax.swing.*;
import java.rmi.RemoteException;
/**
 * Classe de test pour le client de l'interface utilisateur.
 */
public class TestClient1 {
    /**
     * Point d'entrée du programme.
     *
     * @param args les arguments de la ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        // Utilisation de SwingUtilities pour exécuter la création de l'interface utilisateur dans le thread d'interface Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Crée une nouvelle instance de ClientUI et la rend visible
                new ClientUI().setVisible(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
