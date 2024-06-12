package Client;

import Client.Fonctionnalite.AudioClient;
import Client.Fonctionnalite.FileTransferHelper;
import Client.Fonctionnalite.UIHelper;
import Service_Nomage.RemoteInterface;
import Service_Nomage.ServerImpl;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * ClientUI est la classe principale pour l'interface utilisateur du client.
 * Elle permet à l'utilisateur d'agir comme un serveur ou de se connecter à un serveur.
 */
public class ClientUI extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    public static JPanel sidebarPanel, topPanel;
    public JLabel screenLabel;
    public static RemoteInterface selectedServer;
    private JFileChooser fileChooser;
    private JPanel panel = new JPanel();
    private Timer timer;
    private Registry registry;
    private String password;

    /**
     * Constructeur pour ClientUI.
     * @throws RemoteException en cas d'échec de la connexion au registre RMI.
     */
    public ClientUI() throws RemoteException {
        registry = LocateRegistry.getRegistry("localhost", 1099);
        fileChooser = new JFileChooser();
        initializeUI();
    }

    /**
     * Initialise l'interface utilisateur.
     */
    private void initializeUI() {
        setTitle("RemoteDesk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
        choicePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel choiceLabel = new JLabel("Choisissez votre role:");
        choiceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        choicePanel.add(choiceLabel);

        JButton actAsServerButton = new JButton("Act as Server");
        actAsServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actAsServerButton.addActionListener(e -> actAsServer());
        choicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        choicePanel.add(actAsServerButton);

        JButton connectToServerButton = new JButton("Connect to Server");
        connectToServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectToServerButton.addActionListener(e -> connectToServer());
        choicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        choicePanel.add(connectToServerButton);

        getContentPane().add(choicePanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Permet à l'utilisateur d'agir comme serveur.
     */
    private void actAsServer() {
        try {
            registerAsServer();
            getContentPane().removeAll();
            UIHelper.setupUI(this);
            // Modifier le contenu de la barre latérale
            updateSidebarForServerMode();
            //le bouton pour arrêter le serveur
            addStopServerButton();
            // le panneau vide pour pousser le contenu vers la droite
            JPanel leftEmptyPanel = new JPanel();
            leftEmptyPanel.setPreferredSize(new Dimension(100, 0));

            // le JPanel pour contenir le texte et le mot de passe
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

            // Ajouter le texte au centre de la page
            JLabel centerLabel = new JLabel("Voici le mot de passe généré pour le client.");
            centerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerLabel.setVerticalAlignment(SwingConstants.CENTER);
            centerLabel.setFont(new Font("Arial", Font.BOLD, 18));
            centerPanel.add(centerLabel);

             // Ajout de l'espace vertical
            centerPanel.add(Box.createVerticalStrut(80));

            // Récupération et affichage de mot de passe généré par le serveur
            JLabel passwordLabel = new JLabel(password);
            passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
            passwordLabel.setVerticalAlignment(SwingConstants.CENTER);
            passwordLabel.setFont(new Font("Arial", Font.BOLD, 50));
            centerPanel.add(passwordLabel);

            // le panneau principal
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(leftEmptyPanel, BorderLayout.WEST);
            mainPanel.add(centerPanel, BorderLayout.CENTER);
            getContentPane().add(mainPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
        } catch (Exception e) {
            System.err.println("Error setting up as server: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Connecte l'utilisateur à un serveur.
     */
    private void connectToServer() {
        try {
            getContentPane().removeAll();
            UIHelper.setupUI(this);
            displayAvailableServers();
            revalidate();
            repaint();
        } catch (Exception e) {
            System.err.println("Error displaying available servers: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour la barre latérale pour le mode serveur.
     */
    private void updateSidebarForServerMode() {
        ClientUI.sidebarPanel.removeAll();
        JLabel serverStatusLabel = new JLabel("Le serveur est en cours d'exécution...");
        Font font = new Font("Arial", Font.BOLD, 18); // Modifier la police ici
        serverStatusLabel.setFont(font);
        ClientUI.sidebarPanel.add(serverStatusLabel);

        JTextArea serverInstructionsArea = new JTextArea("\n \n 1. Donnez le mot de passe généré au client.\n\n 2. Le client doit se connecter à cette votre bureaux à l'aide de ce password.");
        serverInstructionsArea.setEditable(false);
        serverInstructionsArea.setLineWrap(true);
        serverInstructionsArea.setWrapStyleWord(true);
        Font sidebarFont = new Font("Arial", Font.PLAIN, 17);
        serverInstructionsArea.setFont(sidebarFont);
        ClientUI.sidebarPanel.add(new JScrollPane(serverInstructionsArea));
    }


    /**
     * Ajoute un bouton pour arrêter le serveur.
     */
    private void addStopServerButton() {
        JButton stopServerButton = new JButton("Arrêter le serveur");
        stopServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopServerButton.addActionListener(e -> stopServer());
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(stopServerButton);
    }

    /**
     * Arrête le serveur.
     */
    private void stopServer() {
        try {
            registry.unbind("client_" + getClientIPAddress());
            System.out.println("Le serveur a été arrêté.");
            JOptionPane.showMessageDialog(this, "Le serveur a été arrêté.", "Serveur arrêté", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error stopping server: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Enregistre le client en tant que serveur.
     */
    private void registerAsServer() {
        try {
            RemoteInterface serverImpl = new ServerImpl();
            registry.rebind("client_" + getClientIPAddress(), serverImpl);
            System.out.println("Client registered as server.");
            password = serverImpl.generatePassword();
        } catch (Exception e) {
            System.err.println("Error registering client as server: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Obtient l'adresse IP du client.
     * @return l'adresse IP du client.
     * @throws UnknownHostException en cas d'échec de la résolution de l'adresse IP.
     */
    private String getClientIPAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    /**
     * Affiche les serveurs disponibles.
     */
    private void displayAvailableServers() {
        try {
            String[] serverNames = registry.list();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

            JLabel label = new JLabel("Liste des serveurs disponibles :");
            Font font = new Font("Arial", Font.BOLD, 20);
            label.setFont(font);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(label);

            JComboBox<String> serverList = new JComboBox<>(serverNames);
            serverList.setPreferredSize(new Dimension(500, 30));
            serverList.setMaximumSize(new Dimension(500, 30));
            serverList.setAlignmentX(Component.CENTER_ALIGNMENT);
            serverList.addActionListener(e -> {
                String selectedServerName = (String) serverList.getSelectedItem();
                connectToSelectedServer(selectedServerName);
            });
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(serverList);

            getContentPane().add(panel, BorderLayout.EAST);
            revalidate();
            repaint();
        } catch (Exception e) {
            System.err.println("Error displaying available servers: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Connecte l'utilisateur au serveur sélectionné.
     * @param selectedServerName le nom du serveur sélectionné.
     */
    private void connectToSelectedServer(String selectedServerName) {
        try {
            selectedServer = (RemoteInterface) registry.lookup(selectedServerName);
            check();
        } catch (Exception e) {
            System.err.println("Error connecting to selected server: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Vérifie la permission du serveur.
     */
    private void check() {
        if (!requestPermission()) {
            JOptionPane.showMessageDialog(this, "Incorrect server password. Exiting...");
            System.exit(0);
        } else {
            sidebarPanel.setVisible(false);
            topPanel.setVisible(false);
            panel.setVisible(false);
            startScreenRefresh();
            addEventListeners();
            setupMenuBar();
            //startAudio();
        }
    }
    /**
     * Démarre l'audio.
     */
    private void startAudio() {
        AudioClient audioClient = new AudioClient(selectedServer);
        Thread audioThread = new Thread(audioClient);
        audioThread.start();
    }

    /**
     * Demande la permission du serveur en vérifiant le mot de passe.
     * @return true si le mot de passe est correct, sinon false.
     */
    public boolean requestPermission() {
        String password = JOptionPane.showInputDialog(this, "Enter password for server:");
        try {
            return selectedServer.checkPassword(password);
        } catch (RemoteException e) {
            System.err.println("Error checking password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Démarre le rafraîchissement de l'écran à intervalles réguliers.
     */
    public void startScreenRefresh() {
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshScreen();
            }
        };
        timer = new Timer(100, taskPerformer);
        timer.start();
    }

    private void refreshScreen() {
        try {
            byte[] screenData = selectedServer.captureScreen();
            ByteArrayInputStream bais = new ByteArrayInputStream(screenData);
            BufferedImage image = ImageIO.read(bais);

            if (screenLabel.getWidth() > 0 && screenLabel.getHeight() > 0) {
                Image scaledImage = image.getScaledInstance(screenLabel.getWidth(), screenLabel.getHeight(), Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                screenLabel.setIcon(icon);
            }
            repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error capturing remote screen: " + e.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    /**
     * Configure la barre de menu.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Menu");
        JMenuItem sendFileMenuItem = new JMenuItem("Envoyer un fichier");
        sendFileMenuItem.addActionListener(new SendFileAction());
        JMenuItem receiveFileMenuItem = new JMenuItem("Recevoir un fichier");
        receiveFileMenuItem.addActionListener(new ReceiveFileAction());
        fileMenu.add(sendFileMenuItem);
        fileMenu.add(receiveFileMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Ajoute les écouteurs d'événements de souris et de clavier.
     */
    private void addEventListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    /**
     * ActionListener pour l'envoi de fichier.
     */
    private class SendFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileTransferHelper.sendFile(ClientUI.this, fileChooser, selectedServer);
        }
    }

    /**
     * ActionListener pour la réception de fichier.
     */
    private class ReceiveFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileTransferHelper.receiveFile(ClientUI.this, fileChooser, selectedServer);
        }
    }

    /**
     * Mappe la position locale du curseur à la position distante.
     * @param localCursor la position locale du curseur.
     * @param localScreen la dimension de l'écran local.
     * @param remoteScreen la dimension de l'écran distant.
     * @return la position distante du curseur.
     */
    private Point mapLocalToRemoteCursor(Point localCursor, Dimension localScreen, Dimension remoteScreen) {
        double relativeX = (double) localCursor.x / localScreen.width;
        double relativeY = (double) localCursor.y / localScreen.height;

        int remoteX = (int) (relativeX * remoteScreen.width);
        int remoteY = (int) (relativeY * remoteScreen.height);

        return new Point(remoteX, remoteY);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            selectedServer.keyPressed(e.getKeyCode());
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error sending key press to remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            selectedServer.keyReleased(e.getKeyCode());
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error sending key release to remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        return;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        try {
            int button = e.getButton();
            if (button == MouseEvent.BUTTON1) {
                selectedServer.mousePressed(1);
            } else if (button == MouseEvent.BUTTON2) {
                selectedServer.mousePressed(2);
            } else if (button == MouseEvent.BUTTON3) {
                selectedServer.mousePressed(3);
            } else {

            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error sending mouse press to remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        try {
            int button = e.getButton();
            if (button == MouseEvent.BUTTON1) {
                selectedServer.mouseReleased(1);
            } else if (button == MouseEvent.BUTTON2) {
                selectedServer.mouseReleased(2);
            } else if (button == MouseEvent.BUTTON3) {
                selectedServer.mouseReleased(3);
            } else {

            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error sending mouse release to remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        try {
            Point movePoint = e.getPoint();
            Insets insets = getInsets();
            movePoint.translate(-insets.left, -insets.top - getRootPane().getHeight() + screenLabel.getHeight());

            Dimension localSize = screenLabel.getSize();
            Dimension remoteSize = selectedServer.getScreenSize();

            Point remoteMovePoint = mapLocalToRemoteCursor(movePoint, localSize, remoteSize);
            selectedServer.mouseMoved(remoteMovePoint.x, remoteMovePoint.y);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error moving cursor on remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        try {
            Point dragPoint = e.getPoint();
            Insets insets = getInsets();
            dragPoint.translate(-insets.left, -insets.top - getRootPane().getHeight() + screenLabel.getHeight());

            Dimension localSize = screenLabel.getSize();
            Dimension remoteSize = selectedServer.getScreenSize();

            Point remoteDragPoint = mapLocalToRemoteCursor(dragPoint, localSize, remoteSize);
            selectedServer.dragMouse(remoteDragPoint.x, remoteDragPoint.y);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error dragging cursor on remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
