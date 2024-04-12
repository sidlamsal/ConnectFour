import java.io.*;
import java.net.*;
import java.util.*;

public class C4Server {

  public static void main(String[] args) {
    boolean waitingForPlayers = true;

    try {
      InetAddress ipadd = InetAddress.getByName("localhost");
      ServerSocket serverSocket = new ServerSocket(12345, 128, ipadd); // Server socket listening on port 12345

      System.out.println("Server started at " + ipadd + ". Waiting for players to connect");
      ArrayList<Socket> playerList = new ArrayList<>();
      Map<Thread, Integer> gameMap = new HashMap<>();

      int gameNumber = 0;

      while (waitingForPlayers) {
        boolean twoPlayersFound = false;
        
        while (!twoPlayersFound) {
          // Accept a player connection
          Socket player = serverSocket.accept();
          playerList.add(player);

          if (numberOfActivePlayersIsTwo(playerList)) {// at a given time, the player list should have two active
                                                       // players for the game to be established
            gameNumber += 1;
            System.out.println("2 players have been matched!");
            twoPlayersFound = true;
          }

        }

        System.out.println("Player 1 connected.");
        System.out.println("Player 2 connected.");
        Socket player1 = playerList.get(0);
        Socket player2 = playerList.get(1);
        playerList.clear(); // empty the list to make way for new clients

        // Create a single thread for handling communication with both players
        Thread thread = new Thread(new C4Game(player1, player2));
        gameMap.put(thread, gameNumber);
        System.out.println("Game "+gameNumber+" started.");
        thread.start();
        
        for (Map.Entry<Thread, Integer> entry : gameMap.entrySet()) {
          Thread aGame = entry.getKey();
          int gameId = entry.getValue();

          // Check if the thread is not alive
          if (!aGame.isAlive()) {
              // Print the message and remove the entry from the map
              System.out.println("Game " + gameId + " is over");
              gameMap.remove(aGame); // Remove the current entry from the map
          }
      }

      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean numberOfActivePlayersIsTwo(ArrayList<Socket> listOfPlayers) {
    int numberOfActivePlayers = 0;
    for (Socket player : listOfPlayers) {
      if (!player.isClosed() && player.isConnected()) {
        numberOfActivePlayers += 1;
      } else {
        listOfPlayers.remove(player);
      }
    }

    return numberOfActivePlayers == 2;
  }
}
