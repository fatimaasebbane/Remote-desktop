import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientUI extends JFrame implements MouseMotionListener {
    private JLabel screenLabel;
    private Timer timer;
    private RemoteInterface server;


    public ClientUI() throws RemoteException {

        setTitle("Remote Desktop Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        screenLabel = new JLabel();
        add(screenLabel, BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(null);

        // Connexion au serveur RMI :192.168.137.1
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            server = (RemoteInterface) registry.lookup("remoteDesktopServer");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        // Démarrer le rafraîchissement périodique de l'écran
        startScreenRefresh();

        // Ajouter un écouteur de mouvement de souris à la fenêtre
        addMouseMotionListener(this);

        //Envoyer l'action de clic de souris au serveur
        screenLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    // Envoyer l'action de clic de souris pressé au serveur
                    server.mousePressed(e.getX(), e.getY(), e.getButton());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    // Envoyer l'action de clic de souris relâché au serveur
                    server.mouseReleased(e.getX(), e.getY(), e.getButton());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }



    private void startScreenRefresh() {
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshScreen();
            }
        };
        timer = new Timer(1, taskPerformer); // Rafraîchir toutes les 1 ms
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
            double[] mouseCoordinates = {e.getX() / (double) getWidth(), e.getY() / (double) getHeight()};
            server.receiveMouseEvent(mouseCoordinates);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }


}
