import java.io.*;
import java.net.*;

public class SimpleServer {
    public static void main(String[] args) {
        int port = 8080; // Change port number if necessary
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Handle client request in a new thread
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read request from client
            String request = in.readLine();
            System.out.println("Request: " + request);

            // Send HTTP response
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>Java Server Example</title></head><body>");
            out.println("<h1>Java Server Example</h1>");
            out.println("<form id=\"myForm\">");
            out.println("Input: <input type=\"text\" id=\"inputText\"><br>");
            out.println("<button type=\"button\" onclick=\"sendData()\">Submit</button>");
            out.println("</form>");
            out.println("<script>");
            out.println("function sendData() {");
            out.println("var input = document.getElementById('inputText').value;");
            out.println("var xhr = new XMLHttpRequest();");
            out.println("xhr.open('POST', '/submit', true);");
            out.println("xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');");
            out.println("xhr.send('input=' + encodeURIComponent(input));");
            out.println("}");
            out.println("</script>");
            out.println("</body></html>");

            // Close streams and sockets
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
