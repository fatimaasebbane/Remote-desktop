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
    private String serverPassword;

    public ClientUI() throws RemoteException {

        setTitle("Remote Desktop Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        screenLabel = new JLabel();
        add(screenLabel, BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(null);

        // Demander à l'utilisateur le mot de passe du serveur
        serverPassword = JOptionPane.showInputDialog("Enter the server password:");

        // Connexion au serveur RMI
        try {
            Registry registry = LocateRegistry.getRegistry("192.168.137.1", 1099);
            server = (RemoteInterface) registry.lookup("remoteDesktopServer");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

        // Vérifier le mot de passe avec le serveur
        if (!checkServerPassword(serverPassword)) {
            JOptionPane.showMessageDialog(this, "Incorrect server password. Exiting...");
            System.exit(0);
        }else{
            // Démarrer le rafraîchissement périodique de l'écran
            startScreenRefresh();

            // Ajouter un écouteur de mouvement de souris à l'étiquette screenLabel
            screenLabel.addMouseMotionListener(this);
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
        }



    private boolean checkServerPassword(String password) {
        // Vérifier le mot de passe avec le serveur
        return server.checkPassword(password);
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
            double[] mouseCoordinates = {e.getX(), e.getY() };
            server.receiveMouseEvent(mouseCoordinates);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }



}
