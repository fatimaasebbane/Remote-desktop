package Service_Nomage;

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

/**
 * Implémentation de l'interface distante {@link RemoteInterface}.
 * Cette classe fournit des fonctionnalités pour capturer l'écran, contrôler la souris et le clavier,
 * capturer l'audio et transférer des fichiers.
 */
public class ServerImpl extends UnicastRemoteObject implements RemoteInterface {
    private Robot robot;
    private String password;
    private TargetDataLine microphone;

    /**
     * Constructeur de la classe ServerImpl.
     *
     * @throws RemoteException si l'initialisation du robot ou du microphone échoue.
     */
    public ServerImpl() throws RemoteException {
            try {
                robot = new Robot();
                generatePassword();
                initMicrophone();
            } catch (Exception e) {
                throw new RemoteException("Failed to initialize Robot", e);
            }
    }

    /**
     * Génère un mot de passe aléatoire de 8 caractères et l'affiche sur la console.
     *
     * @return le mot de passe généré.
     * @throws RemoteException si une erreur survient lors de la génération du mot de passe.
     */
    public String generatePassword()  throws RemoteException {
        StringBuilder sb = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        for (int i = 0; i < 8; i++) {
            sb.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        sb.append("!@");
        password = sb.toString();
        System.out.printf("Password: %s\n", password);
        return  password;
    }

    /**
     * Vérifie si le mot de passe fourni correspond au mot de passe généré.
     *
     * @param inputPassword le mot de passe à vérifier.
     * @return true si le mot de passe est correct, false sinon.
     */
    @Override
    public boolean checkPassword(String inputPassword) {
        return inputPassword.equals(password);
    }

    /**
     * Capture l'écran et retourne l'image sous forme de tableau de bytes.
     *
     * @return un tableau de bytes représentant l'image de l'écran.
     * @throws RemoteException si une erreur survient lors de la capture de l'écran.
     */
    @Override
    public byte[] captureScreen() throws RemoteException {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenImage = robot.createScreenCapture(screenRect);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(screenImage, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            System.err.println("Error capturing screen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initialise le microphone pour la capture audio.
     *
     * @throws LineUnavailableException si le microphone n'est pas disponible.
     */
    private void initMicrophone() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
    }

    /**
     * Simule l'appui sur un bouton de la souris.
     *
     * @param button le bouton de la souris (1 pour gauche, 2 pour milieu, 3 pour droit).
     * @throws RemoteException si une erreur survient lors de la simulation.
     */
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

    /**
     * Simule le relâchement d'un bouton de la souris.
     *
     * @param button le bouton de la souris (1 pour gauche, 2 pour milieu, 3 pour droit).
     * @throws RemoteException si une erreur survient lors de la simulation.
     */
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
    /**
     * Retourne la taille de l'écran.
     *
     * @return la dimension de l'écran.
     * @throws RemoteException si une erreur survient lors de la récupération de la taille de l'écran.
     */
    @Override
    public Dimension getScreenSize() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Simule l'appui sur une touche du clavier.
     *
     * @param keyCode le code de la touche à simuler.
     * @throws RemoteException si une erreur survient lors de la simulation.
     */
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

    /**
     * Simule le relâchement d'une touche du clavier.
     *
     * @param keyCode le code de la touche à simuler.
     * @throws RemoteException si une erreur survient lors de la simulation.
     */
    @Override
    public void keyReleased(int keyCode) throws RemoteException {
        robot.keyRelease(keyCode);
    }

    /**
     * Simule le déplacement de la souris à une position donnée.
     *
     * @param x la coordonnée x de la nouvelle position de la souris.
     * @param y la coordonnée y de la nouvelle position de la souris.
     * @throws RemoteException si une erreur survient lors de la simulation.
     */
    @Override
    public void mouseMoved(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    /**
     * Simule le déplacement de la souris tout en maintenant un bouton enfoncé.
     *
     * @param x la coordonnée x de la nouvelle position de la souris.
     * @param y la coordonnée y de la nouvelle position de la souris.
     * @throws RemoteException si une erreur survient lors de la simulation.
     */
    @Override
    public void dragMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    /**
     * Simule la frappe d'une touche du clavier.
     *
     * @param keyChar le caractère de la touche à simuler.
     * @throws RemoteException si une erreur survient lors de la simulation.
     */
    @Override
    public void typeKey(char keyChar) throws RemoteException {
       return;
    }

    /**
     * Reçoit un fichier sous forme de tableau de bytes et l'enregistre dans le dossier de téléchargement de l'utilisateur.
     *
     * @param fileData les données du fichier à enregistrer.
     * @param fileName le nom du fichier à enregistrer.
     * @throws RemoteException si une erreur survient lors de l'enregistrement du fichier.
     */
    @Override
    public void sendFile(byte[] fileData, String fileName) throws RemoteException {
        try {
            // Obtenir le chemin vers le dossier de téléchargement
            String userHome = System.getProperty("user.home");
            String downloadsDir = userHome + File.separator + "Downloads";

            // Créer le fichier dans le dossier de téléchargement
            File file = new File(downloadsDir, fileName);

            // Écrire les données du fichier dans le fichier
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
            }

            System.out.println("File received and saved in: " + downloadsDir + File.separator + fileName);
        } catch (IOException e) {
            throw new RemoteException("Error saving file: " + e.getMessage());
        }
    }


    /**
     * Lit un fichier et retourne son contenu sous forme de tableau de bytes.
     *
     * @param fileName le nom du fichier à lire.
     * @return un tableau de bytes représentant le contenu du fichier.
     * @throws RemoteException si une erreur survient lors de la lecture du fichier.
     */
    @Override
    public byte[] receiveFile(String fileName) throws RemoteException {
        try {
            File file = new File(fileName);
            return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            throw new RemoteException("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Capture un morceau d'audio et retourne son contenu sous forme de tableau de bytes.
     *
     * @return un tableau de bytes représentant le morceau d'audio capturé.
     * @throws RemoteException si une erreur survient lors de la capture de l'audio.
     */
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
