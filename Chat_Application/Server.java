import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

// Handle the case where a user abruptly ends the connection
	// What needs to be done in that case?
	// close the socket connection
	// Handle the case when the user exits
	// How to handle the case where we can let users exit and add new users;
public class Server {
    // Create a socket for the server 
    private static ServerSocket serverSocket = null;
    // Create a socket for the user 
    private static Socket userSocket = null;
    // Maximum number of users 
    private static int maxUsersCount = 5;
    // An array of threads for users
    private static userThread[] threads = null;
    
    private static int userCount = 0;
    
    public static int findIndex()
    {
    	// Function that goes through the userThread and returns the first null index;
    	for(int i=0; i < maxUsersCount; ++i)
    	{
    		if (threads[i] == null)
    		{
    			return i;
    		}
    	}
    	// return maxUserCount if all threads are occupied
    	return maxUsersCount;
    }

    public static void main(String args[]) {
        System.out.println("Starting Server");
		// The default port number.
    	if (args.length < 1)
    	{
    		System.out.println("Invalid number of arguments. Please provide at least port number as argument");
    		System.exit(1);
    	}
    	int portNumber = Integer.parseInt(args[0]); 
    	
    	// Override the value of maxUsersCount if provided.
    	if (args.length == 2)
    	{
    		maxUsersCount = Integer.parseInt(args[1]);
    	}
        /*
         * Create a user socket for each connection and pass it to a new user
         * thread.
         */
    	try {
    		serverSocket = new ServerSocket (portNumber);
    	} catch (Exception e)
    	{
    		System.out.println("Exception occurred while creating Server Socket" + e.getMessage());
    	}
    	// Let me check
    	threads = new userThread[maxUsersCount];
    	
    	
    	
    	while (true) {
    		int index = findIndex();
			if ( index < maxUsersCount )
			{
				try {
					userSocket = serverSocket.accept();
					threads[index] = new userThread(userSocket, threads);
					//userThread userThread = new userThread(userSocket);
					threads[index].start();
					System.out.println("Done");
				}
				catch (Exception e) {
					System.out.println("Exception occurred while creating threads" + e.getMessage() );
				}
			}
			else {
				// No need to have infinite messages in the log saying max count reached
				// As long as the server does not accept new connections post max count we are good
				//System.out.println("Reached Max Count");
				//break;
			}
		}
    }
}

/*
 * Threads
 */
class userThread extends Thread {
    
    private String userName = null;
    private BufferedReader input_stream = null;
    private PrintStream output_stream = null;
    private Socket userSocket = null;
    private final userThread[] threads; //@alva This is because the first guy needs access to other threads;
    //private int index;
										//
    private int maxUsersCount;

    // only relevant for Part IV: adding friendship
    ArrayList<String> friends = new ArrayList<String>();
    ArrayList<String> friendrequests = new ArrayList<String>();  //keep track of sent friend requests 
  
    public userThread(Socket userSocket, userThread[] threads) {
    //public userThread(Socket userSocket) {
        this.userSocket = userSocket;
        this.threads = threads;
        maxUsersCount = threads.length;
    }
    
    public void writeToOutputStream ( String message)
    {
    	synchronized(this) {
    		try 
    		{
    			output_stream.println(message);
    		}
    		catch (Exception e)
    		{
    			System.out.println("Exception while writing to output_stream" + userName + e.getMessage());
    		}
    	}
    }
    
    // Broadcast to everyone else other than the current user
    public void broadcast_to_rest(String message)
    {
    	synchronized (this) {
    		for (int i = 0; i < maxUsersCount; i++ )
			{
    			userThread thread = threads[i];
    			// if threads[i] has null don't do anything
				if (thread == null || thread == this )
				{
					// do nothing
				}
				else 
				{
					try {
						threads[i].writeToOutputStream (message);
						
					}
					catch (Exception e)
		    		{
		    				System.out.println("Exception while broadcasting message");
		    		}
				}
			}
    	}
    }
    
    public void broadcast(String message)
    {
    	synchronized (this) {
    		for (int i = 0; i < maxUsersCount; i++ )
			{
    			userThread thread = threads[i];
				if (thread == null )
				{
				}
				else 
				{
					try {
						threads[i].writeToOutputStream (message);
						
					}
					catch (Exception e)
		    		{
		    				System.out.println("Exception while broadcasting message");
		    		}
				}
			}
    	}
    }
    
    
    void unicast (String toUser, String message )
    {
    	synchronized (this)
    	{
    		for (int i =0; i< maxUsersCount; ++i )
    		{
    			System.out.println("Here");
    			try {
	    			if(threads[i].userName.equals(toUser))
	    			{
	    				System.out.println("Found user " + threads[i].userName );
	    				threads[i].output_stream.println("<" + userName + ">" + message);
	    			}
    			} catch(Exception e)
    			{
    				System.out.println("nothing to do");
    			}
    		}
    	}
    }
    
    
    // sets the threads[i] of current user to null when the thread has to end
    public void removeFromUserThread()
    {
    	synchronized(this)
    	{
	    	for(int i=0; i < maxUsersCount; ++i)
	    	{
	    		if (threads[i] == this )
	    		{
	    			threads[i] = null;
	    		}
	    	}
    	}
    }
    
    Boolean checkIfUserExists ( String user)
    {
    	synchronized (this)
    	{
    		for (int i =0; i< maxUsersCount; ++i )
    		{
    			try {
    				// check that the userName is the same
    				// check that the user accepting friends is in the friend request list of the user
    				// who sent the friend request. This is to prevent users from adding others as friends without having to send friend request.
	    			if(threads[i].userName.equals(user))
	    			{
	    				return true;
	    			}
    			} catch(Exception e)
    			{
    				System.out.println("nothing to do");
    			}
    		}
    	}
    	return false;
    }
    
    Boolean addUserToFriendList(String user)
    {
    	synchronized (this)
    	{
    		for (int i =0; i< maxUsersCount; ++i )
    		{
    			try {
    				// check that the userName is the same
    				// check that the user accepting friends is in the friend request list of the user
    				// who sent the friend request. This is to prevent users from adding others as friends without having to send friend request.
	    			if(threads[i].userName.equals(user) && threads[i].friendrequests.contains(userName))
	    			{
	    				System.out.println("Found user " + threads[i].userName );
	    				threads[i].friends.add(userName);
	    				return true;
	    			}
    			} catch(Exception e)
    			{
    				System.out.println("nothing to do");
    			}
    		}
    	}
    	return false;
    }
    void deleteUserFromFriendList(String user)
    {
    	synchronized (this)
    	{
    		for (int i =0; i< maxUsersCount; ++i )
    		{
    			try {
	    			if(threads[i].userName.equals(user))
	    			{
	    				System.out.println("Found user " + threads[i].userName );
	    				threads[i].friends.remove(userName);
	    			}
    			} catch(Exception e)
    			{
    				System.out.println("nothing to do");
    			}
    		}
    	}
    }
    public void run() {
    	String userMessage;
    	/*
    	 * Create input and output streams for this client, and start conversation.
    	 */
    	
    	try {
    		input_stream = new BufferedReader(new InputStreamReader (userSocket.getInputStream()) );
   	 		output_stream = new PrintStream ( userSocket.getOutputStream() );
   	 		output_stream.println("Enter your name");
    	}
    	catch (IOException e)
    	{
    		System.out.println("Cannot get either Socket input or outputStream of user" + e.getMessage());
    	}
    	
   	 	try {
   	 		userName = input_stream.readLine();
   	 		// Check to see if the user name entered by the user starts with @
   	 		// If the user name starts with @ the server will prompt the user to renter the username
   	 		// only when a valid user name is entered the user can proceed with braodcasting or unicasting messages
   	 		while (userName.charAt(0) == '@')
   	 		{
   	 			output_stream.println("user name starts with @! Please reenter a new user name");
   	 			userName = input_stream.readLine();
   	 		}
   	 		output_stream.println("Welcome " + userName + " to our chat room"+ "\n" + "To leave enter LogOut in a new line");
   	 		broadcast_to_rest ("*** A new user " + userName + " has entered the chat room");
   	 	} catch ( Exception e)
   	 	{
   	 		System.out.println("An exception occured while reading from socket of user " + userName + e.getMessage() );
   	 	}
   	 	
   	 	while (true)
   	 	{
   	 		// variable to store messages to be relayed such as Would you like to be friends.
   	 		String message;
   	 		try {
   	 			userMessage = input_stream.readLine();
   	 			if (userMessage.equals("LogOut"))
   	 			{
   	 				output_stream.println("### Bye "+ userName+ " ###");
   	 				broadcast_to_rest ("*** A user " + userName + " has left the chat room");
   	 				break;
   	 			}
   	 			else if (userMessage.length() > 9 && userMessage.substring(0,9).equals("#friendme")) //friend request received.
   	 			{
   	 				System.out.println("User is sending a friend request");
   	 				String friendRequested = userMessage.substring(11);
   	 				friendrequests.add(friendRequested);
   	 				// Add a check to see if the friend really exists. If the user does not
   	 				// exist send an error back to the requested user saying user does not exist.
   	 				if (checkIfUserExists(friendRequested))
   	 				{
   	 					message = "Would you like to be friends?";
   	 					unicast(friendRequested, message);
   	 					System.out.println(" Friend requested is " + friendRequested );
   	 				}
   	 				else
   	 				{
   	 					output_stream.println(" Error: Requested User does not exist ");
   	 				}
   	 			}
   	 			else if (userMessage.charAt(0) == '@')
   	 			{
   	 				// There are two cases when this message could be received
   	 				// One when as response to friend request
   	 				// Two when we send a private message
   	 				// Check if it is a friend request response
   	 				int indexOfSpace = userMessage.indexOf(' ');
   	 				System.out.println("index of space is" + indexOfSpace);
   	 				String unicastUser = userMessage.substring(1,indexOfSpace);
   	 				String word1 = userMessage.substring(indexOfSpace+1);
   	 				if (word1.equals("#friends"))
   	 				{
   	 					// add the user that requested the friendship to the current users friend list
   	 					friends.add(unicastUser);
   	 					// adds the userName of the user that accepted the friend request to the friend list of user that requested it;
   	 					if (! addUserToFriendList(unicastUser) )
   	 					{
   	 						output_stream.println("Error :Friend confirmation failed. Either user does not exist or User has not sent you friend request");
   	 					}
   	 					else {
   	 						// relay message to both users that they are friends
   	 						// check if friend has been requested only then accept this or else error 
   	 						message = userName + " and " + unicastUser + " are now friends ";
   	 						announceToFriends(unicastUser, message);
   	 					}
   	 				}
   	 				else if(word1.equals("#unfriend"))
   	 				{
   	 					// check if the user is in friend list
   	 					friends.remove(unicastUser);
   	 					if (friendrequests.contains(unicastUser))
   	 					{
   	 						friendrequests.remove(unicastUser);
   	 					}
   	 					
   	 					deleteUserFromFriendList(unicastUser);
   	 					message = userName + " and " + unicastUser + " are not friends anymore ";
	 					announceToFriends(unicastUser, message);
   	 				}
   	 				else {
   	 					System.out.println("user we are searching for" + unicastUser);
   	 					if (friends.contains(unicastUser))
   	 					{	
   	 						String unicastMessage = userMessage.substring(indexOfSpace+1);
   	 						System.out.println("Message to be sent to " + unicastUser );
   	 						System.out.println("Message is " + unicastMessage );
   	 						unicast(unicastUser , unicastMessage );
   	 					}
   	 					else
   	 					{
   	 						output_stream.println("Not your friend! Please add as a friend");
   	 					}
   	 				}
   	 			}
   	 			else
   	 			{
   	 				//output_stream.println("<" + userName + ">" + userMessage);
   	 				broadcast ("<" + userName + ">" + userMessage);
   	 			}
   	 		} catch (Exception e)
   	 		{
   	 			// If a user disconnects the service this exception will occur
   	 			// So we break out of the while loop so that we do not have infinite loop
   	 			System.out.println("Exception occurred" + userName + e.getMessage());
   	 			break;
   	 		}
   	 	}
   	 	try {
   	 		synchronized(this)
   	 		{
   	 			removeFromUserThread();
   	 		}
   	 		userSocket.close();
   	 	
   	 	}
   	 	catch (Exception e)
   	 	{
   	 		System.out.println("Failed to close userSocket for user " + userName + " " + e.getMessage());
   	 	}
   }

	private void announceToFriends(String user, String message) {
		//String message = userName + " and " + user + " are now friends ";
		synchronized(this) {
			output_stream.println(message);
			for (int i =0; i < maxUsersCount; ++i)
			{
				try {
					if (threads[i].userName.equals(user))
					{
						threads[i].output_stream.println(message);
					}
				}
				catch (Exception e)
				{
					// We will have an exception if threads[i] has null reference
					// This is totally valid and depends on number of users. If the number
					// of users are less than max count, we will have this exception occur.
				}
			}
		}
		
	}
}
