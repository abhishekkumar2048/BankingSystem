import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class BankBranch {

	HashMap<Integer, Integer> accountbalancemap = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> accountservermap = new HashMap<Integer, Integer>();
	HashMap<Integer, String[]> servermap = new HashMap<Integer, String[]>();
	ArrayList<Integer> clock = new ArrayList<Integer>();
	ArrayList<Boolean> markerstatus = new ArrayList<Boolean>();
	ArrayList<Integer> intransitamount = new ArrayList<Integer>();
	HashMap<Integer, Integer> globalstatemap;
	ArrayList<Integer> globalstateclock;
	int server_no;
	int port_no;
	boolean snapshot_in_progress;
	int number_of_servers;
	ArrayList<Socket> server_comms = new ArrayList<Socket>();
	ArrayList<ObjectOutputStream> server_comms_oos = new ArrayList<ObjectOutputStream>();
	
	/*Following variables will be used only for server 0*/
	String snapshot_client_hostname;
	int snapshot_client_port;
	ArrayList<Boolean> infomsg_status = new ArrayList<Boolean>(); 
	
	
	public static void main(String[] args) 
			throws ParserConfigurationException, SAXException, 
			IOException, ClassNotFoundException, InterruptedException {
		if (args.length != 1) {
            System.out.println("Provide Server Number as argument ...");
            System.exit(0);
		}
		BankBranch branch = new BankBranch();
		branch.server_no = Integer.parseInt(args[0]);
		branch.snapshot_in_progress = false;
		branch.initializeAccounts(branch.server_no);
		branch.initializeAccServer();
		branch.initializeServerMap();
		branch.port_no = Integer.parseInt(branch.servermap.get(branch.server_no)[1]);
		branch.number_of_servers = branch.servermap.size();
		for(int i=0;i<branch.number_of_servers;i++) {
			branch.clock.add(i, 0);
			branch.markerstatus.add(i, false);
			branch.infomsg_status.add(i, false);
			branch.intransitamount.add(i, 0);
			branch.server_comms.add(i, null);
			branch.server_comms_oos.add(i, null);
		}
		branch.core();
	}
	
	
	private void core() throws IOException, ClassNotFoundException, InterruptedException {
		ServerSocket serversocket = new ServerSocket(port_no);
		while(true) {
			Socket socket = serversocket.accept();
			RequestHandler requesthandler = new RequestHandler(socket, this);
			requesthandler.start();
		}
	}
	
	
	private void initializeAccounts(int server_no) 
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse("data/Servers.xml");
		Element root = dom.getDocumentElement();
		NodeList serverlist = root.getElementsByTagName("Server");
		for(int i=0; i < serverlist.getLength(); i++) {
			Element server = (Element)serverlist.item(i);
			int server_number = Integer.parseInt(server.getAttribute("server_no"));
			if(server_number == server_no) {
				NodeList acclist = server.getElementsByTagName("Account");
				for(int j=0; j < acclist.getLength(); j++) {
					Element account = (Element)acclist.item(j);
					Integer accnumber = Integer.parseInt(account.getAttribute("accno"));
					Integer balance =  Integer.parseInt(account.getAttribute("bal"));
					accountbalancemap.put(accnumber, balance);
				}
				break;
			}
		}
		System.out.println(accountbalancemap);
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

}
