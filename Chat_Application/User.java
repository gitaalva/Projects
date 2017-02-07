import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;


public class User extends Thread {
    
    // The user socket
    private static Socket userSocket = null;
    // The output stream
    private static PrintStream output_stream = null;
    // The input stream
    private static BufferedReader input_stream = null;
    
    private static BufferedReader inputLine = null;
    private static boolean closed = false;
    
    private static String name;
    
    public static void main(String[] args) throws IOException {
    	
    	if (args.length != 2 )
    	{
    		System.out.println("Invalid number of arguments. Please provide IP address  and port number of the server");
    		System.exit(1);
    	}
    	
        // The default port.
        int portNumber = Integer.parseInt(args[1]);
        // The default host.
        String host = args[0];
        /*
         * Open a socket on a given host and port. Open input and output streams.
         */
        try
        {
        	userSocket = new Socket(host,portNumber);
	        inputLine = new BufferedReader( new InputStreamReader( System.in) );
	        output_stream = new PrintStream(userSocket.getOutputStream());
	        input_stream = new BufferedReader ( new InputStreamReader(userSocket.getInputStream() ));
        } catch (Exception e)
        {
        	System.out.println("Exception occured" + e.getMessage() );
        	System.exit(1);
        }

        //YOUR CODE

        /*
         * If everything has been initialized then create a listening thread to 
         * read from the server. 
         * Also send any user’s message to server until user logs out.
     	 */
        Thread listen_t = null;
        name = "Listen Thread";
        if (listen_t == null )
        {
        	listen_t = new Thread ( new User(), name );
        	listen_t.start();
        }
        String terminalInput;
        while ( true )
        {
        	//System.out.println("Client:");
        	terminalInput = inputLine.readLine();
        	if ( terminalInput != null )
        	{
        		output_stream.println(terminalInput);
        		if (terminalInput.equals("LogOut") )
        		{
				//System.out.println("We are logging out");
        			break;
        		}
        	}
        }
        //System.out.println("Out of while loop");
        try {
        	listen_t.join();
        }
        catch (Exception e) {
        	System.out.println("Error occurred while waiting for listening thread to die" + e.getMessage() );
        }
        //System.out.println("Exit Client");
	}
    
    public void run() {
    	 String serverMessage;
        /*
         * Keep on reading from the socket till we receive “### Bye …” from the
         * server. Once we received that then we want to break and close the connection.
         */
    	 try {
	         while ( closed == false )
	         {
	        	 serverMessage = input_stream.readLine();
	        	 if (serverMessage != null )
	        	 {
	        		 System.out.println(serverMessage );
	        		 if (serverMessage.contains("### Bye"))
	        		 {
	        			//System.out.println("Server said Bye"); 
	        			 break;
	        		 }
				 //System.out.print("Client:");
	        	 }
	         }
    	 } catch (Exception e)
    	 {
    		 System.out.println("Exception occured while reading from input stream on thread" + e.getMessage() );
    	 }
	//System.out.println("Exiting thread"); 
     }
}

