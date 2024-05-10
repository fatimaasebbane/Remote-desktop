import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientUI extends JFrame {
    private JLabel screenLabel;
    private Timer timer;
    private RemoteInterface server;

    public ClientUI() {

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

        // Ajouter un écouteur de mouvement de la souris
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                try {
                    server.sendMousePosition(e.getX(), e.getY());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Ajouter un écouteur pour les clics de souris
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    int button = e.getButton();
                    if (button == MouseEvent.BUTTON1) {
                        server.sendMouseClick(MouseEvent.BUTTON1_DOWN_MASK);
                    } else if (button == MouseEvent.BUTTON2) {
                        server.sendMouseClick(MouseEvent.BUTTON2_DOWN_MASK);
                    } else if (button == MouseEvent.BUTTON3) {
                        server.sendMouseClick(MouseEvent.BUTTON3_DOWN_MASK);
                    }
                } catch (Exception ex) {
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


}
