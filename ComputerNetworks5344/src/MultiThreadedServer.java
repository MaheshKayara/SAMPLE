
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

public final class MultiThreadedServer {

	public static ServerSocket serverSocket;
	// Set port number
	public static int portNumber = 4853;

	public static void main(String argv[]) throws Exception {
		try {
			// Establish the listening socket
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Port number is: " + serverSocket.getLocalPort());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// Wait for and process HTTP service requests
		while (true) {
			// Wait for TCP connection
			Socket requestSocket = serverSocket.accept();

			// Create an object to handle the request
			HttpRequest request = new HttpRequest(requestSocket);
			// request.run();

			// Create a new thread for the request
			Thread thread = new Thread(request);

			// Start the thread
			thread.start();

			// requestSocket.close();
		}
	}
}

final class HttpRequest implements Runnable {
	// Constants
	// Recognized HTTP methods
	public static class HTTP_METHOD {
		public static String POST = "POST";
		public static String GET = "GET";
		public static String HEAD = "HEAD";

	}

	public static String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot\\";
	public static String HTTPVERSION = "HTTP/1.0 200 OK";
	public static String CRLF = "\r\n";
	public static String SERVERNAME = "Server: MyServer";
	public static int BUFFER_SIZE = 1024;
	public static Socket socket;
	public static String uri;
	public static String requestedFileName;
	public static String[] serverDetails = fetch_hostname_and_ipaddress();
	public static String ipAddress = serverDetails[0];
	public static String hostname = serverDetails[1];
    //public static String fileName = getUri().toString();

	// Constructor
	@SuppressWarnings("static-access")
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	private static String[] fetch_hostname_and_ipaddress() {
		// TODO Auto-generated method stub
		InetAddress ip = null;
		String hostname = null;
		String arrayData[] = new String[2];

		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		arrayData[0] = ip.toString();
		arrayData[1] = hostname;
		return arrayData;
	}

	// Implements the run() method of the Runnable interface
	public void run() {
		try {
			processRequestFromClient();
			processResponseToClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Process a HTTP request
	public static void processRequestFromClient() throws Exception {
		try {
			System.out.println("******************************REQUEST**********************************");
			System.out.println(Calendar.getInstance().getTime()+"|Request Recieved....");
			InputStream input = socket.getInputStream();
		    StringBuffer requestBuffer = new StringBuffer(2048);
		    int i = 0;
		    byte[] buffer = new byte[BUFFER_SIZE];
		    try {
		      i = input.read(buffer);
		    } catch (SocketException sockExp) {
				System.out.println("Socket Timed-Out. Please Retry..");
				System.out.println(sockExp.toString());
			} catch (IOException e) {
		      e.printStackTrace();
		      i = -1;
		    }
		    for (int j=0; j<i; j++) {
		    	requestBuffer.append((char) buffer[j]);
		    }
		    System.out.println("Request Type: GET");
		    System.out.println("HTTP Version: HTTP/1.1");
		    System.out.print("Request Line: "+requestBuffer.toString());
		    requestedFileName = readRequest(requestBuffer.toString());
		    System.out.println("Requested File: "+requestedFileName);
		} catch (SocketException sockExp) {
			System.out.println("Socket Timed-Out. Please Retry..");
			System.out.println(sockExp.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}	}

	public static String parseUri(String requestString) {
		int index1, index2;
		index1 = requestString.indexOf(' ');
		if (index1 != -1) {
			index2 = requestString.indexOf(' ', index1 + 1);
			if (index2 > index1)
				return requestString.substring(index1 + 1, index2);
		}
		return null;
	}

	public static String readRequest(String requestString) {
	    int i = requestString.indexOf(' ');	
	    if (i != -1) {
	      int j = requestString.indexOf(' ', i + 1);
	      if (j > i)
	        return requestString.substring(i + 1, j);
	    }
	    return null;
	  }

	public static String getUri() {
		return uri;
	}

	public static void processResponseToClient() throws Exception {
		System.out.println("******************************RESPONSE**********************************");
		  System.out.println(Calendar.getInstance().getTime()+"|Creating & Sending Response....");
		  OutputStream output = socket.getOutputStream();
		  BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
		  
		  byte[] bytes = new byte[BUFFER_SIZE];
		    FileInputStream fis = null;
		    try {
		      File file = new File(WEB_ROOT, getRequestedFileName());
		      if (file.exists()) {
		    	  fis = new FileInputStream(file);
		    	  int ch = fis.read(bytes, 0, BUFFER_SIZE);
		    		String headerString = "HTTP/1.1 200 OK" + "  ServerName:" + SERVERNAME + "  HostName:" + hostname
							+ "  IPAddress" + ipAddress + "  Content-Type: "+contentType(getRequestedFileName())
							+ "  Content-Length: "+ch;
		    	  output.write(headerString.getBytes());
		    	  while (ch!=-1) {
		    		  output.write(bytes, 0, ch);
		    		  ch = fis.read(bytes, 0, BUFFER_SIZE);
		    	  }
		      } else {
		        String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
		          "Content-Type: "+contentType(getRequestedFileName())+"\r\n" +
		          "Content-Length: 99\r\n" +
		          "\r\n" +
		          "<h1>HTTP:404 File Not Found</h1>";
		        output.write(errorMessage.getBytes());
		      }
		      System.out.println(Calendar.getInstance().getTime()+"|----Response Sent----");
		    } catch (SocketException sockExp) {
				System.out.println("Socket Timed-Out. Please Retry..");
				System.out.println(sockExp.toString());
				String errorMessage = "HTTP/1.1 500 Server Error\r\n" +
				          "Content-Type: "+contentType(getRequestedFileName())+"\r\n" +
				          "Content-Length: 99\r\n" +
				          "\r\n" +
				          "<h1>HTTP:500 Server Error</h1>";
		    	output.write(errorMessage.getBytes());
			} catch (FileNotFoundException FNFExp) {
		    	String errorMessage = "HTTP/1.1 400 Bad Request\r\n" +
				          "Content-Type: "+contentType(getRequestedFileName())+"\r\n" +
				          "Content-Length: 99\r\n" +
				          "\r\n" +
				          "<h1>400 Bad Request</h1>";
		    	output.write(errorMessage.getBytes());
		    	//FNFExp.printStackTrace();
		    } catch (NullPointerException nullExp) {
		    	//output.write(nullExp.getMessage().getBytes());
		    	System.out.println(nullExp.toString());
		    	//nullExp.printStackTrace();
		    } catch (Exception e) {
		    	output.write(e.getMessage().getBytes());
		    }
		    finally {
		      if (fis!=null)
		        fis.close();
		      output.flush();
		      output.close();
		    }	}

	public static String getRequestedFileName() {
	    return requestedFileName;
	  }
	private static String contentType(String fileName) {

		if (fileName.toLowerCase().endsWith(".htm") || fileName.toLowerCase().endsWith(".html")) {
			return "html";
		} else if (fileName.toLowerCase().endsWith(".gif")) {
			return "image/gif";
		} else if (fileName.toLowerCase().endsWith(".txt")) {
			return "text/txt";
		} else if (fileName.toLowerCase().endsWith(".jpg")) {
			return "image/jpeg";
		} else {
			return "application/octet-stream";
		}
	}

}