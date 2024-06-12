package Service_Nomage;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/**
 * Classe principale pour démarrer le serveur.
 */
public class Server {
    /**
     * Méthode principale pour démarrer le serveur.
     *
     * @param args les arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        try {
            // Démarrage du registre RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099); // Remplacez par l'adresse IP de votre serveur central
            // Création de l'implémentation du serveur
            RemoteInterface server = new ServerImpl();
            // Obtention du nom d'hôte
            String hostName = InetAddress.getLocalHost().getHostName();
            // Enregistrement de l'instance de serveur dans le registre RMI
            Naming.rebind("/client_" + hostName, server);

            System.out.println("\nClient registered as server with name: " + hostName);
        } catch (RemoteException | UnknownHostException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
