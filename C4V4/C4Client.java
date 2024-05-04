import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * This class is used to play a game of Connect-Four as a player.
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 3.0
 */
public class C4Client {

    /**
     * Used to play audio recieved from server
     * 
     * @param socket : player socket
     */
    private static void playAudio(Socket socket) {
        try {
            InputStream is = socket.getInputStream();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            // Wait for the audio to finish playing
            Thread.sleep(clip.getMicrosecondLength() / 1000);

            clip.close();
            audioInputStream.close();
            is.close();
        } catch (Exception e) {
            System.out.println("problem getting/playing audio");
        }
    }

    /**
     * Function to authenticate player by either signing in or signing up
     * @param socket the socket corresponding to the client
     */
    private static void authenticate(Socket socket) throws IOException{ 
        // get I/O streams to server
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        while(true){
            String line = reader.readLine();
            System.out.println(line);
            if(line.contains("Enter")){
                System.out.println("Now taking user's input");
                String userInput = getInputFromUser();
                System.out.println("User entered: "+userInput);
                writer.println(userInput);
            }
            if(line.equals("Success")){
                System.out.println("You have successfully authenticated");
                break;
            }
        }
    }

    /**
     * Function to perform a pre-game check of player activity by processing a ping request from the server
     * @param socket the socket corresponding to the client
     */
    private static void PreGame(Socket socket) {
        boolean gameStarted = false;
        String line;
        try {
            // get I/O streams to server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Current state: pre-game");

            while (!gameStarted) {
                line = reader.readLine();
                //System.out.println("Line received should be marco but is: "+line);
                if (line != null){
                    if (line.contains("marco")) {
                        writer.println("polo");
                    } else if (line.contains("Connect Four!")) {
                        // this indicates that the game has started
                        gameStarted = true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Problem during pre-game");
        }
    }

    /**
     * Function to get input from the user
     */
    private static String getInputFromUser() {
        return System.console().readLine();
    }

    /**
     * 
     * @param socket the socket corresponding to the client
     */
    private static void playGame(Socket socket) {
        Boolean gameOver = false;
        String line;

        try {
            // get I/O streams to server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Current state: play game");

            while (!gameOver) {
                line = reader.readLine();
                System.out.println(line);

                if(line.contains("Enter the column (0-6 - left to right) to change:")){
                    String input = getInputFromUser();
                    writer.println(input);
                }
                else if (line.contains("GAME OVER!")){
                    gameOver = true;
                }
            }

        } catch (Exception e) {
            System.out.println("Problem during game. Did you take too long??");
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        try{
            // get server IP and port to connect to
            String ServerIP = args[0];
            int ServerPortNumber = Integer.parseInt(args[1]);

            // connect to server and indicate that connection has begun
            Socket socket = new Socket(ServerIP, ServerPortNumber);
            System.out.println("Matchmaking...");

            authenticate(socket);

            // while the game has not started, check for a message from the server and
            // respond. This is used to ping that the player is still active.
            PreGame(socket);

            // continously print output from server and play the game
            playGame(socket);

            playAudio(socket);

            socket.close();

        }
        catch (Exception e){
            System.out.println("Game Crashed");
        }
    }
}
