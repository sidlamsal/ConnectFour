import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * This class is used to play a game of Connect-Four as a player.
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 3.0
 */
public class C4Client {

    private static void playAudio(Socket socket) {
        try {
            // Get input stream from the server
            InputStream inputStream = socket.getInputStream();

            // Wrap the input stream in a BufferedInputStream
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            // Create audio input stream
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);

            // Get audio format
            AudioFormat audioFormat = audioInputStream.getFormat();

            // Specify audio format for playback
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            // Open data line for playback
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            // Write audio data to the data line
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }

            // Close resources
            sourceDataLine.drain();
            sourceDataLine.close();
            audioInputStream.close();
            bufferedInputStream.close();
            inputStream.close();
        } catch (Exception e) {
            System.out.println("problem getting/playing audio");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // get server IP and port to connect to
            String ServerIP = args[0];
            int ServerPortNumber = Integer.parseInt(args[1]);

            // connect to server and indicate that connection has begun
            Socket socket = new Socket(ServerIP, ServerPortNumber);
            System.out.println("Matchmaking...");

            // get I/O streams to server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            boolean gameStarted = false;
            String line;

            // while the game has not started, check for a message from the server and
            // respond. This is used to ping that the player is still active.
            while (!gameStarted) {
                line = reader.readLine();
                if (line.contains("marco")) {
                    writer.println("polo");
                } else if (line.contains("Connect Four!")) {
                    // this indicates that the game has started
                    break;
                }
            }

            // continously print output from server
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                // if server asks for input
                if (line.contains("Enter the column (0-6 - left to right) to change:")) {
                    try {
                        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                        String input = consoleReader.readLine();
                        writer.println(input); // Send the change request to the server
                    } catch (Exception e) {
                        System.out.println("You took too long! Game Over, you LOSE.");
                        break;
                    }
                }
                // check for end of game
                if (line.contains("GAME OVER!")) {
                    break;
                }
            }

            playAudio(socket);

            socket.close();
        } catch (Exception e) {
            System.out.println("You took too long or game crashed! Game Over, you LOSE.");
        }
    }
}
