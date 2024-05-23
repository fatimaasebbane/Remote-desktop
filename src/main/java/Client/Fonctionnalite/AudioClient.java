package Client.Fonctionnalite;

import Service_Nomage.RemoteInterface;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;

public class AudioClient implements Runnable {
    private RemoteInterface server;

    public AudioClient(RemoteInterface server) {
        this.server = server;
    }

    @Override
    public void run() {
        startAudioClient();
    }

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
