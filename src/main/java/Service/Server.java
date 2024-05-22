package Service;

import Service.RemoteInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            // Créez un registre RMI sur le port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Créez une instance de Service.ServerImpl et liez-la au registre RMI
            RemoteInterface server = new ServerImpl();

            registry.bind("remoteDesktopServer", server);
            System.out.println("Service.Server is running...");
        } catch (Exception e) {
            System.err.println("Service.Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
