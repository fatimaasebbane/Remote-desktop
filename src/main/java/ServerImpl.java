import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements RemoteInterface {
    private Robot robot;

    protected ServerImpl() throws RemoteException {
            try {
                robot = new Robot();
            } catch (Exception e) {
                throw new RemoteException("Failed to initialize Robot", e);
            }

    }

    @Override
    public byte[] captureScreen() throws RemoteException {
        try {
            // Capture de l'écran à l'aide de la classe Robot
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenImage = robot.createScreenCapture(screenRect);

            // Convertir l'image en tableau d'octets pour l'envoyer via RMI
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(screenImage, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            System.err.println("Error capturing screen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void receiveMouseEvent(double[] mouseCoordinates) throws RemoteException {
        // Obtenir la taille de l'écran du serveur
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        // Obtenir les coordonnées reçues
        double receivedX = mouseCoordinates[0];
        double receivedY = mouseCoordinates[1];

        // Convertir les coordonnées reçues en fonction de la taille de l'écran du serveur
        int x = (int) (receivedX * screenWidth);
        int y = (int) (receivedY * screenHeight);

        // Déplacer la souris sur le bureau du serveur
        robot.mouseMove(x, y);
    }

    @Override
    public void mousePressed(int x, int y, int button) throws RemoteException {

        // Déplacez la souris à la position spécifiée
        robot.mouseMove(x, y);

        // Effectuez le clic de souris pressé
        int inputEvent;
        switch (button) {
            case MouseEvent.BUTTON1:
                inputEvent = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON2:
                inputEvent = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                inputEvent = InputEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                throw new IllegalArgumentException("Button not recognized: " + button);
        }
        robot.mousePress(inputEvent);
    }

    @Override
    public void mouseReleased(int x, int y, int button) throws RemoteException {
     // Déplacez la souris à la position spécifiée
        robot.mouseMove(x, y);

        // Effectuez le clic de souris relâché
        int inputEvent;
        switch (button) {
            case MouseEvent.BUTTON1:
                inputEvent = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON2:
                inputEvent = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                inputEvent = InputEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                throw new IllegalArgumentException("Button not recognized: " + button);
        }
        robot.mouseRelease(inputEvent);
    }
}
