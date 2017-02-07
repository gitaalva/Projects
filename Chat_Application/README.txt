===================
COMPILATION AND RUN
===================

Instructions to run User
javac User.java 
java User <Server IP address> <Port>

Instructions to Run Server
javac Server.java

Note : The Argument MaxUserCount is optional. Default is 5
java Server <PortNumber> <MaxUserCount> 

==========================================
	
The following files implements client and server functionality  

Server:
	Broad Design:
		The server has been designed according to skeleton. The user threads are created and references stored in threads array which is accessible by all threads
		When a new user joins threads[] is updated.
		All access to threads[] are synchronized.
		The run() function in the User performs all the functions of the thread invoking other helper functions.
		The code has been modularized into functions with self evident name.
		
		
Functionalities:
	The Server has been implemented according to the skeleton provided. The Server listens on a port number and accept connections until MaxUserCount is reached
    beyond which the Server does not accept connection. If a client logs out the server then the maxusercount decreases and new connections could be accepted.
	The Server implements all the functionality that are part of the assignment along with additional error condition checks.
	Comments have been added to the code.
	All statements where the threads access common resource have been added under Synchronized this statement.
	Even Broadcast statements to other threads have been added under synchronized so that they could be displayed in the order that the server received.
	Most functionalities with the Server has separate functions with username's that briefly  describe the functionality

Error Conditions:
	The server catches all the exceptions and deals with them rightly. The server ensures that the User thread ends neatly and set to null in case of unexpected behaviour
	1. If the User exits the conversation with Ctrl-C the server catches exception, deletes the user from threads[i] array
	2. FriendRequest Errors:
		When the friendRequest is sent the server check's the user exists and returns error if it does not.
		Security Error: A user can bypass sending request to friends by just sending the command to accept friendRequest. This error condition has been checked.
						A user can confirm friendship only if he has received friend request from that particular user.
	3. Server does not permit the max number of users to extend the maximum count provided. Allows for current user's to exit, which decreases max count and new user
		can join on the fly.
	4. Server does not let the user to provide username with @.
	5. Server catches socket error. Server ensures the threads[i] is set to null when there is an exception and the thread has to exit.
	6. Server displays error when invalid arguments are provided.
						
	

Possible Extenstion and TradeOff :
	1) Server should be able to handle duplicate userNames;
	2) Encryption at both the client and the server end. This will add latency to the application but improve security.
	3) This could also be applied to the client.
	4) Ability to store the conversations between users for specified period.
	5) Ability to store and retrieve User Info based on previous session. For example the friendlist of the User who has logged in.
	
	

User:
The User is a multithreaded client that opens a socket_connection to the server and listens to the socket input_stream on a thread;
The main thread accepts User Input from the standard input
Design Choice: The Design choice is similar to the one given in Skeleton. The listening thread has been create via a new thread instance rather the using
the this.start(). The server has been created according to the Skeleton provided.

Possible Improvements to the User:
	The User could be improved by adding functionalities to add attachments. Currently we only accept text from standard input.
	Error on the client side when server does not accept connection for long.
	Converting the user into a GUI instead of command line.
	Ability to obtain the user's list of friends.

Error Conditions
	When the User enters the wrong ip or the port number.




The following is the chat log of three users User1, User2 and User3

java User localhost 8030

User 1 Logs:

Enter your name
User1
Welcome User1 to our chat room
To leave enter LogOut in a new line
Hello
<User1>Hello
#friendme User2
 Requested User does not exist 
@User2 How are you?
Not your friend! Please add as a friend
*** A new user User2 has entered the chat room
*** A new user User3 has entered the chat room
Hello all
<User1>Hello all
I am just sending a sample broadcast message to check if broadcast works
<User1>I am just sending a sample broadcast message to check if broadcast works
Nice it works
<User1>Nice it works
@user1 what is up?
Not your friend! Please add as a friend
@User1 what is up
Not your friend! Please add as a friend
@User2 what is up?
Not your friend! Please add as a friend
#friendme @User2
User2 and User1 are now friends 
@User2 How are you doing?
<User2>I am good. How are you?
User2 and User1 are not friends anymore 
@User1 reply to me
Not your friend! Please add as a friend
*** A user User2 has left the chat room
LogOut
### Bye User1 ###

	User2 Logs
java User localhost 8030
Enter your name
User2
Welcome User2 to our chat room
To leave enter LogOut in a new line
*** A new user User3 has entered the chat room
<User1>Hello all
<User1>I am just sending a sample broadcast message to check if broadcast works
<User1>Nice it works
<User1>Would you like to be friends?
@User1 #friends
User2 and User1 are now friends 
<User1>How are you doing?
@User1 I am good. How are you?
@User1 #unfriend    
User2 and User1 are not friends anymore 
@User3 #friends
Friend confirmation failed. Either user does not exist or User has not sent you friend request
LogOut
### Bye User2 ###

Enter your name
*** A new user User2 has entered the chat room
User3
Welcome User3 to our chat room
To leave enter LogOut in a new line
<User1>Hello all
<User1>I am just sending a sample broadcast message to check if broadcast works
<User1>Nice it works
*** A user User2 has left the chat room
*** A user User1 has left the chat room
LogOut
### Bye User3 ###