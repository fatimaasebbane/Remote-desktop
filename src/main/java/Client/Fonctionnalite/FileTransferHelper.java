package Client.Fonctionnalite;

import Service_Nomage.RemoteInterface;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileTransferHelper {
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
