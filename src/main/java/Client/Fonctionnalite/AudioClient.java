package Client.Fonctionnalite;

import Service_Nomage.RemoteInterface;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;

/**
 * La classe AudioClient gère la capture et la lecture des flux audio à partir d'un serveur distant.
 * Elle implémente l'interface Runnable pour permettre son exécution dans un thread séparé.
 */
public class AudioClient implements Runnable {
    private RemoteInterface server;


    /**
     * Constructeur de la classe AudioClient.
     *
     * @param server l'interface distante du serveur pour capturer les morceaux audio
     */
    public AudioClient(RemoteInterface server) {
        this.server = server;
    }

    /**
     * Méthode appelée lors de l'exécution du thread.
     * Elle démarre le client audio pour capturer et lire les morceaux audio du serveur.
     */
    @Override
    public void run() {
        startAudioClient();
    }
    /**
     * Démarre le client audio pour capturer et lire les morceaux audio du serveur.
     * Configure le format audio et utilise un SourceDataLine pour lire les morceaux audio capturés.
     */
    private void startAudioClient() {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try (SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info)) {
            speakers.open(format);
            speakers.start();

            while (true) {
                byte[] audioChunk = server.captureAudioChunk();
                ByteArrayInputStream bais = new ByteArrayInputStream(audioChunk);
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = bais.read(buffer)) != -1) {
                    speakers.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
