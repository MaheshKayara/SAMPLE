import java.io.*;
import java.net.*;

public class Client {
    public static void main(String argv[]) throws Exception {
    	
    	String serverIPAddress = argv[0].trim();
    	int portNumber = Integer.parseInt(argv[1].trim());
    	String fileName = argv[2].trim();
    	
    	//This can be used to define default IPAddress,filename and port no
//    	  String serverIPAddress = "localhost";
//    	int portNumber = Integer.parseInt("4853");
//    	String fileName = "sample.txt"; 
    	
        String sentence;
        String modifiedSentence;
        String Client_Name = "Mahesh:Server";
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        long startTime = System.currentTimeMillis();
        Socket clientSocket = new Socket(serverIPAddress, portNumber);
        ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
      
        sentence = "GET "+fileName+" http/1.1 "+"   Client-Name"+Client_Name;   // Client request to be sent to the server
		outToServer.writeObject(sentence + '\n');    // Sending client request to server
		outToServer.flush();

       
        modifiedSentence = inFromServer.readLine();
        System.out.println(modifiedSentence);
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Round Trip Time is "+totalTime+"ns");
        clientSocket.close();
    }
}