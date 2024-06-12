package Client.Fonctionnalite;

import Service_Nomage.RemoteInterface;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * FileTransferHelper est une classe utilitaire pour gérer l'envoi et la réception de fichiers entre le client et le serveur.
 */
public class FileTransferHelper {
    /**
     * Envoie un fichier sélectionné par l'utilisateur au serveur.
     *
     * @param parentFrame le cadre parent pour afficher les dialogues
     * @param fileChooser le sélecteur de fichiers pour choisir le fichier à envoyer
     * @param server l'interface distante du serveur pour envoyer le fichier
     */
    public static void sendFile(JFrame parentFrame, JFileChooser fileChooser, RemoteInterface server) {
        int result = fileChooser.showOpenDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                byte[] fileData = Files.readAllBytes(Paths.get(filePath));
                server.sendFile(fileData, fileChooser.getSelectedFile().getName());
                JOptionPane.showMessageDialog(parentFrame, "Fichier envoyé avec succès !");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Erreur lors de l'envoi du fichier : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Reçoit un fichier du serveur et le sauvegarde à un emplacement choisi par l'utilisateur.
     *
     * @param parentFrame le cadre parent pour afficher les dialogues
     * @param fileChooser le sélecteur de fichiers pour choisir l'emplacement de sauvegarde
     * @param server l'interface distante du serveur pour recevoir le fichier
     */
    public static void receiveFile(JFrame parentFrame, JFileChooser fileChooser, RemoteInterface server) {
        int result = fileChooser.showSaveDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String fileName = fileChooser.getSelectedFile().getName();
                byte[] fileData = server.receiveFile(fileName);
                try (FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile())) {
                    fos.write(fileData);
                }
                JOptionPane.showMessageDialog(parentFrame, "Fichier reçu avec succès !");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Erreur lors de la réception du fichier : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
