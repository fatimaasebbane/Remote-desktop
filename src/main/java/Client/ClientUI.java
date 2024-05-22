package Client;

import Client.Fonctionnalite.AudioClient;
import Client.Fonctionnalite.FileTransferHelper;
import Client.Fonctionnalite.UIHelper;
import Service.RemoteInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientUI extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    public JLabel screenLabel;
    private RemoteInterface server;
    private JFileChooser fileChooser;
    Timer timer;

    public ClientUI() throws RemoteException {
        connectToServer();
        initializeUI();
        startAudio();
    }

    private void connectToServer() {
        // Connexion au serveur RMI
        try {
            Registry registry = LocateRegistry.getRegistry("192.168.137.1", 1099);
            server = (RemoteInterface) registry.lookup("remoteDesktopServer");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        // Initialisation de l'interface utilisateur
        UIHelper.setupUI(this);
        setupMenuBar();
        addEventListeners();
    }

    public boolean checkServerPassword(String password) throws RemoteException {
        // Vérifier le mot de passe avec le serveur
        return server.checkPassword(password);
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

    private void setupMenuBar() {
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


    private void addEventListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }


    private void startAudio() {
        AudioClient audioClient = new AudioClient(server);
        new Thread(audioClient).start();
    }


    private class SendFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileTransferHelper.sendFile(ClientUI.this, fileChooser, server);
        }
    }

    private class ReceiveFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileTransferHelper.receiveFile(ClientUI.this, fileChooser, server);
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
}
