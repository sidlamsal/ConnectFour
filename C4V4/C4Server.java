import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
 * @version 4.0
 */
public class C4Server {

  /**
   * serverSocket: A ServerSocket object used to accept joining players.
   * playerQueue: An ArrayList object which holds Socket objects
   * representing players who have joined.
   * gamesList: An ArrayList object which holds Thread objects representing
   * games that are ongoing.
   * gameNumber: Integer to keep track of the total number of games that
   * have been launched. Used for naming game threads.
   * ipAddress: Integer representing the IP address of the server.
   * serverPort: Integer representing the port number of the server.
   */
  private ServerSocket serverSocket;
  protected ArrayList<Socket> playerQueue;
  private ArrayList<Thread> gamesList;
  private int gameNumber;
  InetAddress ipAddress;
  int serverPort;
  protected HashMap<String, String> logs;// set to file path

  /**
   * Constructor method for creating a new Connect-Four server object.
   * 
   * @param IP   : The IP address of the server
   * @param Port : The port number of the server
   * @throws Exception : Throw exception if error occurs when intitalizing
   *                   server socket.
   */
  private C4Server(String IP, int Port) throws Exception {
    ipAddress = InetAddress.getByName(IP);
    serverPort = Port;
    serverSocket = new ServerSocket(serverPort, 128, ipAddress);
    gameNumber = 0;
    playerQueue = new ArrayList<>();
    gamesList = new ArrayList<>();
    logs = readLogsFromFile("C4Logs.txt");
  }

  /**
   * Method that reads the logs in the file specified in the parameter and returns
   * a HashMap comprising the key value pairs found (username and password)
   * 
   * @param filename the path of the file containing the usernames and passwords
   * @return a HashMap object containing the username-password pairs stored in the
   *         file
   */
  private static HashMap<String, String> readLogsFromFile(String filename) {
    HashMap<String, String> readLogs = new HashMap<>();

    try {
      String logsString = new String(Files.readAllBytes(Paths.get(filename)));

      if (logsString.length() != 0) {
        String[] logsList = logsString.split("\n");
        for (int i = 0; i < logsList.length; i++) {
          String[] usernameAndPassword = logsList[i].split(", ");
          readLogs.put(usernameAndPassword[0], usernameAndPassword[1].trim());
        }
      }
    } catch (IOException e) {
      System.out.println(e);
    }

    return readLogs;
  }

  /**
   * used to add content to filePath
   * 
   * @param filePath
   * @param content
   * @throws IOException
   */
  public static void appendStringToFile(String filePath, String content) throws IOException {
    Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.APPEND);
  }

  /**
   * Method to update the logs found in the file specified in the file path
   * 
   * @param filename the path of the file where the logs of username-password
   *                 combinations need to be made
   */
  private void updateLogsInFile(String filename) {
    try {
      Files.write(Paths.get(filename), new byte[0]);

      for (Map.Entry<String, String> entry : logs.entrySet()) {
        String logString = entry.getKey() + ", " + entry.getValue() + "\n";
        appendStringToFile(filename, logString);
      }

    } catch (IOException e) {
      System.out.println(e);
    }
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

    while (playerQueue.size() >= 2) {
      // Retrieve the first two player (FIFO)
      Socket player1 = playerQueue.remove(0);
      Socket player2 = playerQueue.remove(0);

      // Initialize a new Connect-Four thread
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
   * @param player : The player/socket to check.
   * @return : True/False indicating if the player is still active
   */
  private static boolean isActive(Socket player) {
    boolean active = false;
    try {
      // Get I/O streams
      BufferedReader reader = new BufferedReader(new InputStreamReader(player.getInputStream()));
      PrintWriter writer = new PrintWriter(player.getOutputStream(), true);

      // Send message
      writer.println("marco");

      String reponse = reader.readLine();

      // If player responds with correct reponse, return true, else flase
      if (reponse.contains("polo")) {
        active = true;
      } else {

        active = false;
      }

      return active;

    } catch (Exception e) {
      System.out.println(e);
      return false;
    }

  }

  /**
   * Method to check that players in playerQueue are active.
   * Involves running isActive on all players in the queue
   * and removing any inactive players.
   *
   */
  private void checkPlayers() {
    // Initialize an ArrayList to store active players
    ArrayList<Socket> activePlayers = new ArrayList<>();

    // For all players in the current queue,
    // if the player is active, add them to the
    // activePlayers ArrayList, else log that they
    // have been removed from the queue.
    for (int i = 0; i < playerQueue.size(); i++) {
      Socket player = playerQueue.get(i);
      if (isActive(player)) {
        activePlayers.add(player);
      } else {
        System.out.println(player.toString() + " has been removed.");
      }
    }

    // Set the player queue to the list of active players
    playerQueue = activePlayers;
  }

  public static void main(String[] args) {
    // Dummy "True" variable for readability
    boolean waitingForPlayers = true;

    try {
      // Initialize a new Connect-Four server object and log that the server has
      // started.
      C4Server gameServer = new C4Server("localhost", 12345);
      System.out.println("*Server started at IP: " + gameServer.ipAddress + " and port: " + gameServer.serverPort
          + ".*");
      gameServer.serverSocket.setSoTimeout(10000);
      
      // Start a C4CheckGame thread to check/remove games that have ended in real time
      Thread checkGamesThread = new Thread(new C4CheckGame(gameServer.gamesList));
      checkGamesThread.start();
      System.out.println("Started game checking.");


      while (waitingForPlayers) {
        // Accept player
        System.out.println("Waiting for players to connect\n");
        try{
          Socket player = gameServer.serverSocket.accept();
          System.out.println("A player has joined on socket " + player.getPort() + ".");
  
          Thread auth = new Thread(new AuthenticationHandler(gameServer, player));
          auth.start();
        }
        catch(Exception e){
          System.out.println("timeout waiting for player");
        }
        
        // Attempt to matchmake
        System.out.println("Matchmaking. Player count: " + gameServer.playerQueue.size());
        gameServer.matchmake();
        gameServer.updateLogsInFile("C4Logs.txt");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
