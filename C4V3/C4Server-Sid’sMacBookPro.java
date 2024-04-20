import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class represents a TCP Connect-Four server
 * which does the following: 
 * 1. Waits for players to join
 * 2. Validates that 2 players are actively connected
 * 3. Launches games of Connect-Four in seprate threads
 * 4. Keeps track of when games start and end.
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 3.0
 */
public class C4Server {

  /**
   * serverSocket: A ServerSocket object used to accept joining players.
   * playerQueue: An ArrayList object which holds Socket objects 
   *              representing players who have joined.
   * gamesList: An ArrayList object which holds Thread objects representing
   *            games that are ongoing.
   * gameNumber: Integer to keep track of the total number of games that
   *             have been launched. Used for naming game threads.
   * ipAddress: Integer representing the IP address of the server.
   * serverPort: Integer representing the port number of the server. 
   */
  private ServerSocket serverSocket;
  private ArrayList<Socket> playerQueue;
  private ArrayList<Thread> gamesList;
  private int gameNumber;
  InetAddress ipAddress;
  int serverPort;

  /**
   * Constructor method for creating a new Connect-Four server object.
   * 
   * @param IP : The IP address of the server
   * @param Port : The port number of the server
   * @throws Exception : Throw exception if error occurs when intitalizing 
   *                     server socket.
   */
  private C4Server(String IP, int Port) throws Exception {
    ipAddress = InetAddress.getByName(IP);
    serverPort = Port;
    serverSocket = new ServerSocket(serverPort, 128, ipAddress);
    gameNumber = 0;
    playerQueue = new ArrayList<>();
    gamesList = new ArrayList<>();
  }

  /**
   * Method to initialize games of Connect-Four.
   * This method checks that players in the queue are active.
   * Then it proceeds to launch new threads for each game of two players.
   * 
   */
  private void matchmake() {
    // Check that players in the queue are active
    this.checkPlayers();

    while (this.playerQueue.size() >= 2) {
      // Retrieve the first two player (FIFO)
      Socket player1 = this.playerQueue.remove(0);
      Socket player2 = this.playerQueue.remove(0);

      //Initialize a new Connect-Four thread
      Thread thread = new Thread(new C4Game(player1, player2));

      // Set the name of the thread for tracking purposes
      // Increment game number and add thread to games list
      thread.setName("game" + gameNumber);
      gameNumber++;
      gamesList.add(thread);

      // Start the thread and log that the game has started
      thread.start();
      System.out.println(thread.getName() + " has started.");
    }
  }
  
  /**
   * Static method to check that a player is active.
   * This involves sending a message to a conneceted socket 
   * and waiting for a response.
   * 
   * @param player : The player/socket to check 
   * @return
   */
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
        System.out.println("A player has joined on socket " + player.getPort() + ".");
        gameServer.playerQueue.add(player);
        System.out.println("Matchmaking...");
        gameServer.matchmake();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
