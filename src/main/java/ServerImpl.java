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
    public void sendMousePosition(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    @Override
    public void sendMouseClick(int buttonMask) throws RemoteException {
        switch (buttonMask) {
            case MouseEvent.BUTTON1_DOWN_MASK:
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                break;
            case MouseEvent.BUTTON2_DOWN_MASK:
                robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                break;
            case MouseEvent.BUTTON3_DOWN_MASK:
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                break;
            default:
                break;
        }
    }
}
