import java.util.ArrayList;

/**
 * This class is used to check active connect four games.
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 3.0
 */
public class C4CheckGame implements Runnable {

    // array list of threads representing Connect Four games
    private ArrayList<Thread> gamesList;

    /**
     * Constructor to initalize a new C4CheckGame object
     * 
     * @param gamesList : array list of games to check
     */
    public C4CheckGame(ArrayList<Thread> gamesList) {
        this.gamesList = gamesList;
    }

    @Override
    public void run() {
        while(true){
            try {
                // every 10 seconds
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // iterate over the array of games and remove/announce any inactive games
            for (int i = 0; i < gamesList.size(); i++) {
                Thread t = gamesList.get(i);
                if (!t.isAlive()) {
                    gamesList.remove(i);
                    System.out.println("<GAME CHECKER: " + t.getName() + " has ended.>");
                }
            }
        }
    }
}
