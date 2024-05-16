import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientUI extends JFrame implements KeyListener, MouseListener, MouseMotionListener  {
    private JLabel screenLabel;
    private Timer timer;
    private RemoteInterface server;
    private String serverPassword;

    public ClientUI() throws RemoteException {

        setTitle("Remote Desktop Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Get the screen size of the client
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Set the frame size to 80% of the client's screen size
        int width = (int) (screenSize.width * 0.8);
        int height = (int) (screenSize.height * 0.8);
        setSize(width, height);

        // Center the frame on the screen
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        screenLabel = new JLabel();
        add(screenLabel, BorderLayout.CENTER);

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
            addKeyListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
        }
    }



    private boolean checkServerPassword(String password) throws RemoteException {
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
    public void mouseMoved(MouseEvent e) {
        try {
            int[] tab ={e.getX(), e.getY()};
            server.receiveMouseEvent(tab);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error moving cursor on remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            Point clickPoint = e.getPoint();
            int x = clickPoint.x;
            int y = clickPoint.y;

            int remoteWidth = server.getScreenWidth();
            int remoteHeight = server.getScreenHeight();
            int labelWidth = screenLabel.getWidth();
            int labelHeight = screenLabel.getHeight();

            double scaleX = (double) remoteWidth / labelWidth;
            double scaleY = (double) remoteHeight / labelHeight;

            int remoteX = (int) (x * scaleX);
            int remoteY = (int) (y * scaleY);

            remoteX = Math.max(0, Math.min(remoteX, remoteWidth - 1));
            remoteY = Math.max(0, Math.min(remoteY, remoteHeight - 1));

            server.clickMouse(remoteX, remoteY);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error sending mouse click to remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }


}
