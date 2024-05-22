package Client.Fonctionnalite;

import Service.RemoteInterface;

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

            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while (true) {
                byte[] audioChunk = server.captureAudioChunk();
                ByteArrayInputStream bais = new ByteArrayInputStream(audioChunk);

                // Fill the buffer with data from the audio chunk
                while ((bytesRead = bais.read(buffer, 0, bufferSize)) != -1) {
                    // Write to the buffer of the SourceDataLine
                    int bytesWritten = speakers.write(buffer, 0, bytesRead);

                    // Ensure that all bytes are written before proceeding
                    while (bytesWritten != bytesRead) {
                        bytesWritten += speakers.write(buffer, bytesWritten, bytesRead - bytesWritten);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
