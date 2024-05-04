/**
 * This class is used to authenticate a player
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 4.0
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AuthenticationHandler implements Runnable {

    private Socket player;          // player to authenticate
    private BufferedReader reader;  // used to read messages from player
    private PrintWriter writer;     // used to send messages to player
    private C4Server gameServer;    // game server obj which has references to
                                    // player queue and logs

    /**
     * Constructor for authenticator
     * 
     * @param gameServer
     * @param player
     */
    public AuthenticationHandler(C4Server gameServer, Socket player) {

        this.player = player;
        this.gameServer = gameServer;
        try {
            this.reader = new BufferedReader(new InputStreamReader(player.getInputStream()));
            this.writer = new PrintWriter(player.getOutputStream(), true);
        } catch (Exception e) {

        }
    }

    /**
     * Used to send question to client and return response 
     * 
     * @param question
     * @return a string representing the client's response
     */
    private String sendAndReceive(String question) {

        writer.println(question);

        String response = null;

        try {
            response = reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        return response;
    }

    /**
     * Used to go through sign in protocol
     * 1. Asks for username
     * 2. If that is in the logs
     * 3. asks for password
     * 4. if both match
     * 5. add to player queue
     * 
     */
    private void signIn() {
        String userName = this.sendAndReceive("Enter your username");
        if (this.gameServer.logs.containsKey(userName)) {
            String password = this.sendAndReceive("Enter your password").trim();
            if (gameServer.logs.get(userName).contains(password)) {
                gameServer.playerQueue.add(this.player);
                writer.println("Success");
            } else {
                writer.println("Password is incorrect.");
                writer.println("Sign in Failed!");
                authentication(); // retry
            }
        } else {
            writer.println("Username not found.");
            writer.println("Sign in Failed!");
            authentication(); // retry
        }
    }

    /**
     * Used to go through sign up protocol
     * Ask for a valid user name and password
     * Add player to queue
     */
    private void signUp() {
        String username = this.sendAndReceive("Enter your username");

        if (this.gameServer.logs.containsKey(username)) {
            writer.println("Username is taken!");
            authentication(); // retry
        } else {
            String password = this.sendAndReceive("Enter your password");
            this.gameServer.logs.put(username, password);
            gameServer.playerQueue.add(this.player);
            writer.println("Success");
        }
    }

    /**
     * Used to authenticate player
     * Ask the player if they want to sign up or in, and initialize respective protocol
     */
    private void authentication() {
        String InOrUp = this.sendAndReceive("Enter 1 to sign in, or 2 to sign up");

        if (InOrUp.equals("1")) {
            this.signIn();
        } else if (InOrUp.equals("2")) {
            this.signUp();
        } else {
            writer.println("Authentication Failed!");
            authentication();
        }
    }

    @Override
    public void run() {
        authentication();
    }

}
