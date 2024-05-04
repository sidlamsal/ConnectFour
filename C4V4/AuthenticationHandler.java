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
    private Boolean isAuth;
    private ArrayList<Socket> playerQueue;

    public AuthenticationHandler(HashMap<String, String> logs, Socket player, ArrayList<Socket> playerQueue){
        this.logs = logs;
        this.player = player;
        this.isAuth = false;
        this.playerQueue = playerQueue;
        try{
            this.reader = new BufferedReader(new InputStreamReader(player.getInputStream()));
            this.writer = new PrintWriter(player.getOutputStream(), true);
        }
        catch (Exception e){

        }
    }

    private String sendAndRecieve(String question){
        
        writer.println(question);

        String response = null;

        try {
            reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        return response;
    }

    private void signIn(){
        String userName = this.sendAndRecieve("Enter your username");
        if (this.logs.containsKey(userName)){
            String password = this.sendAndRecieve("Enter your password");
            if (logs.get("username") == password){
                this.isAuth = true;
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
        String username = this.sendAndRecieve("Enter your username");

        if (this.logs.containsKey(username)){
            writer.println("Username is taken!");
            authentication(); // retry
        }
        else{
            String password = this.sendAndRecieve("Enter your password");
            this.logs.put(username, password);
            this.isAuth = true;
        }
    }

    private void authentication(){
        String InOrUp = this.sendAndRecieve("Enter 1 to sign in, or 2 to sign up");

        if (InOrUp == "1"){
            this.signIn();
        }
        else if (InOrUp == "2"){
            this.signUp();
        }
        else {
            writer.println("Authentication Failed!");
        }
    }

    @Override
    public void run() {
        authentication();
        if (this.isAuth){
            playerQueue.add(this.player);
        }
    }
    
}
