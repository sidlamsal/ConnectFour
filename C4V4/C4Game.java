import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is used to run a game of Connect-Four with 2 players.
 * 
 * @author Pranav Mishra
 * @author Sid Lamsal
 * @version 3.0
 */
public class C4Game implements Runnable {

    /**
     * player1 and player2: Socket objects representing 2 players
     * player1Hints and player2Hints: integer to keep track of how many hints each
     * player has
     * gameBoard: 2d array representing a 7x7 Connect-Four board
     */
    private Socket player1;
    private Socket player2;
    private int player1Hints = 1;
    private int player2Hints = 1;
    int[][] gameBoard = generateBoard();
    private Boolean TO = false;
    private File winAudio = new File("win.wav");
    private File loseAudio = new File("lost.wav");

    /**
     * Constructor method for creating a new C4Game object.
     * 
     * @param player1 : socket connected to player1
     * @param player2 : socket connected to player2
     */
    public C4Game(Socket player1, Socket player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * Method used to initalize a new game board.
     * 
     * @return a 7x7 2d array with all elements set to 0
     */
    private int[][] generateBoard() {
        int[][] matrix = new int[7][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                matrix[i][j] = 0;
            }
        }
        return matrix;
    }

    /**
     * Method used to convert game board to a string.
     * 
     * @return a string representation of the game board
     */
    private String printBoard() {
        String board = "";
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                board = board + gameBoard[i][j] + " ";
            }
            board = board + "\n";
        }
        return board;
    }

    /**
     * Method used to send a string representation of the game to both players.
     * 
     * @param writer1 : writer to player 1
     * @param writer2 : writer to player 2
     */
    private void sendBoardToPlayers(PrintWriter writer1, PrintWriter writer2) {
        // Get string representation of the board
        String board = this.printBoard();

        // Send the board to both players
        writer1.println(board);
        writer2.println(board);
    }

    /**
     * Method used to update the borad.
     * Used when a player makes a move.
     * 
     * @param column       : the column to add a "chip"
     * @param playerNumber : the player number used to indicate who made the move
     */
    private void updateBoard(int column, int playerNumber) {

        int row = 0;
        // Get the row where the move should be placed.
        // To do this, we increment from the bottom row up and find the first row
        // in bounds that is empty.
        // context: In connect-4, the player picks a column and a colored chip is
        // dropped into that column. Here we must determine which row the
        // the chip would fall into
        while ((row < 7) && (gameBoard[row][column] == 0)) {
            row++;
        }

        // add move to the game board
        gameBoard[row - 1][column] = playerNumber;
    }

    /**
     * Check that a move is valid: in bounds and column is not full
     * 
     * @param column : column to check
     * @return : True/False if the proposed move can be made
     */
    private boolean validMove(int column) {
        // true if column number is in bounds (0-6) and column is not full
        return (column <= 6) && (column >= 0) && (gameBoard[0][column] == 0);
    }

    /**
     * Method used to simulate a players turn.
     * 
     * @param writer       : output stream of player, used to send messages
     * @param reader       : input stream of player used to read move
     * @param playerNumber : integer representing which player's turn it is
     * @throws Exception : if any error occurs
     */
    private void makeMove(PrintWriter writer, BufferedReader reader, int playerNumber) throws Exception {
        String changeRequest = null;
        // Send the player a message indicating if they can ask for a hint
        Boolean canMakeHint = false;
        if (playerNumber == 1 && (player1Hints > 0)) {
            writer.println("Player 1, you may enter 'H' for a hint.");
            canMakeHint = (player1Hints > 0);
        } else if (playerNumber == 2 && (player2Hints > 0)) {
            writer.println("Player 2, you may enter 'H' for a hint.");
            canMakeHint = (player2Hints > 0);
        }

        writer.println("Enter the column (0-6 - left to right) to change:");

        // Set up timer for 30-second wait
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TO = true;
                timer.cancel(); // Stop the timer
            }
        }, 30000); // 30 seconds

        while (!TO && changeRequest == null) {
            if (reader.ready()) {
                // Read input from client
                changeRequest = reader.readLine();
                timer.cancel();
            }
        }

        if (changeRequest == null) {
            TO = true;

        } else if ((changeRequest.contains("H")) && canMakeHint) {
            // If the player wants to make a hint and has hints remaining
            // API call to get best move
            int bestMove = this.fetchHint(playerNumber);

            if (bestMove != -1) {
                // if best move can be determined, tell player, update board, and decrement
                // number of moves left
                writer.println("The best move is column " + bestMove);
                updateBoard(bestMove, playerNumber);
                if (playerNumber == 1) {
                    player1Hints--;
                } else {
                    player2Hints--;
                }
            } else {
                // if best move cannot be determined, send a message and retry their turn
                writer.println("Unfortunately, we cannot provide a hint at this time. Please move manually");
                makeMove(writer, reader, playerNumber);
            }
        } else if ((changeRequest.contains("H")) && !canMakeHint) {
            // if the player wants to make a move but cant,
            // notify them and let them retry their turn
            writer.println("You have no hints left. Please move manually");
            makeMove(writer, reader, playerNumber);
        } else {
            // if the player does not want a hint and instead inputs a column number
            try {
                int column = Integer.parseInt(changeRequest);
                // if the move is valid
                if (validMove(column)) {
                    updateBoard(column, playerNumber);
                } else {
                    // if the move is invalid, notifiy the player and retry their turn
                    writer.println("Error: not a valid move: Try again!");
                    makeMove(writer, reader, playerNumber);
                }
            } catch (Exception e) {
                // if the inputed move is invalid, notify the player and retry the turn
                writer.println("Error: move must be an integer between 0-6");
                makeMove(writer, reader, playerNumber);
            }
        }
    }

    /**
     * This function makes an API call to the server to get the best move (which is
     * a column name) for the player making the move (playerMoving)
     * 
     * @param playerMoving : the player who is receiving the hint
     * @return an integer representing the column for the best move or -1 if it
     *         cannot be found
     */
    private int fetchHint(int playerMoving) {
        // Constructing the board state string
        String boardState = "";
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boardState += gameBoard[row][col];
            }
        }

        String url = "https://kevinalbs.com/connect4/back-end/index.php/getMoves?board_data=" + boardState + "&player="
                + playerMoving;

        try {
            // Creating URL object
            URL apiUrl = new URL(url);

            // Opening connection
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            // Setting request method
            connection.setRequestMethod("GET");

            // Reading response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            String response = "";

            while (inputLine != null) {
                response = response + inputLine;
                inputLine = in.readLine();
            }

            // Closing BufferedReader
            in.close();

            // Closing connection
            connection.disconnect();

            // split string to an array
            String[] moves = response.substring(1, response.length() - 1).split(",");

            int bestMove = -1;
            int max = Integer.MIN_VALUE;

            // iterate the array to find the optimal move
            for (String move : moves) {
                int thisVale = Integer.parseInt(move.split(":")[1]);
                if (thisVale > max) {
                    max = thisVale;
                    bestMove = Integer.parseInt(move.split(":")[0].substring(1, 2));
                }
            }

            return bestMove;

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Returning a default value (if no valid move found or in case of error)
        return -1;
    }

    /**
     * This function checks if a game is over.
     * This can happen if either player has won or the board is full.
     * 
     * @return if a game is not over, the function returns true. Otherwise, it
     *         returns false
     */
    private boolean gameNotOver() {
        if (checkWin(1) || checkWin(2)) {
            return false;// if either player has won that means game is over
        }

        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if (gameBoard[row][col] == 0) {
                    return true; // If at least one empty cell found, game is not over
                }
            }
        }

        return false; // if both players have not won and board is full, game is over

    }

    /**
     * This method checks the state of the board to check for a win state.
     * A win is 4 moves in a row in any direction.
     * Each "move" is indicated by the integer at a particular cell of the board.
     * 1 = player 1 move
     * 2 = player 2 move
     * 0 = no move
     * 
     * @param player : Check if this player has won
     * @return true/false if the player has or has not won
     */
    private boolean checkWin(int player) {
        // Check horizontal
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 4; col++) {
                if (gameBoard[row][col] == player &&
                        gameBoard[row][col + 1] == player &&
                        gameBoard[row][col + 2] == player &&
                        gameBoard[row][col + 3] == player) {
                    return true;
                }
            }
        }

        // Check vertical
        for (int col = 0; col < 7; col++) {
            for (int row = 0; row < 4; row++) {
                if (gameBoard[row][col] == player &&
                        gameBoard[row + 1][col] == player &&
                        gameBoard[row + 2][col] == player &&
                        gameBoard[row + 3][col] == player) {
                    return true;
                }
            }
        }

        // Check diagonal (positive slope)
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (gameBoard[row][col] == player &&
                        gameBoard[row + 1][col + 1] == player &&
                        gameBoard[row + 2][col + 2] == player &&
                        gameBoard[row + 3][col + 3] == player) {
                    return true;
                }
            }
        }

        // Check diagonal (negative slope)
        for (int row = 0; row < 4; row++) {
            for (int col = 3; col < 7; col++) {
                if (gameBoard[row][col] == player &&
                        gameBoard[row + 1][col - 1] == player &&
                        gameBoard[row + 2][col - 2] == player &&
                        gameBoard[row + 3][col - 3] == player) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Used to send some string to both players
     * 
     * @param writer1 : write stream of player 1
     * @param writer2 : write stream of player 2
     * @param sendString : string to send
     */
    private void sendToBothPlayer(PrintWriter writer1, PrintWriter writer2, String sendString) {
        writer1.println(sendString);
        writer2.println(sendString);
    }

    /**
     * Used to send audio to player
     * 
     * @param player : to whom to send the audio to
     * @param audioFile : which audio (.wav) file to send
     */
    private void sendAudio(Socket player, File audioFile) {
        try {
            // Create file input stream for the audio file
            FileInputStream fileInputStream = new FileInputStream(audioFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // Get socket's output stream
            OutputStream outputStream = player.getOutputStream();

            // Send audio file to the client
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            // Close resources
            bufferedInputStream.close();
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            System.out.println("problem sending audio");
            System.out.println(e);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // reader: reads input from player, writer: sends output to player
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
            PrintWriter writer1 = new PrintWriter(player1.getOutputStream(), true);

            BufferedReader reader2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
            PrintWriter writer2 = new PrintWriter(player2.getOutputStream(), true);

            // indicate that the game has started
            sendToBothPlayer(writer1, writer2, "Connect Four!");
            writer1.println("Welcome Player 1");
            writer2.println("Welcome Player 2");

            // make the board and send it
            this.generateBoard();
            this.sendBoardToPlayers(writer1, writer2);

            int turnPlayer = 1;

            // while the game is not over, keep alternating moves
            while (gameNotOver()) {
                sendToBothPlayer(writer1, writer2, "Current Turn: Player " + turnPlayer);
                makeMove(writer1, reader1, turnPlayer);

                turnPlayer = 2;

                // send board to players after each move
                this.sendBoardToPlayers(writer1, writer2);

                // if the game is over break out of loop
                if (!gameNotOver() || TO) {
                    break;
                }

                sendToBothPlayer(writer1, writer2, "Current Turn: Player " + turnPlayer);
                makeMove(writer2, reader2, turnPlayer);

                turnPlayer = 1;

                // send board to players after each move
                this.sendBoardToPlayers(writer1, writer2);

                // if the game is over break out of loop
                if (!gameNotOver() || TO) {
                    break;
                }
            }

            Socket winner = null;
            Socket loser = null;

            if (TO) {
                sendToBothPlayer(writer1, writer2, "GAME OVER! Player " + turnPlayer + " WINS VIA TIMEOUT!");
                if (turnPlayer == 1){
                    winner = player1;
                } else {
                    winner = player2;
                }
                Thread.sleep(1000);  
                sendAudio(winner, winAudio);
            } else if (checkWin(1)) {
                sendToBothPlayer(writer1, writer2, "GAME OVER! Player 1 WINS!");
                winner = player1;
                loser = player2;
                Thread.sleep(1000);
                sendAudio(winner, winAudio);
                sendAudio(loser, loseAudio);
            } else if (checkWin(2)) {
                sendToBothPlayer(writer1, writer2, "GAME OVER! Player 2 WINS!");
                winner = player2;
                loser = player1;
                Thread.sleep(1000);
                sendAudio(winner, winAudio);
                sendAudio(loser, loseAudio);
            } else {
                sendToBothPlayer(writer1, writer2, "GAME OVER! It's a draw!");
                Thread.sleep(1000);
                sendAudio(player1, winAudio);
                sendAudio(player2, winAudio);
            }

        } catch (Exception e) {
            // e.printStackTrace();

        } finally {
            try {
                player1.close();
                player2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}