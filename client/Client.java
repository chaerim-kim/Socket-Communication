import java.io.*;
import java.net.*;
import java.util.*;

// Class to read command line arguments and to communicate with server for file transfering
public class Client {
  // Initialising the state variables
  private static Socket clientSocket = null;
  private static BufferedReader socketInput = null;
  private static PrintWriter socketOutput = null;

  private DataInputStream dis = null;
  private DataOutputStream dos = null;
  private OutputStream os = null;

  private static String fileName;
  private String fromUser;

  private String cmd;
  private String cmdFile;


  // Main method to read command line argument and to call corresponding functions
  public static void main(String args[]) throws IOException {
    // creating new class instance
    Client client = new Client();

    try {
      // Connect the socket to the server port
      clientSocket = new Socket("localhost", 8888);
      // to read from the server
      socketInput = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()) );
      // to send the command to server
      socketOutput = new PrintWriter( clientSocket.getOutputStream(), true );


      if (args.length == 0){
        System.out.println("Invalid argument.");
      }


      for ( int i = 0 ; i < args.length && i < 2 ; i++ ) {
        if( args.length == 1 && args[0].equals("list") ){
          client.getList(args[0]);
          break;
        }

        else if ( args[0].equals("get") ) {
          client.getFile(args[0],args[1]);
          break;
        }

        else if ( args[0].equals("put") ) {
          client.putFile(args[0],args[1]);
          break;
        }

        else {
          System.out.println("Invalid argument. Available are: 'list', 'get', 'put'.");
          break;
        }
      }
    } catch( IOException e ) {
      System.out.println("IO exception has occurred.");
    }
  } //main


  // Function to check if file exists in clientFiles folder
  public boolean checkExists(String fileFromUser){
    boolean exists = false;
    File folder = new File("clientFiles"); //folder path
    File[] listOfFiles = folder.listFiles();

    for (File file : listOfFiles) {
      if (file.isFile()) {
        if (fileFromUser.equals(file.getName())){
          exists=true;
        }
      }
    }
    return exists;
  }



  // Lists all the files on serverFiles folder.
  public static void getList( String cmd ) {
    // writes command to the socket
    socketOutput.println(cmd);

    String fileList = "";

    try{
      while ((fileList = socketInput.readLine()) != null ){
        // if done is read, terminate the loop
        if (fileList.equals("done")){
          break;
        }
        System.out.println(fileList);
      }
    } catch (Exception e) {
      System.out.println("No items in folder. ");
    }
  }



  // Requests fileName to the server and recieve file
  public static void getFile (String cmd, String cmdFile) {
    // writes command and filename to the socket
    socketOutput.println (cmd);
    socketOutput.println (cmdFile);

    try {
      InputStream is = clientSocket.getInputStream();
      DataInputStream dis = new DataInputStream(is);

      // where the result of each read() method call is stored
      int bytesRead;

      // writes the filename and create file
      fileName = dis.readUTF();

      File fileSavePath = new File("clientFiles/" + fileName);
      OutputStream os = new FileOutputStream(fileSavePath);

      // reads file size
      long size = dis.readLong(); // file size
      byte[] buffer = new byte[1024];

      while ((bytesRead = dis.read(buffer)) > 0){
        // Writes to the OutputStream
        os.write(buffer, 0, bytesRead);
        size = size - bytesRead; // until end of file
      }
      os.flush();
      os.close();
      System.out.println("Get Done");

    } catch( IOException e ) {
      System.out.println("Requested file doesn't exist.");
    }
  }



  // Put the requested file from clientfile to serverfile
  public  void putFile(String cmd, String cmdFile) {
    // writes command and filename to the socket
    socketOutput.println(cmd);
    socketOutput.println(cmdFile);

    try {
      // reads requested filename
      String fileFromUser;
      fileFromUser = socketInput.readLine();

      File filepath = new File("clientFiles/" + fileFromUser);
      byte[] bytearr = new byte[(int) filepath.length()];

      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filepath));
      DataInputStream dis = new DataInputStream(bis);

      OutputStream os = clientSocket.getOutputStream();
      DataOutputStream dos = new DataOutputStream(os);

      if(checkExists(fileFromUser) == true){
        // read the requested file
        dis.readFully(bytearr, 0, bytearr.length);
        // write the filename to the outputstream
        dos.writeUTF(filepath.getName());
        // write the file size to the outputstream
        dos.writeLong(bytearr.length);
        // write the content to the outputstream
        dos.write(bytearr, 0, bytearr.length);

        System.out.println("File sent to server.");

      } else if (checkExists(fileFromUser) == false) {
        System.out.println("File doesn't exist!\n");
      }

    } catch (IOException e) {
      System.out.println("Requested file doesn't exist.");
    }
  } // putfile
} //class
