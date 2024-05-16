import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class ServerImpl extends UnicastRemoteObject implements RemoteInterface {
    private Robot robot;
    private String password;

    protected ServerImpl() throws RemoteException {
            try {
                robot = new Robot();
                generatePassword();
            } catch (Exception e) {
                throw new RemoteException("Failed to initialize Robot", e);
            }

    }
    // Méthode pour générer un mot de passe aléatoire
    private void generatePassword() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        password = sb.toString();
        System.out.printf("password :"+password);
    }

    // Méthode pour vérifier le mot de passe
    @Override
    public boolean checkPassword(String inputPassword) {
        return inputPassword.equals(password);
    }

    @Override
    public int getScreenWidth() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize().width;
    }

    @Override
    public int getScreenHeight() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize().height;
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
    public void receiveMouseEvent(int[] mouseCoordinates) throws RemoteException {
        this.robot.mouseMove( mouseCoordinates[0],  mouseCoordinates[1]);
    }

    @Override
    public void clickMouse(int x, int y) throws RemoteException {
            robot.mouseMove(x, y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

}
