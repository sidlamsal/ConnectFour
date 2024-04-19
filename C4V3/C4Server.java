import java.io.*;
import java.net.*;
import java.util.*;

public class C4Server {

  private ServerSocket serverSocket;
  private ArrayList<Socket> playerQueue;
  private ArrayList<Thread> gamesList;
  private int gameNumber;
  InetAddress ipAddress;
  int serverPort;

  private C4Server(String IP, int Port) throws Exception {
    ipAddress = InetAddress.getByName(IP);
    serverPort = Port;
    serverSocket = new ServerSocket(serverPort, 128, ipAddress);
    gameNumber = 0;
    playerQueue = new ArrayList<>();
    gamesList = new ArrayList<>();
  }

  private void matchmake() {
    this.checkPlayers();
    while (this.playerQueue.size() >= 2) {
      Socket player1 = this.playerQueue.remove(0);
      Socket player2 = this.playerQueue.remove(0);

      Thread thread = new Thread(new C4Game(player1, player2));
      thread.setName("game" + gameNumber);
      gameNumber++;
      gamesList.add(thread);
      thread.start();
      System.out.println(thread.getName() + " has started.");
    }
  }
  
  private static boolean isActive(Socket player) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(player.getInputStream()));
      PrintWriter writer = new PrintWriter(player.getOutputStream(), true);

      writer.println("marco");
      if(reader.readLine().contains("polo")){
        return true;
      }
      else{
        return false;
      }

    } catch (Exception e) {
      return false;
    }
  }

  private void checkPlayers() {
    ArrayList<Socket> activePlayers = new ArrayList<>();

    for (int i = 0; i < playerQueue.size(); i++) {
      Socket player = playerQueue.get(i);
      if (isActive(player)) {
        activePlayers.add(player);
      } else {
        System.out.println(player.toString() + " has been removed.");
      }
    }
    // Replace the original playerQueue with the list of active players
    playerQueue = activePlayers;
  }

  public static void main(String[] args) {
    boolean waitingForPlayers = true;

    try {
      C4Server gameServer = new C4Server("localhost", 12345);
      System.out.println("Server started at IP: " + gameServer.ipAddress + " and port: " + gameServer.serverPort
          + ".");

      Thread checkGamesThread = new Thread(new C4CheckGame(gameServer.gamesList));
      checkGamesThread.start();
      System.out.println("Started game checking.");

      while (waitingForPlayers) {
        // could we multi-thread this? yes
        System.out.println("Waiting for players to connect");
        Socket player = gameServer.serverSocket.accept();
        System.out.println("A player has joined.");
        gameServer.playerQueue.add(player);
        System.out.println("matchmaking...");
        gameServer.matchmake();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
