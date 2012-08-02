import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Client {

	HashMap<Integer, Integer> accountservermap = new HashMap<Integer, Integer>();
	HashMap<Integer, String[]> servermap = new HashMap<Integer, String[]>();
	ArrayList<InformationMessage> infomsgs = new ArrayList<InformationMessage>();
	int number_of_servers;
	final int PORTFACTOR = 13;
	
	public static void main(String[] args) 
			throws ParserConfigurationException, SAXException, 
			IOException, ClassNotFoundException {
		Client client = new Client();
		client.initializeAccServer();
		client.initializeServerMap();
		client.number_of_servers = client.servermap.size();
		for(int i=0;i<client.number_of_servers; i++) {
			client.infomsgs.add(i, null);
		}
		client.core();
	}
	
	
	private void core() throws UnknownHostException, IOException, ClassNotFoundException {
		while(true) {
			System.out.println();
			System.out.println("----------------Menu Options:----------------");
			System.out.println("1. Check Balance");
			System.out.println("2. Deposit");
			System.out.println("3. Withdraw");
			System.out.println("4. Transfer");
			System.out.println("5. Snapshot");
			Scanner s = new Scanner(System.in);
			System.out.print("Your selection:  ");
			int option = s.nextInt();
			System.out.println();
			if(option == 1) {
				System.out.print("Enter Source Account#:  ");
				int account_no = s.nextInt();
				System.out.println();
				if(verifyAccountNumber(account_no)) {
					int server_no = accountservermap.get(account_no);
					String hostname = servermap.get(server_no)[0];
					int port = Integer.parseInt(servermap.get(server_no)[1]);
					Socket socket = new Socket(hostname, port);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					BalanceRequest req = new BalanceRequest(account_no);
					oos.writeObject(req);
					AccBalanceResponse response = (AccBalanceResponse) ois.readObject();
					int balance = response.balance;
					System.out.println("Account# "+account_no+"   Balance: "+balance);
				}
				else {
					System.out.println("Account does not exist.");
					continue;
				}
			}
			else if(option == 2) {
				System.out.print("Enter Source Account#:  ");
				int account_no = s.nextInt();
				System.out.println();
				if(verifyAccountNumber(account_no)) {
					int amount = 0;
					while(amount<=0) {
						System.out.print("Enter positive amount:  ");
						amount = s.nextInt();
						System.out.println();
					}
					int server_no = accountservermap.get(account_no);
					String hostname = servermap.get(server_no)[0];
					int port = Integer.parseInt(servermap.get(server_no)[1]);
					Socket socket = new Socket(hostname, port);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					DepositRequest req = new DepositRequest(account_no, amount);
					oos.writeObject(req);
					AccBalanceResponse response = (AccBalanceResponse) ois.readObject();
					int balance = response.balance;
					System.out.println("Account# "+account_no+"   New Balance: "+balance);
				}
				else {
					System.out.println("Account does not exist.");
					continue;
				}
			}
			else if(option == 3) {
				System.out.print("Enter Source Account#:  ");
				int account_no = s.nextInt();
				System.out.println();
				if(verifyAccountNumber(account_no)) {
					int amount = 0;
					while(amount<=0) {
						System.out.print("Enter positive amount:  ");
						amount = s.nextInt();
						System.out.println();
					}
					int server_no = accountservermap.get(account_no);
					String hostname = servermap.get(server_no)[0];
					int port = Integer.parseInt(servermap.get(server_no)[1]);
					Socket socket = new Socket(hostname, port);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					WithdrawRequest req = new WithdrawRequest(account_no, amount);
					oos.writeObject(req);
					AccBalanceResponse response = (AccBalanceResponse) ois.readObject();
					int balance = response.balance;
					if(response.completed) {
						System.out.println("Account# "+account_no+"   New Balance: "+balance);
					}
					else {
						System.out.println("Insufficient funds for withdrawal.");
						System.out.println("Account# "+account_no+"   Balance: "+balance);
					}
					
				}
				else {
					System.out.println("Account does not exist.");
					continue;
				}
			}
			else if(option == 4) {
				System.out.print("Enter Source Account#:  ");
				int src_account_no = s.nextInt();
				System.out.println();
				System.out.print("Enter Destination Account#:  ");
				int dest_account_no = s.nextInt();
				System.out.println();
				if(verifyAccountNumber(src_account_no) && verifyAccountNumber(dest_account_no)) {
					int amount = 0;
					while(amount<=0) {
						System.out.print("Enter positive amount:  ");
						amount = s.nextInt();
						System.out.println();
					}
					int server_no = accountservermap.get(src_account_no);
					String hostname = servermap.get(server_no)[0];
					int port = Integer.parseInt(servermap.get(server_no)[1]);
					Socket socket = new Socket(hostname, port);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					TransferRequest req = new TransferRequest(src_account_no,dest_account_no, amount);
					oos.writeObject(req);
					AccBalanceResponse response = (AccBalanceResponse) ois.readObject();
					int balance = response.balance;
					if(response.completed) {
						System.out.println("Source Account# "+src_account_no+"   New Balance: "+balance);
					}
					else {
						System.out.println("Insufficient funds for withdrawal.");
						System.out.println("Account# "+src_account_no+"   Balance: "+balance);
					}
				}
				else {
					System.out.println("Source or/and Destination Account does not exist.");
					continue;
				}
			}
			else if(option == 5) {
				String hostname = servermap.get(0)[0];
				int port = Integer.parseInt(servermap.get(0)[1]);
				Socket socket = new Socket(hostname, port);
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				String client_hostname = socket.getLocalAddress().getHostName();
				int client_port = socket.getLocalPort();
				SnapshotInit snapshotinit = new SnapshotInit(client_hostname, client_port+PORTFACTOR);
				ServerSocket infomsg_listen_socket = new ServerSocket(client_port+PORTFACTOR);
				oos.writeObject(snapshotinit);
				System.out.println("Collecting Snapshot data ...");
				for(int i=0; i<number_of_servers; i++) {
					Socket infomsg_socket = infomsg_listen_socket.accept();
					ObjectInputStream ois = new ObjectInputStream(infomsg_socket.getInputStream());
					InformationMessage infomsg = (InformationMessage) ois.readObject();
					infomsgs.set(i, infomsg);
				}
				displaySnapshot();
				testSnapshot();
				for(int i=0; i<number_of_servers; i++) {
					infomsgs.set(i, null);
				}
			}
			else {
				System.out.println("Invalid option. Try again.");
				continue;
			}
			
		}

	}
	
	
	private void initializeAccServer() 
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse("data/Servers.xml");
		Element root = dom.getDocumentElement();
		NodeList serverlist = root.getElementsByTagName("Server");
		for(int i=0; i < serverlist.getLength(); i++) {
			Element server = (Element)serverlist.item(i);
			int server_no = Integer.parseInt(server.getAttribute("server_no"));
			NodeList acclist = server.getElementsByTagName("Account");
			for(int j=0; j < acclist.getLength(); j++) {
					Element account = (Element)acclist.item(j);
					int accnumber = Integer.parseInt(account.getAttribute("accno"));
					accountservermap.put(accnumber, server_no);
			}
		}
	}
	
	
	private void initializeServerMap() 
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse("data/Servers.xml");
		Element root = dom.getDocumentElement();
		NodeList serverlist = root.getElementsByTagName("Server");
		for(int i=0; i < serverlist.getLength(); i++) {
			Element server = (Element)serverlist.item(i);
			int server_no = Integer.parseInt(server.getAttribute("server_no"));
			String[] serverinfo = new String[2];
			serverinfo[0] = server.getAttribute("hostname");
			serverinfo[1] = server.getAttribute("port");
			servermap.put(server_no, serverinfo);
		}
	}
	
	
	private boolean verifyAccountNumber(int account_no) {
		if( !(accountservermap.containsKey(account_no)) ) {
			return false;
		}
		return true;
	}
	
	
	protected void displaySnapshot() {
		System.out.println();
		System.out.println(".............SNAPSHOT OUTPUT............");
		for(InformationMessage infomsg : infomsgs) {
			int server_no = infomsg.server_no;
			System.out.println("Global state for Server#: "+server_no);
			System.out.println("Account-Balance information: "+infomsg.accountbalancemap);
			System.out.println("In-transit amounts from:");
			for(int i=0; i<number_of_servers; i++) {
				System.out.println("From Server#: "+i+" = "+infomsg.amount.get(i));
			}
			System.out.println();
		}
		System.out.println("..............End of Snapshot Output...........");
		System.out.println();
	}
	
	
	protected void testSnapshot() {
		for(int i=0; i<number_of_servers; i++) {
			for(int j = 1; j<(number_of_servers-i); j++) {
				if(infomsgs.get(j-1).server_no>infomsgs.get(j).server_no) {
					InformationMessage temp = infomsgs.get(j-1);
					infomsgs.set(j-1, infomsgs.get(j));
					infomsgs.set(j, temp);
				}
			}
		}
		System.out.println();
		System.out.println("Global state Vector clock values for servers:");
		for(int i=0; i<number_of_servers; i++) {
			System.out.println("Server#: "+infomsgs.get(i).server_no+" = "+ infomsgs.get(i).clock);
		}
		
		for(int i=0;i<number_of_servers;i++) {
			int max = infomsgs.get(i).clock.get(i);
			for(int j=0; j<number_of_servers; j++) {
				if(infomsgs.get(j).clock.get(i)>max) {
					System.out.println("Not a consistent snapshot.");
					return;
				}
			}
		}
		System.out.println("Consistent Snapshot.");
		System.out.println();
	}

}
