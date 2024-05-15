import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientUI extends JFrame {
    private JLabel screenLabel;
    private Timer timer;
    private RemoteInterface server;
    private Robot robot;

    public ClientUI() throws RemoteException {
        // Initialisation de la classe Robot pour reproduire les événements de souris
        try {
             robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Error creating Robot: " + e.getMessage());
            e.printStackTrace();
        }

        setTitle("Remote Desktop Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        screenLabel = new JLabel();
        add(screenLabel, BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(null);

        // Connexion au serveur RMI
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            server = (RemoteInterface) registry.lookup("Server");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        // Démarrer le rafraîchissement périodique de l'écran
        startScreenRefresh();

        // Démarrer le rafraîchissement périodique de la souris
        refreshMouse();

    }

    private void startScreenRefresh() {
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshScreen();
            }
        };
        timer = new Timer(1, taskPerformer); // Rafraîchir toutes les 1 seconde
        timer.start();
    }

    private void refreshScreen() {
        try {
           // Récupération de l'écran distant
            byte[] imageData = server.captureScreen();
            if (imageData != null) {
                // Convertir les données d'image en ImageIcon
                ImageIcon imageIcon = new ImageIcon(imageData);

                // Redimensionner l'image capturée en fonction de la taille de l'écran du client
                int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
                int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
                Image scaledImage = imageIcon.getImage().getScaledInstance(screenWidth, screenHeight, Image.SCALE_SMOOTH);
                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);

                // Afficher l'image redimensionnée dans JLabel
                screenLabel.setIcon(scaledImageIcon);
                // Rafraîchir la fenêtre pour afficher la nouvelle image
                revalidate();
                repaint();

            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
    public void refreshMouse() throws RemoteException {
        new Thread(() -> {
            while (true) {
                try {
                    // Récupérer les coordonnées de la souris du serveur
                    int[] mouseCoordinates = server.sendMouseEvent();
                    // Afficher les coordonnées de la souris dans la console (à des fins de débogage)
                    System.out.println("Mouse X: " + mouseCoordinates[0] + ", Mouse Y: " + mouseCoordinates[1]);
                    robot.mouseMove(mouseCoordinates[0],mouseCoordinates[1]);
                    // Pause pour éviter une surcharge inutile
                    Thread.sleep(1); // Ajustez selon vos besoins
                } catch (Exception e) {
                    System.err.println("Client exception: " + e.toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }



}
