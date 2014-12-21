import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.NoSuchPaddingException;

public class Client {

	private static final String BROKER_IP = "127.0.0.1";
	private static final int BROKER_PORT = 5000;
	static Socket s;
	static PrintWriter outToBroker;
	static BufferedReader inputFromBroker;
	static Scanner sc;
	private static SecureRandom random;
	/**
	 * 
	 */
	static long cb_seed = 13554269;
	static long ce1_seed = 63521233;
	static long ce2_seed = 63521234;
	static long ce3_seed = 63521235;

	static DESEncryption cb_longterm;
	static DESEncryption ce1_longterm;
	static DESEncryption ce2_longterm;
	static DESEncryption ce3_longterm;
	static DESEncryption cb_session;
	static String CBSessionID, str;
	static int i, j , k;

	static boolean valid=false;
	static boolean flag=false;
	static String catalogE;
	static String[] products;
	static String str_pid_Toamnt;
	static String[] pid_Toamnt;
	static int pID;
	static int total_amnt ;

	static boolean flag3;
	static boolean flag5 = false;

	static String confirm;

	static String[] receipt;
	static    String pid, amt_d,amt_bal;
	static InputStream is;
	static OutputStream os;

	//********************************************************************************************
	static byte[] mybytearray;
	static FileOutputStream fos;
	static BufferedOutputStream bos;
	static int  bytesRead;
	static int current = 0;

	//********************************************************************************************

	public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, NoSuchPaddingException, Exception {
		new Client();
		int []prods = new int[3];

		System.out.println("\n*************************************************************************");
		System.out.println("****************** Welcome to Online Broker System **********************");
		System.out.println("*************************************************************************");
		sc = new Scanner(System.in);


		while(!valid)
		{
			System.out.println("----------------------------");
			System.out.print("Enter your username : ");
			String uname = sc.next();

			System.out.print("Enter password : ");
			String passHash;
			if (System.console() == null) {
				passHash = new Hash().getHash(sc.next());
			} else {
				char passwordArray[] = System.console().readPassword();
				passHash = new Hash().getHash(new String(passwordArray));

			}
			System.out.println("----------------------------\n");
			//bs1
			if(isLogged(uname,passHash)){
				valid=true;
			} else {
				valid = false;
				System.out.println("The Username and Password you have typed doesn't match");
			}

		}


		SendToBroker(cb_longterm.encrypt(CBSessionID));
		//System.out.println("Session Key cb: "+ CBSessionID);



		System.out.println("Please select the product catagory..\n---------------\n 1) for Movies \n 2) for Music\n 3) for ebooks\n99) for exit\n-------------");

		i = sc.nextInt();
		do{
			switch (i)
			{
			case 1:
				//sending client 1 so as to make a session with ECOM1
				SendToBroker(cb_session.encrypt("1"));

				//receiving catalog from broker
				String temp = inputFromBroker.readLine();
				catalogE = ce1_longterm.decrypt(temp);
				products = catalogE.split("\\|");


				do{
					System.out.println("Select the product(s):");
					System.out.println("------------------------------------");
					for(int i = 0; i<3;i++)
						System.out.println(products[i]);
					System.out.println("------------------------------------");
					j = sc.nextInt();
					if (j < 1 || j > 3) {
						System.out.println("please input a valid option\n");
						flag = true;
						continue;
					}
					j = j - 1;
					do{
						System.out.println("quantity (at least one unit)?");
						k = sc.nextInt();
						prods[j] += k;
					} while(prods[j] == 0);

					do{
						System.out.println("1)Continue shopping			2)View Cart and Checkout");
						j = sc.nextInt();
						if (j == 1)
							flag = true;
						else if (j == 2){
							flag = false;
							break;
						}
					}while(j!=1&&j!=2);

				}while(flag);
				System.out.println("*************** your cart *****************");
				for(int l =0; l<3; l++) {
					System.out.print(products[l]+"\t");
					System.out.println(prods[l]);
				}
				System.out.println("******************************************");
				//making a string of purchased products to send it to client through Broker

				str = Integer.toString(prods[0])+"|"+Integer.toString(prods[1])+"|"+Integer.toString(prods[2]);

				outToBroker.println(ce1_longterm.encrypt(str));

				//receiving purchase ID and total purchase amount from ECOM1 through Broker
				String str_pid_Toamnt = ce1_longterm.decrypt(inputFromBroker.readLine());
				String[] pid_Toamnt = str_pid_Toamnt.split("\\|");
				int pID = Integer.parseInt(pid_Toamnt[0]);
				pid = pid_Toamnt[0];
				int total_amnt = Integer.parseInt(pid_Toamnt[1]);
				System.out.println("Order ID ; "+pID+"\t"+"Total payable Amount : "+total_amnt);
				System.out.println("******************************************");
				boolean flag3 = false;
				do{
					System.out.println("place order?(1 for Yes OR 2 for No)");
					int i = sc.nextInt();

					if (i == 1){
						System.out.println("please wait...placing order");
						for (int zz = 0; zz < 5; zz++) {
							System.out.println(".");
							TimeUnit.MILLISECONDS.sleep(300);
						}
						//sending amount payable and order number to Broker
						outToBroker.println(cb_session.encrypt(str_pid_Toamnt));
						flag3 = false;
					}
					else if (i == 2){
						System.out.println("Transaction cancelled!");
						return;
					}
					else
						flag3 = true;
				}while(flag3);


				

				String confirm = cb_session.decrypt(inputFromBroker.readLine());

				if (confirm.equals("Insufficient Funds!"))
				{
					System.out.println(confirm);
					System.exit(0);
				} else {
					System.out.println("Why the hell did it come here!!!!!!");
					String[] receipt = confirm.split("\\|");

					System.out.println(receipt[0]+"\n"+"----------------------------------");
					System.out.println("Order ID\t\t"+receipt[1]);
					System.out.println("Amount debited\t\t"+receipt[2]);
					System.out.println("Account Balance\t\t"+receipt[3]);
					System.out.println("----------------------------------");

					pid = receipt[1];
					amt_d = receipt[2];
					amt_bal = receipt[3];
					Writer w =new Writer("ClientLogs");
					String msg = pid +"\t"+ amt_d+"\t"+amt_bal;
					w.write(msg, cb_longterm);

					//********************************************************************************************
					mybytearray  = new byte [100000];
					fos = new FileOutputStream("Received_Product1");
					bos = new BufferedOutputStream(fos);
					bytesRead = is.read(mybytearray,0,mybytearray.length);
					//System.out.println("bytes read here is ; "+bytesRead);

					//os.write(mybytearray, 0 , mybytearray.length);
					/*		do {
					bytesRead =
							is.read(mybytearray, current, (mybytearray.length-current));
					if(bytesRead >= 0) current += bytesRead;
				} while(bytesRead > -1);
					 */

					//bos.write(mybytearray, 0 , current);
					bos.write(mybytearray, 0 , bytesRead);
					bos.flush();
					//********************************************************************************************
				}
				break;

			case 2:
				//  System.out.println("Attempting to connect to ECOM2");
				SendToBroker(cb_session.encrypt("2"));
				catalogE = ce2_longterm.decrypt(inputFromBroker.readLine());
				products = catalogE.split("\\|");
				System.out.println("Please start your shopping!");
				do{
					System.out.println("Select the product(s):");
					for(int i = 0; i<3;i++)
						System.out.println(products[i]);

					j = sc.nextInt();
					if (j < 1 || j > 3) {
						System.out.println("please input a valid option");
						flag = true;
						continue;
					}
					j = j - 1;
					do{
						System.out.println("quantity (at least one unit)?");
						k = sc.nextInt();
						prods[j] += k;
					} while(prods[j] == 0);

					do{
						System.out.println("1)Continue shopping			2)View Cart and Checkout");
						j = sc.nextInt();
						if (j == 1)
							flag = true;
						else if (j == 2){
							flag = false;
							break;
						}
					}while(j!=1&&j!=2);

				}while(flag);
				System.out.println("*************** your cart *****************");
				for(int l =0; l<3; l++) {
					System.out.print(products[l]+"\t");
					System.out.println(prods[l]);
				}
				System.out.println("******************************************");

				//making a string of purchsed products to send it to client through Broker

				str = Integer.toString(prods[0])+"|"+Integer.toString(prods[1])+"|"+Integer.toString(prods[2]);

				outToBroker.println(ce2_longterm.encrypt(str));

				//receiving purchase ID and total purchase amount from ECOM1 through Broker
				str_pid_Toamnt = ce2_longterm.decrypt(inputFromBroker.readLine());
				pid_Toamnt = str_pid_Toamnt.split("\\|");
				pID = Integer.parseInt(pid_Toamnt[0]);
				total_amnt = Integer.parseInt(pid_Toamnt[1]);
				System.out.println("Order ID ; "+pID+"\t"+"Total payable Amount : "+total_amnt);
				System.out.println("******************************************");
				flag3 = false;
				do{
					System.out.println("place order?1 for Yes OR 2 for No");
					int i = sc.nextInt();

					if (i == 1){
						System.out.println("please wait...placing order");
						for (int zz = 0; zz < 5; zz++) {
							System.out.println(".");
							TimeUnit.MILLISECONDS.sleep(500);
						}
						flag3 = false;
					}
					else if (i == 2){
						System.out.println("Transaction cancelled!");
						return;
					}
					else
						flag3 = true;
				}while(flag3);

				//sending amount payable and order number to Broker
				outToBroker.println(cb_session.encrypt(str_pid_Toamnt));

				confirm = cb_session.decrypt(inputFromBroker.readLine());

				receipt = confirm.split("\\|");

				System.out.println(receipt[0]+"\n"+"----------------------------------");
				System.out.println("Order ID\t\t"+receipt[1]);
				System.out.println("Amount debited\t\t"+receipt[2]);
				System.out.println("Account Balance\t\t"+receipt[3]);
				System.out.println("----------------------------------");

				pid = receipt[1];
				amt_d = receipt[2];
				amt_d = receipt[2];
				amt_d = receipt[2];
				amt_bal = receipt[3];
				Writer w1 =new Writer("ClientLogs.txt");
				String msg1 = pid +"\t"+ amt_d+"\t"+amt_bal;
				w1.write(msg1, cb_longterm);


				//********************************************************************************************
				mybytearray  = new byte [100000];
				fos = new FileOutputStream("Received_Product2");
				bos = new BufferedOutputStream(fos);
				bytesRead = is.read(mybytearray,0,mybytearray.length);

				/*				// os.write(mybytearray, 0 , mybytearray.length);
				do {
					bytesRead =
							is.read(mybytearray, current, (mybytearray.length-current));
					if(bytesRead >= 0) current += bytesRead;
				} while(bytesRead > -1);

				 */
				bos.write(mybytearray, 0 , bytesRead);
				bos.flush();

				//********************************************************************************************

				break;

			case 3:
				System.out.println("Attempting to connect to ECOM3");
				SendToBroker(cb_session.encrypt("3"));

				catalogE = ce3_longterm.decrypt(inputFromBroker.readLine());
				products = catalogE.split("\\|");
				System.out.println("Please start your shopping!");
				do{
					System.out.println("Select the product(s):");
					for(int i = 0; i<3;i++)
						System.out.println(products[i]);

					j = sc.nextInt();
					if (j < 1 || j > 3) {
						System.out.println("please input a valid option");
						flag = true;
						continue;
					}
					j = j - 1;
					do{
						System.out.println("quantity (at least one unit)?");
						k = sc.nextInt();
						prods[j] += k;
					} while(prods[j] == 0);

					do{
						System.out.println("1)Continue shopping			2)View Cart and Checkout");
						j = sc.nextInt();
						if (j == 1)
							flag = true;
						else if (j == 2){
							flag = false;
							break;
						}
					}while(j!=1&&j!=2);

				}while(flag);
				System.out.println("*************** your cart *****************");
				for(int l =0; l<3; l++) {
					System.out.print(products[l]+"\t");
					System.out.println(prods[l]);
				}
				System.out.println("******************************************");

				//making a string of purchsed products to send it to client through Broker

				str = Integer.toString(prods[0])+"|"+Integer.toString(prods[1])+"|"+Integer.toString(prods[2]);

				outToBroker.println(ce3_longterm.encrypt(str));

				//receiving purchase ID and total purchase amount from ECOM1 through Broker
				str_pid_Toamnt = ce3_longterm.decrypt(inputFromBroker.readLine());
				pid_Toamnt = str_pid_Toamnt.split("\\|");
				pID = Integer.parseInt(pid_Toamnt[0]);
				total_amnt = Integer.parseInt(pid_Toamnt[1]);
				System.out.println("Order ID ; "+pID+"\t"+"Total payable Amount : "+total_amnt);
				System.out.println("******************************************");
				flag3 = false;

				do{
					System.out.println("place order?1 for Yes OR 2 for No");
					int i = sc.nextInt();

					if (i == 1){
						System.out.println("please wait...placing order");
						for (int zz = 0; zz < 5; zz++) {
							System.out.println(".");
							TimeUnit.MILLISECONDS.sleep(500);
						}
						flag3 = false;
					}
					else if (i == 2){
						System.out.println("Transaction cancelled!");
						return;
					}
					else
						flag3 = true;
				}while(flag3);


				//sending amount payable and order number to Broker
				outToBroker.println(cb_session.encrypt(str_pid_Toamnt));

				confirm = cb_session.decrypt(inputFromBroker.readLine());

				receipt = confirm.split("\\|");

				System.out.println(receipt[0]+"\n"+"----------------------------------");
				System.out.println("Order ID\t\t"+receipt[1]);
				System.out.println("Amount debited\t\t"+receipt[2]);
				System.out.println("Account Balance\t\t"+receipt[3]);
				System.out.println("----------------------------------");

				pid = receipt[1];
				amt_d = receipt[2];
				amt_bal = receipt[3];
				Writer w2 =new Writer("ClientLogs.txt");
				String msg2 = pid +"\t"+ amt_d+"\t"+amt_bal;
				w2.write(msg2, cb_longterm);

				//********************************************************************************************
				mybytearray  = new byte [100000];
				fos = new FileOutputStream("Received_Product3");
				bos = new BufferedOutputStream(fos);
				bytesRead = is.read(mybytearray,0,mybytearray.length);

				//  os.write(mybytearray, 0 , mybytearray.length);
				// os.write(mybytearray, 0 , mybytearray.length);
				/*				do {
					bytesRead =
							is.read(mybytearray, current, (mybytearray.length-current));
					if(bytesRead >= 0) current += bytesRead;
				} while(bytesRead > -1);

				 */
				bos.write(mybytearray, 0 , bytesRead);
				bos.flush();


				//********************************************************************************************

				break;

			case 99:	
				System.out.println("exiting client..");
				for (int zz = 0; zz < 2; zz++) {
					System.out.println(".");
					TimeUnit.MILLISECONDS.sleep(300);
				}  
				System.exit(0);
				break;

			default:
				System.out.println("please enter a correct option\n");
				flag5 = true;
				break;
			}
		} while(flag5);

		System.out.println("\n****************************************************");
		System.out.println("************ Thanks for shopping with us ***********");
		System.out.println("****************************************************");

		for (int zz = 0; zz < 2; zz++) {
			System.out.println(".");
			TimeUnit.MILLISECONDS.sleep(300);
		}   
		//main ends here


	}

	public static  int signUp(String userpass){
		String u1 = "akash";
		String p1=new Hash().getHash("linux");
		int s;

		String username = userpass.split(" ")[0];
		String passHash = userpass.split(" ")[1];
		if(username.equals(u1)&&passHash.equals(p1))
			s = 1;
		else
			s = 0;
		return s;
	}
	private static boolean isLogged(String uname,String passhash) throws IOException {

		String auth = uname+" "+ passhash;
		int msg = signUp(auth);

		return msg != 0;
	}

	public Client() throws Exception {
		try {

			random = new SecureRandom();
			connectToBroker();
			IORes();
			estkeys();

		} catch (IOException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	//client to broker connection functions
	public static void connectToBroker() throws IOException {
		s = new Socket(BROKER_IP, BROKER_PORT);

	}

	public static void IORes() throws IOException {
		is = s.getInputStream();
		os = s.getOutputStream();
		outToBroker = new PrintWriter(os, true);
		inputFromBroker = new BufferedReader(new InputStreamReader(is));
	}

	public static void SendToBroker(String msg) throws IOException {

		outToBroker.println(msg);

	}

	public static void SendToBroker(int msg) throws IOException {

		outToBroker.println(msg);

	}

	public static String recieveFromBroker() throws IOException {
		return inputFromBroker.readLine();
	}

	public static int recieveIntFromBroker() throws IOException {
		return inputFromBroker.read();
	}

	public static String nextSessionId() {

		return new BigInteger(130, random).toString(32);
	}

	public static  void estkeys() throws Exception {
		CBSessionID = nextSessionId();

		cb_longterm = new DESEncryption(Long.toString(new Random(cb_seed).nextLong()));
		ce1_longterm = new DESEncryption(Long.toString(new Random(ce1_seed).nextLong()));
		ce2_longterm = new DESEncryption(Long.toString(new Random(ce2_seed).nextLong()));
		ce3_longterm = new DESEncryption(Long.toString(new Random(ce3_seed).nextLong()));

		cb_session = new DESEncryption(CBSessionID);

	}

}
