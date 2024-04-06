import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class C4Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345); // Connect to localhost on port 12345 (server port#)
            System.out.println("Matchmaking...");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // continously print output from server
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                //if server asks for input
                if (line.contains("Enter the column (0-6) to change:")) {
                    // read input and send to server
                    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                    String input = consoleReader.readLine();
                    writer.println(input); // Send the change request to the server
                }
                // go back to waiting for output from server to print
                // check for end of game
                if (line.contains("GAME OVER!")) {
                    break;
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
