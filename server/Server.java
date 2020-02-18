import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


// Class that connects to server for client to connect to, for file transfer,
public class Server {
  // Main method to connect to socket
  public static void main(String[] args) throws IOException {
    // Initializing state variables
    ExecutorService service = null;
    ServerSocket serverSocket = null ;
    Socket clientSocket = null;

    // Connects to port 8888
    try {
      serverSocket = new ServerSocket( 8888 );
      System.out.println("Server Start");

    } catch (IOException e) {
      System.out.println("Cannot connect to the server.");
    }


    // Initialise the executor by creating a threadpool of size 10
    service = Executors.newFixedThreadPool(10);


    while (true) {
      // accept the client socket's connection to server socket,
      // Then hand over the request to the ClientHandler
      try {
        clientSocket = serverSocket.accept();
        service.submit( new ClientHandler( clientSocket ) );


      } catch (Exception e) {
          System.err.println("Conection Error.");
      }
    } //while
  }  // main
} // Server
