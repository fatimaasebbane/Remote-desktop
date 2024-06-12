package Service_Nomage;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * Interface distante pour les opérations RMI (Remote Method Invocation).
 */
public interface RemoteInterface extends Remote {
    /**
     * Capture l'écran et renvoie l'image sous forme de tableau d'octets.
     *
     * @return un tableau d'octets représentant l'image capturée de l'écran.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    byte[] captureScreen() throws RemoteException;

    /**
     * Vérifie si le mot de passe fourni est correct.
     *
     * @param password le mot de passe à vérifier.
     * @return true si le mot de passe est correct, false sinon.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    boolean checkPassword(String password) throws RemoteException;

    /**
     * Simule l'appui d'un bouton de la souris.
     *
     * @param button le bouton de la souris à appuyer (1 pour gauche, 2 pour milieu, 3 pour droit).
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void mousePressed(int button) throws RemoteException;

    /**
     * Simule le relâchement d'un bouton de la souris.
     *
     * @param button le bouton de la souris à relâcher (1 pour gauche, 2 pour milieu, 3 pour droit).
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void mouseReleased(int button) throws RemoteException;

    /**
     * Simule l'appui sur une touche du clavier.
     *
     * @param keyCode le code de la touche à appuyer.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void keyPressed(int keyCode) throws RemoteException;

    /**
     * Simule le relâchement d'une touche du clavier.
     *
     * @param keyCode le code de la touche à relâcher.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void keyReleased(int keyCode) throws RemoteException;

    /**
     * Simule le déplacement de la souris vers les coordonnées spécifiées.
     *
     * @param x la coordonnée X de destination.
     * @param y la coordonnée Y de destination.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void mouseMoved(int x, int y) throws RemoteException;

    /**
     * Simule le glissement de la souris vers les coordonnées spécifiées.
     *
     * @param x la coordonnée X de destination.
     * @param y la coordonnée Y de destination.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void dragMouse(int x, int y) throws RemoteException;

    /**
     * Envoie un fichier au serveur.
     *
     * @param fileData les données du fichier à envoyer.
     * @param fileName le nom du fichier.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void sendFile(byte[] fileData, String fileName) throws RemoteException;

    /**
     * Reçoit un fichier du serveur.
     *
     * @param fileName le nom du fichier à recevoir.
     * @return les données du fichier sous forme de tableau d'octets.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    byte[] receiveFile(String fileName) throws RemoteException;

    /**
     * Obtient la taille de l'écran.
     *
     * @return un objet Dimension représentant la taille de l'écran.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    Dimension getScreenSize() throws RemoteException;

    /**
     * Simule la frappe d'un caractère au clavier.
     *
     * @param keyChar le caractère à taper.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    void typeKey(char keyChar) throws RemoteException;

    /**
     * Capture un morceau d'audio et renvoie les données audio sous forme de tableau d'octets.
     *
     * @return un tableau d'octets représentant les données audio capturées.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    byte[] captureAudioChunk() throws RemoteException;

    /**
     * Génère un mot de passe et le renvoie.
     *
     * @return le mot de passe généré.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    String generatePassword() throws RemoteException;

}