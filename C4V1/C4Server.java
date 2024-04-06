import java.io.*;
import java.net.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class C4Server {

    public static void main(String[] args) {
        boolean waitingForTwoPlayersPerGame = true;
        try {
            ServerSocket serverSocket = new ServerSocket(12345); // Server socket listening on port 12345
            System.out.println("Server started. Waiting for players to connect");

            while (waitingForTwoPlayersPerGame){
                // Accept the first player connection
                Socket player1 = serverSocket.accept();
                System.out.println("Player 1 connected.");

                // Accept the second player connection
                Socket player2 = serverSocket.accept();
                System.out.println("Player 2 connected.");
            
                // Create a single thread for handling communication with both players
                Thread thread = new Thread(new C4Game(player1, player2));
                System.out.println("Game started.");
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class C4Game implements Runnable {
        private Socket player1;
        private Socket player2;
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
            return (column <= 6) && (column>=0) && (gameBoard[0][column] == 0);
        }

        private void makeMove(PrintWriter writer, BufferedReader reader, int playerNumber) throws Exception {
            // the following line signlas to the client that it is their turn to make a move
            writer.println("Enter the column (0-6) to change:");
            String changeRequest = reader.readLine();
            // * need to add if statement for "H" => API call and set column to whatever is
            // the best move before passing to update board

            int column = Integer.parseInt(changeRequest);
            if (validMove(column)) {
                updateBoard(column, playerNumber);
            }
            else{
                writer.println("Try agian!");
                makeMove(writer, reader, playerNumber);
            }
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
                    this.sendBoardToPlayers(writer1, writer2);
                }

                //announce game over and result
                if (checkWin(1)) {
                    writer1.println("GAME OVER! Player 1 WINS!");
                    writer2.println("GAME OVER! Player 1 WINS!");
                }
                else if (checkWin(2)){
                    writer1.println("GAME OVER! Player 2 WINS!");
                    writer2.println("GAME OVER! Player 2 WINS!");
                }
                else{
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
}
