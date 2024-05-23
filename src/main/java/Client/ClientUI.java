package Client;

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

public class ClientUI extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    public static JPanel sidebarPanel,topPanel;
    public JLabel screenLabel;
    public static RemoteInterface selectedServer;
    private JFileChooser fileChooser;
    JPanel panel = new JPanel();
    Timer timer;
    Registry registry = LocateRegistry.getRegistry("localhost", 1099);

    public ClientUI() throws RemoteException {
        fileChooser = new JFileChooser();
        initializeUI();
    }


    private void initializeUI() {
        UIHelper.setupUI(this);
        setupMenuBar();
        //registerAsServer();
        displayAvailableServers();
    }

    private void registerAsServer() {
        try {
            // Créez une instance de votre implémentation distante ServerImpl
            RemoteInterface serverImpl = new ServerImpl();
            registry.bind("client_" + getClientIPAddress(), serverImpl);
            System.out.println("Client registered as server.");
        } catch (Exception e) {
            System.err.println("Error registering client as server: " + e.toString());
            e.printStackTrace();
        }
    }

    private String getClientIPAddress() throws UnknownHostException {
        String hostName = InetAddress.getLocalHost().getHostName();
        return hostName;
    }

    private void displayAvailableServers() {
        try {
            String[] serverNames = registry.list();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

            JLabel label = new JLabel("Liste des serveurs disponibles :");
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

            getContentPane().add(panel, BorderLayout.NORTH);
            revalidate();
            repaint();
        } catch (Exception e) {
            System.err.println("Error displaying available servers: " + e.toString());
            e.printStackTrace();
        }
    }

    private void connectToSelectedServer(String selectedServerName) {
        try {
            selectedServer = (RemoteInterface) registry.lookup(selectedServerName);
            check();
        } catch (Exception e) {
            System.err.println("Error connecting to selected server: " + e.toString());
            e.printStackTrace();
        }
    }
    private void check(){
        if (!requestPermission()) {
            JOptionPane.showMessageDialog(this,"Incorrect server password. Exiting...");
            System.exit(0);
        } else {
            sidebarPanel.setVisible(false);
            topPanel.setVisible(false);
            panel.setVisible(false);
            startScreenRefresh();
            addEventListeners();
            //startAudio();
        }
    }
    public boolean requestPermission() {
        String password = JOptionPane.showInputDialog(this, "Enter password for server :");
        try {
            return selectedServer.checkPassword(password);
        } catch (RemoteException e) {
            System.err.println("Error checking password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

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


    private void addEventListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    private class SendFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileTransferHelper.sendFile(ClientUI.this, fileChooser, selectedServer);
        }
    }

    private class ReceiveFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileTransferHelper.receiveFile(ClientUI.this, fileChooser, selectedServer);
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
