import java.io.*;
import java.net.*;
import java.util.*;

public class C4Game implements Runnable {
    private Socket player1;
    private Socket player2;
    private int player1Hints = 1;
    private int player2Hints = 1;
    int[][] gameBoard = generateBoard();

    public C4Game(Socket player1, Socket player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    private int[][] generateBoard() {
        int[][] matrix = new int[7][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                matrix[i][j] = 0;
            }
        }
        return matrix;
    }

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

    private void sendBoardToPlayers(PrintWriter writer1, PrintWriter writer2) {
        String board = this.printBoard();
        writer1.println(board);
        writer2.println(board);
    }

    private void updateBoard(int column, int playerNumber) {

        int row = 0;
        while ((row < 7) && (gameBoard[row][column] == 0)) {
            row++;
        }
        gameBoard[row - 1][column] = playerNumber;
    }

    private boolean validMove(int column) {
        return (column <= 6) && (column >= 0) && (gameBoard[0][column] == 0);
    }

    private void makeMove(PrintWriter writer, BufferedReader reader, int playerNumber) throws Exception {
        // the following line signlas to the client that it is their turn to make a move
        Boolean canMakeHint;

        if (playerNumber == 1) {
            writer.println("Player 1, you have " + player1Hints + " hints left.");
            canMakeHint = (player1Hints > 0);
        } else {
            writer.println("Player 2, you have " + player2Hints + " hints left.");
            canMakeHint = (player2Hints > 0);
        }

        writer.println("Enter the column (0-6 - left to right) to change:");

        String changeRequest = reader.readLine();

        if ((changeRequest.contains("H")) && canMakeHint) {
            // API call to get best move
            // set column to best move
            // update board
            int bestMove = this.fetchHint(playerNumber);
            if (bestMove != -1) {
                writer.println("The best move is column " + bestMove);
                updateBoard(bestMove, playerNumber);
                if (playerNumber == 1) {
                    player1Hints--;
                } else {
                    player2Hints--;
                }
            } else {
                writer.println("Unfortunately, we cannot provide a hint at this time. Please move manually");
                makeMove(writer, reader, playerNumber);
            }
        } else if ((changeRequest.contains("H")) && !canMakeHint) {
            writer.println("You have no hints left. Please move manually");
            makeMove(writer, reader, playerNumber);
        } else {
            try {
                int column = Integer.parseInt(changeRequest);
                if (validMove(column)) {
                    updateBoard(column, playerNumber);
                } else {
                    writer.println("Error: not a valid move: Try again!");
                    makeMove(writer, reader, playerNumber);
                }
            } catch (Exception e) {
                writer.println("Error: move must be an integer between 0-6");
                makeMove(writer, reader, playerNumber);
            }
        }
    }

    /*
     * This function makes an API call to the server to get the best move (which is
     * a column name) for the player making the move (playerMoving)
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

            String[] moves = response.substring(1, response.length() - 1).split(",");

            int bestMove = -1;
            int max = Integer.MIN_VALUE;

            // System.out.println(response);
            for (String move : moves) {
                // System.out.println(move);
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

    private boolean gameNotOver() {
        // this function checks if a game is over
        // this can happen if either player has won or the board is full
        // if a game is not over, the function returns true. Otherwise, it returns false
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

    private boolean checkWin(int player) {
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

    @Override
    public void run() {
        try {
            // reader: reads input from player, writer: sends output to player
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
            PrintWriter writer1 = new PrintWriter(player1.getOutputStream(), true);

            BufferedReader reader2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
            PrintWriter writer2 = new PrintWriter(player2.getOutputStream(), true);

            // make the board and send it
            writer1.println("Connect Four!");
            writer2.println("Connect Four!");
            this.generateBoard();
            this.sendBoardToPlayers(writer1, writer2);

            // while the game is not over, keep alternating moves
            while (gameNotOver()) {
                makeMove(writer1, reader1, 1);
                this.sendBoardToPlayers(writer1, writer2);
                if (!gameNotOver()) {
                    break;
                }

                makeMove(writer2, reader2, 2);
                // there should be a check here too, right???
                if (!gameNotOver()) {
                    break;
                }
                this.sendBoardToPlayers(writer1, writer2);
            }

            // announce game over and result
            if (checkWin(1)) {
                writer1.println("GAME OVER! Player 1 WINS!");
                writer2.println("GAME OVER! Player 1 WINS!");
            } else if (checkWin(2)) {
                writer1.println("GAME OVER! Player 2 WINS!");
                writer2.println("GAME OVER! Player 2 WINS!");
            } else {
                writer1.println("GAME OVER! It's a draw!");
                writer2.println("GAME OVER! It's a draw!");
            }

        } catch (Exception e) {
            e.printStackTrace();

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