import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class is used to play a game of Connect-Four as a player.
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 3.0
 */
public class C4Client {

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
                    // read input from terminal
                    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                    String input = consoleReader.readLine();
                    writer.println(input); // Send the change request to the server
                }
                // check for end of game
                if (line.contains("GAME OVER!")) {
                    break;
                }
            }

            reader.close();
            writer.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
