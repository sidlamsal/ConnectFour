import java.util.ArrayList;

/**
 * This class is used to 
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 3.0
 */
public class C4CheckGame implements Runnable {

    private ArrayList<Thread> gamesList;

    public C4CheckGame(ArrayList<Thread> gamesList) {
        this.gamesList = gamesList;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for (int i = 0; i < gamesList.size(); i++) {
                Thread t = gamesList.get(i);
                if (!t.isAlive()) {
                    gamesList.remove(i);
                    System.out.println(t.getName() + " has ended.");
                }
            }
        }
    }
}
