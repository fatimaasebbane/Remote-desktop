import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            // Créez un registre RMI sur le port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Créez une instance de ServerImpl et liez-la au registre RMI
            RemoteInterface server = new ServerImpl();
            //String adress = String.valueOf(InetAddress.getByName("192.168.137.1"));

            registry.bind("remoteDesktopServer", server);
            System.out.println("Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
