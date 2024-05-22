package Service;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.InputEvent;
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
    private TargetDataLine microphone;
    private boolean isPlayingMedia = false;



    protected ServerImpl() throws RemoteException {
            try {
                robot = new Robot();
                generatePassword();
                initMicrophone();
                startMediaDetection();
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
    private void initMicrophone() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
    }
    private void startMediaDetection() {
        new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    // Analyse the buffer to detect if media is playing
                    boolean mediaPlaying = isMediaPlaying(buffer, bytesRead);
                    try {
                        setPlayingMedia(mediaPlaying);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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

    public void clickMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }


    @Override
    public void keyPressed(int keyCode) throws RemoteException {
        System.out.println("Key pressed: " + keyCode);
        try {
            Robot robot = new Robot();
            robot.keyPress(keyCode);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid key code: " + keyCode);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
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
            return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            throw new RemoteException("Error reading file: " + e.getMessage());
        }
    }
    @Override
    public boolean isMediaPlaying(byte[] buffer, int bytesRead) {
        // Simple heuristic: if the average volume level is above a threshold, assume media is playing
        long sum = 0;
        for (int i = 0; i < bytesRead; i += 2) {
            int sample = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
            sum += Math.abs(sample);
        }
        double average = sum / (bytesRead / 2.0);
        return average > 1000;
    }

    @Override
    public boolean isPlayingMedia() throws RemoteException {
        return isPlayingMedia;
    }

    @Override
    public void setPlayingMedia(boolean playing) throws RemoteException {
        this.isPlayingMedia = playing;
    }

    @Override
    public byte[] captureAudioChunk() throws RemoteException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = microphone.read(buffer, 0, buffer.length);
        if (bytesRead > 0) {
            out.write(buffer, 0, bytesRead);
        }
        return out.toByteArray();
    }

}
