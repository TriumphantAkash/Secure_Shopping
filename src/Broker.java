/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Broker {

	private static final String EOM1_IP = "127.0.0.1";
	private static final String EOM2_IP = "127.0.0.1";
	private static final String EOM3_IP = "127.0.0.1";
	private static final int ECOM1_PORT = 5001;
	private static final int ECOM2_PORT = 5002;
	private static final int ECOM3_PORT = 5003;
	private static final int BROKER_PORT = 5000;

	static ServerSocket serverSock;
	static Socket s;
	static PrintWriter outClient;
	static BufferedReader inClient;
	static Socket conn_ecom;
	static PrintWriter outEcom;
	static BufferedReader inEcom;
	static SecureRandom random;

	static String BESessionID;
	static String BCSessionID;
	static DESEncryption bc_longterm;
	static DESEncryption be_longterm;
	static DESEncryption be_session;
	static DESEncryption bc_session;

	static BufferedReader in_terminal;
	static long be_seed = 12125637;
	static long bc_seed = 13554269;
	static HashMap<String, String > authFile;
	static String u,p ;
	static int clientBucket = 50000, ecom1Bucket = 0;
	static int clientid;
	static String client_id, pid;
	static String amt;

	static String str_pid_Toamnt;
	static String[] pid_Toamnt;
	static int pID;
	static int total_amnt;
	static String receiptc;
	static String receipte;
	static int bytesRead;
	static int current = 0;
	static InputStream is_c;
	static OutputStream os_c;

	static byte[] mybytearray;
	static InputStream is;
	static FileOutputStream fos;
	static BufferedOutputStream bos;

	int which_ecom()
	{

		System.out.println("Hi");
		return 0;
	}


	public static void main(String[] args) throws IOException, InterruptedException, Exception
	{
		new Broker();

		BCSessionID = bc_longterm.decrypt(inClient.readLine());
		//System.out.println("Session Key BC:"+ BCSessionID);
		bc_session = new DESEncryption(BCSessionID);


		//receiving client input for catalog request
		String client_input = bc_session.decrypt(inClient.readLine());
		int c_input_int = Integer.parseInt(client_input);
		switch (c_input_int)
		{
		case 1:
			System.out.println("Broker connecting to ECOM1");
			sessionWithEcom(EOM1_IP, ECOM1_PORT);
			String temp = inEcom.readLine();
			//sending catalog to client
			outClient.println(temp);

			//sending encrypted purchase information to ECOM1
			outEcom.println(inClient.readLine());

			//forwarding str_pid_Toamnt to Client
			outClient.println(inEcom.readLine());

			//reading orderid+ total payable amount sent from Client
			str_pid_Toamnt = bc_session.decrypt(inClient.readLine());
			pid_Toamnt = str_pid_Toamnt.split("\\|");

			int pID = Integer.parseInt(pid_Toamnt[0]);
			pid = pid_Toamnt[0];
			int total_amnt = Integer.parseInt(pid_Toamnt[1]);
			amt = pid_Toamnt[1];

			// perform money transaction from client bucket to ECom1 bucket
			// perform money transaction from client bucket to ECom1 bucket\
			if ( total_amnt > clientBucket) {
				String error_msg = "Insufficient Funds!";
				outClient.println(bc_session.encrypt(error_msg));
				System.out.println("client has insufficient funds..so exiting!");
				System.exit(0);
			} else {
				clientBucket -= total_amnt;
				ecom1Bucket += total_amnt;
				//sending client and ECOM1 notification about the money transaction
				receiptc = "[Transaction Confirmation Receipt]"+"|"+Integer.toString(pID)+"|"+Integer.toString(total_amnt)+"|"+Integer.toString(clientBucket);
				outClient.println(bc_session.encrypt(receiptc));
				receipte = "[Transaction Confirmation Receipt]"+"|"+Integer.toString(pID)+"|"+Integer.toString(total_amnt)+"|"+Integer.toString(ecom1Bucket);
				outEcom.println(be_session.encrypt(receipte));
				//******************************************************************************	
				//reading file that broker wrote on my io stream

				// receive file
				byte[] mybytearray  = new byte [100000];
				InputStream is = conn_ecom.getInputStream();
				// FileOutputStream fos = new FileOutputStream("broker_to_ecom");
				//   BufferedOutputStream bos = new BufferedOutputStream(fos);
				bytesRead = is.read(mybytearray,0,mybytearray.length);
				System.out.println("Number of bytes sending to client : "+bytesRead);
				//	      current = bytesRead;
				/*
    	      do {
    	         bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
    	         if(bytesRead >= 0) current += bytesRead;
    	      } while(bytesRead > -1);
    	      //System.out.println( mybytearray+"................'"+current+"..."+mybytearray.length);
				 */ 	      
				//    	      bos.write(mybytearray, 0 , mybytearray.length);
				//    	      bos.flush();
				//    	      System.out.println("File " + "ecom_to_broker"
				//    	          + " downloaded (" + current + " bytes read)");


				//writing product on client socket
				os_c.write(mybytearray,0,mybytearray.length);
				os_c.flush();
				System.out.println("product sent to client");
				//*****************************************************************************
			}
			break;

		case 2:
			System.out.println("Broker connecting to ECOM2");
			sessionWithEcom(EOM2_IP, ECOM2_PORT);
			outClient.println(inEcom.readLine());

			//sending encrypted purchase information to ECOM1
			outEcom.println(inClient.readLine());

			//forwarding str_pid_Toamnt to Client
			outClient.println(inEcom.readLine());

			//reading orderid+ total payable amount sent from Client
			str_pid_Toamnt = bc_session.decrypt(inClient.readLine());

			//Broker should store this purchase information in a file here
			pid_Toamnt = str_pid_Toamnt.split("\\|");

			pID = Integer.parseInt(pid_Toamnt[0]);
			//////////////////////////////////////////////////////////////////////////////////
			pid = pid_Toamnt[0];
			total_amnt = Integer.parseInt(pid_Toamnt[1]);
			//////////////////////////////////////////////////////////////////////////////////
			amt = pid_Toamnt[1];
			System.out.println("So, the purchase ID and total amount is: "+ pID+" and "+ total_amnt);

			// perform money transaction from client buckt to ECom1 bucket

			clientBucket -= total_amnt;
			ecom1Bucket += total_amnt;


			//sending client and ECOM1 notification about the money transaction
			receiptc = "[Transaction Confirmation Receipt]"+"|"+Integer.toString(pID)+"|"+Integer.toString(total_amnt)+"|"+Integer.toString(clientBucket);
			receipte = "[Transaction Confirmation Receipt]"+"|"+Integer.toString(pID)+"|"+Integer.toString(total_amnt)+"|"+Integer.toString(ecom1Bucket);

			//  System.out.println("sending the following message to Client:\n"+receipt);
			outClient.println(bc_session.encrypt(receiptc));
			outEcom.println(be_session.encrypt(receipte));


			//******************************************************************************	
			//reading file that broker wrote on my io stream

			// receive file
			mybytearray  = new byte [100000];
			is = conn_ecom.getInputStream();
			//   	     fos = new FileOutputStream("broker_to_ecom");
			//  	      bos = new BufferedOutputStream(fos);
			bytesRead = is.read(mybytearray,0,mybytearray.length);
			//	      current = bytesRead;
			/*
    	      do {
    	         bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
    	         if(bytesRead >= 0) current += bytesRead;
    	      } while(bytesRead > -1);
    	      //System.out.println( mybytearray+"................'"+current+"..."+mybytearray.length);
			 */ 	      
			//  bos.write(mybytearray, 0 , mybytearray.length);
			// bos.flush();
			// System.out.println("File " + "ecom_to_broker"
			//   + " downloaded (" + current + " bytes read)");


			//writing product on client socket
			os_c.write(mybytearray,0,mybytearray.length);
			os_c.flush();
			System.out.println("product sent to client");
			//*****************************************************************************

			break;
		case 3:
			System.out.println("Broker connecting to ECOM3");
			sessionWithEcom(EOM3_IP, ECOM3_PORT);
			outClient.println(inEcom.readLine());

			//sending encrypted purchase information to ECOM1
			outEcom.println(inClient.readLine());

			//forwarding str_pid_Toamnt to Client
			outClient.println(inEcom.readLine());

			//reading orderid+ total payable amount sent from Client
			str_pid_Toamnt = bc_session.decrypt(inClient.readLine());

			//Broker should store this purchase information in a file here
			pid_Toamnt = str_pid_Toamnt.split("\\|");

			pID = Integer.parseInt(pid_Toamnt[0]);

			pid = pid_Toamnt[0];
			total_amnt = Integer.parseInt(pid_Toamnt[1]);

			amt = pid_Toamnt[1];
			System.out.println("So, the purchase ID and total amount is: "+ pID+" and "+ total_amnt);

			// perform money transaction from client bucket to ECom1 bucket

			clientBucket -= total_amnt;
			ecom1Bucket += total_amnt;


			//sending client and ECOM1 notification about the money transaction
			receiptc = "[Transaction Confirmation Receipt]"+"|"+Integer.toString(pID)+"|"+Integer.toString(total_amnt)+"|"+Integer.toString(clientBucket);
			receipte = "[Transaction Confirmation Receipt]"+"|"+Integer.toString(pID)+"|"+Integer.toString(total_amnt)+"|"+Integer.toString(ecom1Bucket);

			//  System.out.println("sending the following message to Client:\n"+receipt);
			outClient.println(bc_session.encrypt(receiptc));
			outEcom.println(be_session.encrypt(receipte));

			//******************************************************************************	
			//reading file that broker wrote on my io stream

			// receive file
			mybytearray  = new byte [100000];
			is = conn_ecom.getInputStream();
			//  fos = new FileOutputStream("broker_to_ecom");
			//   bos = new BufferedOutputStream(fos);
			bytesRead = is.read(mybytearray,0,mybytearray.length);
			System.out.println("bytes read on broker : "+bytesRead);
			// 	      current = bytesRead;
			/*
    	      do {
    	         bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
    	         if(bytesRead >= 0) current += bytesRead;
    	      } while(bytesRead > -1);
    	      //System.out.println( mybytearray+"................'"+current+"..."+mybytearray.length);
			 */ 	      
			// bos.write(mybytearray, 0 , mybytearray.length);
			// bos.flush();
			// System.out.println("File " + "ecom_to_broker"
			//    + " downloaded (" + current + " bytes read)");


			//writing product on client socket
			os_c.write(mybytearray,0,mybytearray.length);
			os_c.flush();
			System.out.println("product sent to client");
			//*****************************************************************************

			break;
		default:
			System.out.println("I don't think it will ever come here");
			break;
		}


		String msg = client_id+"\t"+pid+"\t"+amt+"\t";
		Writer w = new Writer("BrokerLogs");
		w.write(msg, be_longterm);
		s.close();

		serverSock.close();
		System.out.println("Broker closed");


	}

	public Broker() throws Exception {
		try {
			random = new SecureRandom();
			System.out.println("\n**************************************************");
			System.out.println("****************** Broker Started ****************");
			System.out.println("**************************************************");
			//Borker as server
			serverSock = new ServerSocket(BROKER_PORT);

			s = serverSock.accept();

			//Communication objects for broker
			is_c = s.getInputStream();
			os_c = s.getOutputStream();
			outClient = new PrintWriter(os_c, true);
			inClient = new BufferedReader(new InputStreamReader(is_c));
			System.out.println("Broker listening on port " + 5000);
			authFile = new HashMap<>();
			estKeys();


			//signUp();


		} catch (IOException ex) {
			Logger.getLogger(Broker.class.getName()).log(Level.SEVERE, null, ex);

		}
		in_terminal = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("In constructor ");
	}

	public static void sessionWithEcom(String ecomIP, int ecom_port) throws IOException
	{
		//Broker as client
		conn_ecom = new Socket(ecomIP, ecom_port);

		//communication objects to Ecom
		outEcom = new PrintWriter(conn_ecom.getOutputStream(), true);
		inEcom = new BufferedReader(new InputStreamReader(conn_ecom.getInputStream()));
		System.out.println("************Session key BE:"+BESessionID);
		System.out.println("Encrypted BE session key"+be_longterm.encrypt(BESessionID));
		outEcom.println(be_longterm.encrypt(BESessionID));
	}
	public static String nextSessionId() {

		return new BigInteger(130, random).toString(32);
	}

	public  void signUp(){
		u="akash";
		p=new Hash().getHash("linux");


	}
	public static void estKeys() throws Exception {
		//generate long term session key for broker

		be_longterm = new DESEncryption(Long.toString(new Random(be_seed).nextLong()));
		bc_longterm = new DESEncryption(Long.toString(new Random(bc_seed).nextLong()));
		/////////////////////////////////////////////////////////////////////////////////
		clientid = new Random().nextInt();
		BESessionID = nextSessionId();
		be_session = new DESEncryption(BESessionID);

	}

}
