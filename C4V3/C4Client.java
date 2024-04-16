import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class C4Client {

    public static void main(String[] args) {
        try {
            String ServerIP = args[0];
            int ServerPortNumber = Integer.parseInt(args[1]);
            Socket socket = new Socket(ServerIP, ServerPortNumber); // Connect to IP on port 12345 (server port#)
            System.out.println("Matchmaking...");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            boolean gameStarted = false;
            String line;
            
            while(!gameStarted){
                line = reader.readLine();
                if (line.contains("marco")){
                    writer.println("polo");
                }
                else if (line.contains("Connect Four!")){
                    break;
                }
            }

            // continously print output from server
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                //if server asks for input
                if (line.contains("Enter the column (0-6 - left to right) to change:")) {
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

            reader.close();
            writer.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
