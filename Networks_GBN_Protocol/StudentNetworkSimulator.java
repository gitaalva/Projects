import java.util.*;
import java.io.*;
import java.security.MessageDigest;

public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B 
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

	/*
	 * @aalva Cases To be handled;
	 * 		What happens when a timer TimesOut
	 * 			Guess it will be handled: We will check with test cases;
	 */
    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    int previousAckNum = -1;
    
    //statistics variable
    //
    
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)
    int sequenceNumber; 						// variable that stores sequence number for A;
    int checksumvalue;
    private int expectedAckNumber;
    private int expectedSequenceNumberB;
    Boolean isHeadReTransmitted = false;
    Double averageRTT = 0.0;
    Double communicationTime = 0.0;
    int withoutRTCount = 0;
    // @aalva
    /* This variable checks for two conditions to determine when aoutput should call sendPacketToB()
       If the variable is true then a_output should call sendPacketToB()
       		This is true in the beginning when our program starts and we call sendPacketToB() and set this variable to false;
    		as we should be sending packets only after receiving acknowledgements;
    		We set this variable to true once again when we have received all the acknowledgements and there is no message waiting in
    		senders queue to be sent to be. In this case, we need to wait for a_output call from above which should again call sendPacketToB()
    		Hence we set this value to true again and whenever a_output is called it will call sendPacketToB()
    */
    Boolean isFirstPacket; 						 
    Queue<Message> layer5QueueA; 				// @aalva a queue that stores layer 5 messages from A when send
    							 				// window is full. If this queue is full program will exit.
    int queueMaxSize = 3000; 		 				// @aalva above this size we will exit the window.
    Queue<Packet> sendWindow;
    Queue<Double> senderTimeQueue;
    HashMap<Integer, Packet> receiverWindow;
    
    // to store the beginning and end of the expected sequence number range in both A and B;
    int begA;
    int endA;
    int begB;
    int endB;
    
    Boolean isNacked;
    double firstPacketTime;
    Boolean isfirstPackeTimeSet = false;
    double lastPacketTime;
    double previousDupAck = 0.0;
    
    
    // statistics variable
    int numOrigPackets;
    int numReTransmission;
    int numOfAckPackets;
    int numOfCorruptedPackets;
    int numOfCorruptedAcks;
    int numOfSuccessfulTransfer;
  
    
    
   // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
        WindowSize = winsize;
        LimitSeqNo = winsize+1;
        RxmtInterval = delay;
    }

   
    int calculateCheckSum ( String data )
    {
    	int checksum = 0;
    	int i = 0;
    	while ( i < data.length() )
    	{
    		checksum += (int) data.charAt(i);
    		++i;
    	}
    	checksum = ~checksum;	// @aalva is this operation really required
    	return checksum;
    }
    
    int checkShouldBe(Packet packet) 
    {
      char[] data = new char[1000];
      int  checksum;
      data = packet.getPayload().toCharArray();
      checksum = 0;
      int i=0;
    
      while( i < data.length ) {
    	 
        checksum += data[i];
        i++;
      }
      checksum += packet.getAcknum() + packet.getSeqnum();
      checksum = ~checksum;

      return checksum;
    }
    
    // some notes on selective repeat;
    // sender must retransmit all unacked packets on timeout
    // as soon as an ack is received sender removes the item from it's window
    // what do we do with corrupted ack (ignore) and wait for timeout
    // important function is the currentSequence number range;
    // To recognize duplicate ack
    
    void sendPacketToB ()
    {
    	while (sendWindow.size() < WindowSize )
    	{
    		if( layer5QueueA.isEmpty())
        	{
    			isFirstPacket = true; // bad variable name 
        		return ;
        	}
    		Message message = layer5QueueA.remove();
    		sequenceNumber = sequenceNumber%LimitSeqNo;
    		endA = sequenceNumber;
    		expectedAckNumber = sequenceNumber;
    		System.out.println("Sending packet with sequence number " + sequenceNumber);
    		int checkSumValue = calculateCheckSum(message.getData()+ sequenceNumber );
    		int[] dummySack = new int[5];
    		Packet aOutputPacket = new Packet(sequenceNumber,sequenceNumber, checkSumValue, message.getData(),dummySack);
    		sendWindow.add(aOutputPacket);
    		sequenceNumber += 1;
    		//if (senderTimeQueue.size() == 0)
    		//{
    		stopTimer(A); // We will get a warning in the beginning which we will ignore
    		startTimer (A,RxmtInterval); 
    		if (!isfirstPackeTimeSet)
    		{
    			isfirstPackeTimeSet = true;
    			firstPacketTime = getTime();
    			System.out.println("First time" + firstPacketTime);
    		}
    		Double timeInOfPacket = getTime();
    		System.out.println("Setting time in of packet as " + timeInOfPacket);
    		senderTimeQueue.add(timeInOfPacket);
    		numOrigPackets += 1;
    		toLayer3(A, aOutputPacket);
    	}
    }
    
    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	System.out.println("StudentNetworkSimulator::aOutput begin");
    	if ( layer5QueueA.size() == queueMaxSize )
    	{
    		System.out.println("Sender Buffer Exceeded! Exiting the program");
    		System.exit(1);
    	}
    	else {
    		layer5QueueA.add(message);
    		if ( sendWindow.size() < WindowSize ) {
    			sendPacketToB();
    			//isFirstPacket = false;
    		}
    	}
    }
    
    // check's if the sequenceNumber is in range
    // Since we have a sliding window of sequenceNumbers
    Boolean aSequenceInRange (int seqNum, int beg, int end )
    {
    	if ( beg <= end )
    	{
    		if ( seqNum >= beg && seqNum <= end )
    		{
    			return true;
    		}
    	}
    	if (beg > end )
    	{
    		if ( seqNum >= beg && seqNum < LimitSeqNo )
    		{
    			return true;
    		}
    		else if (seqNum <= end )
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    Boolean checkDuplicateAck(int ackNum)
    {
    	if (ackNum == previousAckNum)
    	{
    		return true;
    	}
    	return false;
    }
    
    // This function is used to retransmit packet when we have duplicate timeOut
    void reTransmitPacket(Packet ackPacket)
    {
    	System.out.println("Duplicate Ack Selective Ack Retransmit " );
    	int[] sack = ackPacket.getSack();
    	if ( !sendWindow.isEmpty() )
    	{
    		System.out.println("Retransmitting packet with sequence number " + sendWindow.peek().getSeqnum());
    		for(Packet p: sendWindow)
    		{
    			if ( Arrays.binarySearch(sack, p.getSeqnum()) >= 0 )
    			{
    				System.out.println("Packet " + p.getSeqnum() + " selectively acknowledged! No retransmit ");
    			}
    			else
    			{
    				// sending packet to Layer3
    				stopTimer(A);
    				//startTimer (A,RxmtInterval);
    				
    				toLayer3(A,p);
    			}
    		}
    		/*
    		numReTransmission += 1;
    		//resetTimerQueue(getTime());
    		isHeadReTransmitted = true;
    		toLayer3(A, sendWindow.peek());
    		startTimer (A,RxmtInterval);
    		*/
    	}
    }
    
    // This function is used when we have to retransmit packet when we don't have duplicate timeout;
    void reTransmitPacket()
    {
    	System.out.println("Time Out : Retransmitting all packets");
    	while ( !sendWindow.isEmpty() )
    	{
    		for ( Packet p:sendWindow )
    		{
    			Packet newp = new Packet(p.getSeqnum(),p.getAcknum(),p.getChecksum(),new int[5]);
    			stopTimer(A);
				startTimer (A,RxmtInterval);
				toLayer3(A,newp);
    		}
    	}
    }
    
    void removeSackedItems(Packet p)
    {
    	int[] sack = p.getSack();
    	while (!sendWindow.isEmpty())
    	{
    		// implies first item in the window is selectively acked
    		// We will move the window forward
    		if ( Arrays.binarySearch(sack, sendWindow.peek().getSeqnum()) >= 0 )
    		{
    			sendWindow.remove();
    		}
    		else
    		{
    			return;
    		}
    	}
    }
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
    	System.out.println("A::Received ack for sequence number " + packet.getAcknum());
    	if ( calculateCheckSum(new Integer(packet.getAcknum()).toString()) == packet.getChecksum() )
    	{
    		numOfAckPackets += 1;
    		if (!aSequenceInRange(packet.getAcknum(),begA,endB))
    		{
    			// check if Duplicate ack and check if previous ack was sent before RTT
    			// No need to retransmit if RTT has not elapsed since previous retransmit.
    			if(checkDuplicateAck(packet.getAcknum()) && getTime()-previousDupAck >= RxmtInterval )
    			{
    				System.out.println("A::Duplicate Ack Received");
    				reTransmitPacket(packet);
    				previousDupAck = getTime();
    			}
    		}
    		else 
    		{
    			// This is the case where we received a valid acknum. Since it's cumulative and in range;
    			double time = getTime();
    			System.out.println("Time is " + time);
    			//iterate until you reach the ack number in the window. Remove all the previous packets from window;
    			while (!sendWindow.isEmpty() && sendWindow.peek().getSeqnum() != packet.getAcknum() )
    			{
    				//previousAckNum = packet.getAcknum();
    				System.out.println("Cumulative Ack Received! Moving window");
    				numOfSuccessfulTransfer += 1;
    				if ( !isHeadReTransmitted )
        			{
        				if (!senderTimeQueue.isEmpty())
        				{
        					System.out.println("Time in Queue for packet " + packet.getAcknum() + " is " + senderTimeQueue.peek() );
        					averageRTT = averageRTT + ( time - senderTimeQueue.peek() );
        					withoutRTCount += 1;
        				}
        			}
    				else
    				{
    					isHeadReTransmitted = false;
    					communicationTime = communicationTime +  ( time - senderTimeQueue.peek() );
    				}
    				sendWindow.remove();
    				senderTimeQueue.remove();
    				begA = (begA+1)%LimitSeqNo;
    			}
    			if ( !isHeadReTransmitted )
    			{
    				System.out.println("Time in Queue for packet " + packet.getAcknum() + " is " + senderTimeQueue.peek() );
    				averageRTT = averageRTT + ( time - senderTimeQueue.peek() );
    				communicationTime = communicationTime +  ( time - senderTimeQueue.peek() );
    				withoutRTCount += 1;
    			}
    			else
    			{
    				communicationTime = communicationTime +  ( time - senderTimeQueue.peek() );
    				isHeadReTransmitted = false;
    			}
    			previousAckNum = packet.getAcknum(); // To check for Duplicate Acks we need the previous acknum.
    			sendWindow.remove();
    			// We remove the items from the sendWindow which have been selectively acked along with our ack.
    			removeSackedItems(packet);
    			senderTimeQueue.remove();
    			begA = (begA+1)%LimitSeqNo;
    			lastPacketTime = getTime();
    			numOfSuccessfulTransfer += 1;
    			if (!sendWindow.isEmpty())
    				System.out.println("Cumulative Ack Received! Send Window Start Moved to " + sendWindow.peek().getSeqnum() );
    			sendPacketToB();
    		}
    	}
    	else
    	{
    		numOfCorruptedAcks += 1; 
    		System.out.println("Received Corrupt Ack! Dropping it");
    	}
    }
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	if (!senderTimeQueue.isEmpty() && senderTimeQueue.peek() <= getTime() )
    	{
    		System.out.println("Time Out");
    		reTransmitPacket();
    	}
    	else
    	{
    		System.out.println("Timer Interrupt but all packets were sent");
    		startTimer(A,RxmtInterval);
    	}
    }
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
    	isFirstPacket = true;
    	layer5QueueA = new LinkedList<Message>(); 
    	sendWindow = new LinkedList<Packet>();
    	senderTimeQueue = new LinkedList<Double>();
    	begA = 0;
    	endA = WindowSize-1;
    	numOrigPackets = 0;
        numReTransmission = 0;
        numOfAckPackets = 0;
        numOfCorruptedPackets = 0;
        numOfCorruptedAcks = 0;
        averageRTT = 0.0;
        //firstPacketTime = getTime();
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    /*
    void deliverAndSlideWindow()
    {
    	while ( receiverWindow.get(expectedSequenceNumberB) )
    	{
    		
    	}
    }
    */
    int[] getSackArrayReceiver (int beg )
    {
    	int count = 0;
    	int[] sack = new int[5];
    	Arrays.fill(sack,-1);
    	while( aSequenceInRange (beg, begB, endB ) && count < 5)
		{
			if ( receiverWindow.containsKey(beg) )
			{
				sack[count] = beg;
				System.out.println(beg);
			}
			beg = beg+1% LimitSeqNo;
		}
    	return sack;
    }
    protected void bInput(Packet packet)
    {
    	// check if the packet is corrupted.
    	if (packet.getChecksum() == calculateCheckSum(packet.getPayload() + packet.getSeqnum()) )
    	{
    		// If the sequence number is in range
    		if ( aSequenceInRange (packet.getSeqnum(), begB, endB ) )
    		{
    			// store packet in map
    			if (receiverWindow.size() == LimitSeqNo )
    			{
    				System.out.println( "B::Error:: Receiver Window Limit Exceeded ");
    			}
    			receiverWindow.put(packet.getSeqnum(), packet);
    			if ( packet.getSeqnum() == expectedSequenceNumberB )
    			{
    				while ( receiverWindow.containsKey(expectedSequenceNumberB))
    		    	{
    		    		Packet expectedPacket = receiverWindow.get(expectedSequenceNumberB);
    		    		receiverWindow.remove(expectedSequenceNumberB);
    					toLayer5(expectedPacket.getPayload());
    					expectedPacket.setChecksum (calculateCheckSum(new Integer(expectedSequenceNumberB).toString() ) );
    					System.out.println("The SACK packets are ");
    					expectedPacket.setSack(getSackArrayReceiver((begB+1) % LimitSeqNo ));
    					toLayer3(B, expectedPacket);
    					expectedSequenceNumberB = (expectedSequenceNumberB + 1)%LimitSeqNo;
    					begB = expectedSequenceNumberB;
    					endB = (endB+1)%LimitSeqNo;
    					System.out.println("Delivered and Acked Packet Number " + expectedPacket.getSeqnum() );
    		    	}
    			}
    			else 
    			{
    				// Out of order packet
    				String message = "";
    				int acknum;
    				if (expectedSequenceNumberB == 0)
    				{
    					acknum = LimitSeqNo -1;
    				}
    				else
    				{
    					acknum = expectedSequenceNumberB-1;
    				}
    				System.out.println("B::Out of Order Packet Received with seqnum " + packet.getSeqnum() + " Sending Duplicate Ack with acknum " + acknum );
    				int checksum = calculateCheckSum( new Integer(acknum).toString() );
    				System.out.println("The SACK packets are "); // Next line calls getting sack function that prints out sack values
    				Packet dupAckPacket = new Packet(acknum,acknum,checksum, message,getSackArrayReceiver((begB+1) % LimitSeqNo ));
    				toLayer3(B,dupAckPacket);
    			}
    		}
    		else 
    		{
    			// Old packet received. Send cumulative ack of the packet before the expected packet.
				String message = "";
				int acknum;
				if (expectedSequenceNumberB == 0)
				{
					acknum = LimitSeqNo -1;
				}
				else
				{
					acknum = expectedSequenceNumberB-1;
				}
				System.out.println("B::Out of Order Packet Received with seqnum " + packet.getSeqnum() + " Sending Duplicate Ack with acknum " + acknum );
				int checksum = calculateCheckSum( new Integer(acknum).toString() );
				System.out.println("The SACK packets are ");
				Packet dupAckPacket = new Packet(acknum,acknum,checksum, message,getSackArrayReceiver((begB+1) % LimitSeqNo ));
				toLayer3(B,dupAckPacket);
    		}
    	}
    	else
    	{
    		System.out.println("B:: corrupt packet received! - Dropping it" );
    		numOfCorruptedPackets += 1;
    	}
    }
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	expectedSequenceNumberB = 0;
    	begB = 0;
    	endB = WindowSize-1;
    	isNacked = false;
    	receiverWindow = new HashMap<Integer, Packet>();
    }

    // Use to print final statistics
    protected void Simulation_done()
    {
    	
    	System.out.println (" The total number of packets is " + numOrigPackets );
    	System.out.println (" The total number of valid acks is" + numOfAckPackets );
    	System.out.println(" The total number of corrupted acks is " + numOfCorruptedAcks);
    	System.out.println(" The total number of corrupted packets is " + numOfCorruptedPackets );
    	System.out.println(" The total number of retransmission is " + numReTransmission );
    	System.out.println( "The total number of succesfull transmission is" + numOfSuccessfulTransfer);
    	System.out.println("Without RTCount" + withoutRTCount);
    	System.out.println("Total communication time is" + averageRTT);
    	averageRTT = averageRTT/withoutRTCount;
    	
    	System.out.println("lastPacketTime is" + new Double(lastPacketTime - firstPacketTime).toString() );
    	communicationTime = communicationTime/numOfSuccessfulTransfer;
    	System.out.println( "Communication Time" + communicationTime);
    	System.out.println("Average RTT without retransmission " + averageRTT );
    }	
}
