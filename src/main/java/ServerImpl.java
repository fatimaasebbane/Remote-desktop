import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
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
        int mouseButton = 0;
        switch (button) {
            case 1:
                mouseButton = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case 2:
                mouseButton = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case 3:
                mouseButton = InputEvent.BUTTON3_DOWN_MASK;
                break;
        }
        robot.mousePress(mouseButton);
    }

    @Override
    public void mouseReleased(int button) throws RemoteException {
        int mouseButton = 0;
        switch (button) {
            case 1:
                mouseButton = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case 2:
                mouseButton = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case 3:
                mouseButton = InputEvent.BUTTON3_DOWN_MASK;
                break;
        }
        robot.mouseRelease(mouseButton);
    }

    @Override
    public Dimension getScreenSize() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    @Override
    public void clickMouse(int x, int y) throws RemoteException {
        return;
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
    public void mouseMoved(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    @Override
    public void dragMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }
    @Override
    public void typeKey(char keyChar) throws RemoteException {
       return;
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
            byte[] fileData = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileData);
            fis.close();
            return fileData;
        } catch (IOException e) {
            throw new RemoteException("Erreur lors de la lecture du fichier", e);
        }
    }
}
