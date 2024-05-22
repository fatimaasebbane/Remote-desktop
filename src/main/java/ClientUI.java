import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientUI extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    private Timer timer;
    private RemoteInterface server;
    private String serverPassword;
    private JFileChooser fileChooser;
    private JTextField passwordField;
    private JLabel screenLabel;
    private JPanel contentPanel;
    private JPanel centerPanel;
    private JPanel sidebarPanel;
    private double scaleX;
    private double scaleY;


    public ClientUI() throws RemoteException {
        ConnectToserver();
        initialise();
    }


    public void ConnectToserver(){
      // Connexion au serveur RMI
        try {
            Registry registry = LocateRegistry.getRegistry("192.168.137.1", 1099);
            server = (RemoteInterface) registry.lookup("remoteDesktopServer");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
    public void initialise(){

        Dimension localScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension remoteScreenSize = null;

        // Gestion des événements de souris et de clavier
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        if (remoteScreenSize != null) {
            double widthScale = localScreenSize.getWidth() / remoteScreenSize.getWidth();
            double heightScale = localScreenSize.getHeight() / remoteScreenSize.getHeight();
            double scale = Math.min(widthScale, heightScale);

            int width = (int) (remoteScreenSize.getWidth() * scale);
            int height = (int) (remoteScreenSize.getHeight() * scale);

            scaleX = scale;
            scaleY = scale;

            setSize(width, height);
        } else {
            setSize((int) (localScreenSize.getWidth() * 0.8), (int) (localScreenSize.getHeight() * 0.8));
        }

        setTitle("AnyDesk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        // Création du panneau de contenu
        contentPanel = new JPanel(new BorderLayout());
        setContentPane(contentPanel);

        // Création du panneau supérieur pour le logo et le nom de l'application
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel logoLabel = new JLabel(new ImageIcon("logo.png")); // Remplacez "logo.png" par le chemin de votre fichier logo
        JLabel appNameLabel = new JLabel("AnyDesk");
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(logoLabel);
        topPanel.add(appNameLabel);
        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Création du panneau central pour le champ de saisie du mot de passe
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JLabel passwordLabel = new JLabel("Enter Server Password:");
        passwordField = new JTextField(20);
        centerPanel.add(passwordLabel);
        centerPanel.add(passwordField);
        JButton connectButton = new JButton("Connect");
        connectButton.setBackground(new Color(0, 153, 204)); // Couleur de fond bleue
        connectButton.setForeground(Color.WHITE); // Texte en blanc
        connectButton.setFocusPainted(false); // Enlever le contour du bouton lorsqu'il est sélectionné
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverPassword = passwordField.getText();
                try {
                    if (!checkServerPassword(serverPassword)) {
                        JOptionPane.showMessageDialog(ClientUI.this, "Incorrect server password. Exiting...");
                        System.exit(0);
                    } else {
                        startScreenRefresh();
                        centerPanel.setVisible(false);
                        sidebarPanel.setVisible(false);
                        topPanel.setVisible(false);
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
        centerPanel.add(connectButton);
        contentPanel.add(centerPanel, BorderLayout.WEST);

        // Création de la barre latérale à droite
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebarPanel.add(new JLabel("Instructions:"));
        JTextArea instructionsArea = new JTextArea("1. Enter the code generated by the server.\n2. Click 'Connect' to start the remote session.");
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        sidebarPanel.add(new JScrollPane(instructionsArea));
        contentPanel.add(sidebarPanel, BorderLayout.EAST);

        // Initialisation du sélecteur de fichiers
        fileChooser = new JFileChooser();

        screenLabel = new JLabel();
        screenLabel.add(contentPanel, BorderLayout.CENTER);

        // Menu pour le transfert de fichiers
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Fichier");
        JMenuItem sendFileMenuItem = new JMenuItem("Envoyer un fichier");
        sendFileMenuItem.addActionListener(new SendFileAction());
        JMenuItem receiveFileMenuItem = new JMenuItem("Recevoir un fichier");
        receiveFileMenuItem.addActionListener(new ReceiveFileAction());
        fileMenu.add(sendFileMenuItem);
        fileMenu.add(receiveFileMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
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
        timer = new Timer(100, taskPerformer);
        timer.start();
    }

    private void refreshScreen() {
        try {
            byte[] screenData = server.captureScreen();
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
            server.keyPressed(e.getKeyCode());
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error sending key press to remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            server.keyReleased(e.getKeyCode());
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
                server.mousePressed(1);
            } else if (button == MouseEvent.BUTTON2) {
                server.mousePressed(2);
            } else if (button == MouseEvent.BUTTON3) {
                server.mousePressed(3);
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
                server.mouseReleased(1);
            } else if (button == MouseEvent.BUTTON2) {
                server.mouseReleased(2);
            } else if (button == MouseEvent.BUTTON3) {
                server.mouseReleased(3);
            } else {

            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error sending mouse release to remote screen: " + ex.getMessage(), "Remote Screen Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        return;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        try {
            Point movePoint = e.getPoint();
            Insets insets = getInsets();
            movePoint.translate(-insets.left, -insets.top - getRootPane().getHeight() + screenLabel.getHeight());

            Dimension localSize = screenLabel.getSize();
            Dimension remoteSize = server.getScreenSize();

            Point remoteMovePoint = mapLocalToRemoteCursor(movePoint, localSize, remoteSize);
            server.mouseMoved(remoteMovePoint.x, remoteMovePoint.y);
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
            Dimension remoteSize = server.getScreenSize();

            Point remoteDragPoint = mapLocalToRemoteCursor(dragPoint, localSize, remoteSize);
            server.dragMouse(remoteDragPoint.x, remoteDragPoint.y);
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

    private class SendFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = fileChooser.showOpenDialog(ClientUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    byte[] fileData = Files.readAllBytes(Paths.get(filePath));
                    server.sendFile(fileData, fileChooser.getSelectedFile().getName());
                    JOptionPane.showMessageDialog(ClientUI.this, "Fichier envoyé avec succès !");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ClientUI.this, "Erreur lors de l'envoi du fichier : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }
    private class ReceiveFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = fileChooser.showSaveDialog(ClientUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    String fileName = fileChooser.getSelectedFile().getName();
                    byte[] fileData = server.receiveFile(fileName);
                    FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile());
                    fos.write(fileData);
                    fos.close();
                    JOptionPane.showMessageDialog(ClientUI.this, "Fichier reçu avec succès !");
                } catch ( IOException ex) {
                    JOptionPane.showMessageDialog(ClientUI.this, "Erreur lors de la réception du fichier : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }
}