import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;

public class ServerImpl extends UnicastRemoteObject implements RemoteInterface {
    private Robot robot;
    private Timer timer;
    RemoteInterface client;

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
    public int[] sendMouseEvent() throws RemoteException {
        // Capturer les positions de la souris
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        int mouseX = (int) mousePosition.getX();
        int mouseY = (int) mousePosition.getY();

        // Créer un tableau contenant les coordonnées X et Y de la souris
        int[] mouseCoordinates = {mouseX, mouseY};

        // Renvoyer les coordonnées de la souris au client
        return mouseCoordinates;
    }
}
