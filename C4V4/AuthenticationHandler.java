import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class AuthenticationHandler implements Runnable  {

    HashMap<String, String> logs;
    Socket player;
    BufferedReader reader;
    PrintWriter writer;

    public AuthenticationHandler(HashMap<String, String> logs, Socket player){
        this.logs = logs;
        this.player = player;
        try{
            this.reader = new BufferedReader(new InputStreamReader(player.getInputStream()));
            this.writer = new PrintWriter(player.getOutputStream(), true);
        }
        catch (Exception e){

        }
    }

    private String getAndRecieve(String question){
        
        writer.println(question);

        String response = null;

        try {
            reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }


        return response;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
    
}
