
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ECOM3 {

	private static final int ECOM3_PORT = 5003;
	static ServerSocket listener;
	static Socket socket;
	static PrintWriter outBroker;
	static BufferedReader inBroker;
	static OutputStream os;

	static DESEncryption e3c_longterm;
	static DESEncryption e3b_longterm;
	static long e3b_seed = 12125637;
	static long e3c_seed = 63521235;
	static DESEncryption e3b_session;
	static SecureRandom random;

	static String E3BSessionID;
	static String catalog = "1)Book1			$20|2)Book2			$25|3)Book3			$15";

	public static void main(String[] args) throws IOException, Exception {
		System.out.println("\n*********************************************************");
		System.out.println("****************** Ebooks server Started ****************");
		System.out.println("*********************************************************");
		int[] prod_count = new int[3];
		ECOM3 ecoM3 = new ECOM3();
		E3BSessionID = "";
		boolean done = false;
		while (!done){
			String fromBroker = inBroker.readLine();
			// System.out.println("Encrypted session key from Broker : "+ fromBroker);

			E3BSessionID = e3b_longterm.decrypt(fromBroker);
			e3b_session = new DESEncryption(E3BSessionID);
			//   System.out.println("***********EBSessionID after decryption is: " + E3BSessionID);
			//  TimeUnit.SECONDS.sleep(5);
			outBroker.println(e3c_longterm.encrypt(catalog));

			//read purchase information from client stream

			String purchase_info = e3c_longterm.decrypt(inBroker.readLine());

			String[] prods = purchase_info.split("\\|");
			for (int i =0; i< 3; i++){
				prod_count[i] = Integer.parseInt(prods[i]);
			}

			int total_amount = 20*prod_count[0]+25*prod_count[1]+15*prod_count[2];
			//This will generate a random purchase ID between 1 and 100.
			Random ran = new Random();
			int purchase_id = ran.nextInt(1) + 100;
			//making a string containing total amount and purchase ID
			String str_pid_Toamnt = Integer.toString(purchase_id)+"|"+Integer.toString(total_amount);

			//encrypting using ec_longterm and writing on broker stream

			outBroker.println(e3c_longterm.encrypt(str_pid_Toamnt));

			//outBroker.println(eb_session.encrypt(ec_longterm.encrypt(catalog)));
			// String temp1 = outBroker.println(ec_longterm.encrypt(catalog));
			//System.out.println("catalog encrypted by ec_longterm is: " + temp1);
			//String temp2 = eb_session.encrypt(temp1);
			//System.out.println("catalog encrypted by eb_session is: " + temp2);
			// System.out.println("Sending Temp2 to broker");
			// outBroker.println(temp2);

			String confirm = e3b_session.decrypt(inBroker.readLine());
			String[] receipt = confirm.split("\\|");

			System.out.println(receipt[0]+"\n"+"----------------------------------");
			System.out.println("Order ID\t\t"+receipt[1]);
			System.out.println("Amount Credited\t\t"+receipt[2]);
			System.out.println("Account Balance\t\t"+receipt[3]);
			System.out.println("----------------------------------");
			Writer w =new Writer("ECOM3Logs");
			String msg = receipt[1] +"\t"+ receipt[2]+"\t"+receipt[3];
			w.write(msg, e3b_longterm);

			done = true;
			//*****************************************              
			if (done){

				//sending product file

				File myFile = new File ("Product3.pdf");
				byte [] mybytearray  = new byte [(int)myFile.length()];
				System.out.println("size of mybytearray is:" + mybytearray.length);
				FileInputStream fis = new FileInputStream(myFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				int c = bis.read(mybytearray,0,mybytearray.length);
				System.out.println("bis.read returned : "+c);
				System.out.println("Sending " + "product" + "(" + mybytearray.length + " bytes)");
				os.write(mybytearray,0,mybytearray.length);
				os.flush();
				System.out.println("Done.");
			}
			//*********************************************************
			socket.close();


		}


		//            String encr_manual_request = inBroker.readLine();
		//            String request  =eb_session.decrypt(encr_manual_request);
		//            String manual = "R121 Godfather 2,R135 Dil Chahta Hai 1.5,R432 Scarface 3.5";
		//            //envrypt with longterm of e,c
		//            String encryptedManual = ec_longterm.encrypt(manual);
		//            //encrypt with short term of e,b
		//            outBroker.println(eb_session.encrypt(encryptedManual));




		//    }
	}

	public ECOM3() throws Exception {
		try {
			listener = new ServerSocket(ECOM3_PORT);
			socket = listener.accept();
			System.out.println("Broker conected to ECOM1\n");
			os = socket.getOutputStream();
			outBroker = new PrintWriter(os, true);
			inBroker = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			random = new SecureRandom();
			estKeys();
		} catch (IOException ex) {

		}
	}

	public static String nextSessionId() {

		return new BigInteger(130, random).toString(32);
	}

	public void estKeys() throws Exception {
		//generate long term session key for broker 
		e3c_longterm = new DESEncryption(Long.toString(new Random(e3c_seed).nextLong()));
		e3b_longterm = new DESEncryption(Long.toString(new Random(e3b_seed).nextLong()));

	}
}
