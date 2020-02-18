import java.net.*;
import java.io.*;
import java.util.*;

// Class to execute requests from server side.
public class ClientHandler extends Thread {
  // Initialising state variables
  private Socket socket;
  private PrintWriter out = null;
  private BufferedReader in = null;

  private PrintWriter log = null;
  private DataInputStream dis = null;
  private DataOutputStream dos = null;
  private OutputStream os = null;


  // Constructor to initialize the ClientHandler
  public ClientHandler(Socket socket) {
		super("ClientHandler");
		this.socket = socket;
  }


  // Funciton to check if the file exists in the directory
  public boolean checkExists(String fileFromUser){
    boolean exists = false;
    File folder = new File("serverFiles"); //folder path
    File[] listOfFiles = folder.listFiles();

    for (File file : listOfFiles) {
      if (file.isFile()) {
        if (fileFromUser.equals(file.getName())){
          exists = true;
        }
      }
    }
    return exists;
  }


  // Funciton that processes command line arguments and execute accordingly
  public void run() {
		try {
      // Input and output streams to/from the client
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);


      // Reads User input from Client class
      String fromUser;
      fromUser= in.readLine();


      if (fromUser.equals("list")){
        // creates a new string that will save list of files in the folder
        try{
          File folder = new File("serverFiles");
          File[] listOfFiles = folder.listFiles();
          String temp = "";
          // ArrayList<String> test = new ArrayList<>();

          for (File file : listOfFiles) {
            if (file.isFile()) {
                temp = file.getName();
                out.println(temp);
            }
          }
          System.out.println("list done");
          out.flush();
          // To notify the Client side that it has reached the end
          out.println("done");
          // out.close();

        } catch ( Exception e){
          e.printStackTrace();
        }
      }


      // Reads filename from client's command line argument
      String fileFromUser;
      fileFromUser = in.readLine();


      // Send a file from serverFiles to a client that requests it.
      if (fromUser.equals("get")){

        File filepath = new File("serverFiles/" + fileFromUser);

          try {
            byte[] bytearr = new byte[(int) filepath.length()];

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filepath));
            DataInputStream dis = new DataInputStream(bis);

            OutputStream os = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            if(checkExists(fileFromUser) == true) {
              // read the requested file
              dis.readFully(bytearr, 0, bytearr.length);
              // write the filename to the outputstream
              dos.writeUTF(filepath.getName());
              // write the file size to the outputstream
              dos.writeLong(bytearr.length);
              // write the content to the outputstream
              dos.write(bytearr, 0, bytearr.length);

              dos.close();
              System.out.println("File sent to client.");
            }

          } catch ( Exception e) {
            System.out.println("File doesn't exist!\n");
            // System.exit(-1);
          }
      } // get function



      // Read a file from a client and place it in the serverFiles folder.
       if (fromUser.equals("put")){
        out.println(fileFromUser);//push the thing back to client

        try{
          // Connects to the socket
          InputStream in = socket.getInputStream();
          DataInputStream dis = new DataInputStream(in);

          int bytesRead;

          // writes the filename and create file
          fileFromUser = dis.readUTF();
          File fileSavePath = new File("serverFiles/" + fileFromUser);
          OutputStream os = new FileOutputStream(fileSavePath);

          // reads file size
          long size = dis.readLong(); // file size
          byte[] buffer = new byte[1024];

          while ((bytesRead = dis.read(buffer)) > 0){
            // writes the information read to outputstream
            os.write(buffer, 0, bytesRead);
            size = size - bytesRead;
          }
          os.flush();
          System.out.println("Put done.");

        } catch( IOException e ) {
          System.out.println("Requested file doesn't exist.");
        }
      }

      // Logging.
      FileWriter fw = new FileWriter("log.txt", true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter log = new PrintWriter(bw);

      InetAddress inet = socket.getInetAddress();
      Date date = new Date();

      // for 'list' where filename doesnt exist, print empty string
      if (fileFromUser == null) {
        fileFromUser = "";
      }
      log.println(date.toString() +" : "+ inet.getHostName() +" : "+fromUser);
      log.close();


    } catch (IOException e) {
      System.out.println("An IO expection occured.");
    }
  } // run
} //class
