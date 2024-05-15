import javax.imageio.ImageIO;
import java.awt.*;
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
        // Déplacer la souris sur le bureau du serveur
        int x = (int) mouseCoordinates[0];
        int y = (int) mouseCoordinates[1];
        robot.mouseMove(x, y);
    }
}
