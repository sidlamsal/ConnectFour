import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class AuthenticationHandler implements Runnable  {

    private HashMap<String, String> logs;
    private Socket player;
    private BufferedReader reader;
    private PrintWriter writer;
    private ArrayList<Socket> playerQueue;

    public AuthenticationHandler(HashMap<String, String> logs, Socket player, ArrayList<Socket> playerQueue){
        this.logs = logs;
        this.player = player;
        this.playerQueue = playerQueue;
        try{
            this.reader = new BufferedReader(new InputStreamReader(player.getInputStream()));
            this.writer = new PrintWriter(player.getOutputStream(), true);
        }
        catch (Exception e){

        }
    }

    private String sendAndReceive(String question){
        
        writer.println(question);

        String response = null;

        try {
            response = reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        return response;
    }

    private void signIn(){
        String userName = this.sendAndReceive("Enter your username");
        if (this.logs.containsKey(userName)){
            String password = this.sendAndReceive("Enter your password");
            if (logs.get("username") == password){
                playerQueue.add(this.player);
                writer.println("Success");
            }
            else{
                writer.println("Password is incorrect.");
                writer.println("Sign in Failed!");
                authentication(); // retry 
            }
        }
        else{
            writer.println("Username not found.");
            writer.println("Sign in Failed!");
            authentication(); // retry
        }
    }

    private void signUp(){
        String username = this.sendAndReceive("Enter your username");

        if (this.logs.containsKey(username)){
            writer.println("Username is taken!");
            authentication(); // retry
        }
        else{
            String password = this.sendAndReceive("Enter your password");
            this.logs.put(username, password);
            playerQueue.add(this.player);
            writer.println("Success");
        }
    }

    private void authentication(){
        String InOrUp = this.sendAndReceive("Enter 1 to sign in, or 2 to sign up");

        if (InOrUp.equals("1")){
            this.signIn();
        }
        else if (InOrUp.equals("2")){
            this.signUp();
        }
        else {
            writer.println("Authentication Failed!");
            authentication();
        }
    }

    @Override
    public void run() {
        authentication();
    }
    
}
