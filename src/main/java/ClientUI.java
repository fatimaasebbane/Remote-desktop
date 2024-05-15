import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientUI extends JFrame implements MouseMotionListener {
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
            Registry registry = LocateRegistry.getRegistry("192.168.137.1", 1099);
            server = (RemoteInterface) registry.lookup("remoteDesktopServer");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        // Démarrer le rafraîchissement périodique de l'écran
        startScreenRefresh();

        // Ajouter un écouteur de mouvement de souris à la fenêtre
        addMouseMotionListener(this);

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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        try {
            // Récupérer les coordonnées de la souris et les envoyer au serveur
            double[] mouseCoordinates = {e.getX(), e.getY()};
            server.receiveMouseEvent(mouseCoordinates);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
}
