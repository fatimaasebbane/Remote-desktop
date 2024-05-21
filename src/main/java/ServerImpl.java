import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = sdf.format(new Date());

        // Ajouter des caractères alphanumérique au mot de passe
        for (int i = 0; i < timestamp.length(); i++) {
            char c = timestamp.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
            }
        }

        // Ajouter des caractères spéciaux
        sb.append("!@#$%");

        password = sb.toString();
        System.out.printf("Password: %s\n", password);
    }

    // Méthode pour vérifier le mot de passe
    @Override
    public boolean checkPassword(String inputPassword) {
        return inputPassword.equals(password);
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
    public void mousePressed(int button) throws RemoteException {
        robot.mousePress(button);
    }

    @Override
    public void mouseReleased(int button) throws RemoteException {
        robot.mouseRelease(button);
    }

    @Override
    public Dimension getScreenSize() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    @Override
    public void clickMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    @Override
    public void keyPressed(int keyCode) throws RemoteException {
        robot.keyPress(keyCode);
    }

    @Override
    public void keyReleased(int keyCode) throws RemoteException {
        robot.keyRelease(keyCode);
    }
    @Override
    public void moveCursor(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    @Override
    public void dragMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }
    @Override
    public void typeKey(char keyChar) throws RemoteException {
        // Simulate typing by pressing and releasing the corresponding key code
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(keyChar);
        keyPressed(keyCode);
        keyReleased(keyCode);
    }


    @Override
    public void sendFile(byte[] fileData, String fileName) throws RemoteException {
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileData);
            fos.close();
            System.out.println("File received: " + fileName);
        } catch (IOException e) {
            throw new RemoteException("Error saving file: " + e.getMessage());
        }
    }

    @Override
    public byte[] receiveFile(String fileName) throws RemoteException {
        try {
            File file = new File(fileName);
            return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            throw new RemoteException("Error reading file: " + e.getMessage());
        }
    }


}
