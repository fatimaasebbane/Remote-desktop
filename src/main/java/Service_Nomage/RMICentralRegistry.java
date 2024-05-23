package Service_Nomage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMICentralRegistry {
    public static void main(String[] args) {
        try {
            // Démarrer le registre RMI sur le port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry started on port 1099");
            // Utilisation d'une boucle infinie pour garder le programme actif
            synchronized (RMICentralRegistry.class) {
                RMICentralRegistry.class.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}