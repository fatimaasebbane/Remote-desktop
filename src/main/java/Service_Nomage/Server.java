package Service_Nomage;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            // Démarrage du registre RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099); // Remplacez par l'adresse IP de votre serveur central

            RemoteInterface server = new ServerImpl();

            String hostName = InetAddress.getLocalHost().getHostName();
            Naming.rebind("/client_" + hostName, server);

            System.out.println("Client registered as server with name: " + hostName);
        } catch (RemoteException | UnknownHostException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
