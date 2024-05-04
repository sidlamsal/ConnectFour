import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AuthenticationHandler implements Runnable {

    private Socket player;
    private BufferedReader reader;
    private PrintWriter writer;
    private C4Server gameServer;

    public AuthenticationHandler(C4Server gameServer, Socket player) {

        this.player = player;
        this.gameServer = gameServer;
        try {
            this.reader = new BufferedReader(new InputStreamReader(player.getInputStream()));
            this.writer = new PrintWriter(player.getOutputStream(), true);
        } catch (Exception e) {

        }
    }

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
