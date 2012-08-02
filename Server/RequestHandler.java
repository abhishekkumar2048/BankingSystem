import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RequestHandler extends Thread {
	Socket socket;
	ObjectOutputStream socketos;
	ObjectInputStream socketis;
	BankBranch branch;
	
	public RequestHandler(Socket socket, BankBranch branch) throws IOException {
		this.socket = socket;
		socketos = new ObjectOutputStream(socket.getOutputStream());
		socketis = new ObjectInputStream(socket.getInputStream());
		this.branch = branch;
	}
	
	
	public void run() {
		while(true) {
			try {
				Object temp = socketis.readObject();
				if(temp instanceof BalanceRequest) {
					BalanceRequest req = (BalanceRequest) temp;
					int account_no = req.account_no;
					int balance = branch.accountbalancemap.get(account_no);
					AccBalanceResponse response = new AccBalanceResponse(account_no, balance, true);
					socketos.writeObject(response);
				}
				
				if(temp instanceof DepositRequest) {
					DepositRequest req = (DepositRequest) temp;
					int account_no = req.account_no;
					int amount = req.amount;
					int balance = branch.accountbalancemap.get(account_no);
					balance = balance+amount;
					branch.accountbalancemap.put(account_no, balance);
					AccBalanceResponse response = new AccBalanceResponse(account_no, balance, true);
					socketos.writeObject(response);
				}
				
				if(temp instanceof WithdrawRequest) {
					WithdrawRequest req = (WithdrawRequest) temp;
					int account_no = req.account_no;
					int amount = req.amount;
					int balance = branch.accountbalancemap.get(account_no);
					int tempbalance = balance - amount;
					if(tempbalance>0) {
						balance = tempbalance;
						branch.accountbalancemap.put(account_no, balance);
						AccBalanceResponse response = new AccBalanceResponse(account_no, balance, true);
						socketos.writeObject(response);
					}
					else {
						AccBalanceResponse response = new AccBalanceResponse(account_no, balance, false);
						socketos.writeObject(response);
					}
				}
				
				if(temp instanceof TransferRequest) {
					TransferRequest req = (TransferRequest) temp;
					int source_account_no = req.source_account_no;
					int dest_account_no = req.dest_account_no;
					int amount = req.amount;
					int balance = branch.accountbalancemap.get(source_account_no);
					int tempbalance = balance - amount;
					if(tempbalance<0) {
						AccBalanceResponse response = new AccBalanceResponse(source_account_no, balance, false);
						socketos.writeObject(response);
					}
					else {
						balance = tempbalance;
						branch.accountbalancemap.put(source_account_no, balance);
						int dest_server_no = branch.accountservermap.get(dest_account_no);
						AccBalanceResponse response = new AccBalanceResponse(source_account_no, balance, true);
						socketos.writeObject(response);
						int prevclockvalue = branch.clock.get(branch.server_no);
						branch.clock.set(branch.server_no, prevclockvalue+1);
						ServerTransferRequest serverrequest = new ServerTransferRequest(branch.server_no,dest_account_no, amount, branch.clock);
						prepareSocketStream(dest_server_no);
						branch.server_comms_oos.get(dest_server_no).writeObject(serverrequest);
					}
					
				}
				
				if(temp instanceof ServerTransferRequest) {
					ServerTransferRequest req = (ServerTransferRequest) temp;
					Thread.sleep(3000);
					int server_no = req.server_no;
					int account_no = req.account_no;
					int amount = req.amount;
					int balance = branch.accountbalancemap.get(account_no);
					branch.accountbalancemap.put(account_no, balance+amount);
					for(int i=0; i<branch.number_of_servers; i++) {
						int initialval = branch.clock.get(i);
						int piggybackval = req.piggyback_clock.get(i);
						if(initialval < piggybackval) {
							branch.clock.set(i, piggybackval);
						}
						if(i==branch.server_no) {
							int prev_value = branch.clock.get(i);
							branch.clock.set(i, prev_value+1);
						}
					}
					if(branch.snapshot_in_progress && !(branch.markerstatus.get(server_no))) {
						int prevtransit_amount = branch.intransitamount.get(server_no);
						branch.intransitamount.set(server_no, prevtransit_amount+amount);
					}
					
				}
				
				if(temp instanceof MarkerMessage) {
					MarkerMessage markermsg = (MarkerMessage) temp;
					branch.markerstatus.set(markermsg.server_no, true);
					System.out.println("Marker msg received from: "+markermsg.server_no);
					boolean all_markers_received = true;
					for(Boolean markerstatus : branch.markerstatus) {
						if(!markerstatus) {
							all_markers_received = false;
							break;
						}
					}
					if(all_markers_received) {
						InformationMessage infomsg = new InformationMessage(branch.server_no, 
								branch.globalstatemap, branch.globalstateclock, branch.intransitamount);
						prepareSocketStream(0);
						branch.server_comms_oos.get(0).writeObject(infomsg);
						for(int i=0;i<branch.number_of_servers;i++) {
							branch.markerstatus.set(i, false);
							branch.intransitamount.set(i, 0);
						}
						branch.globalstatemap.clear();
						branch.globalstateclock.clear();
						branch.snapshot_in_progress = false;
					}
					else {
						if(!(branch.snapshot_in_progress)) {
							branch.globalstatemap = new HashMap<Integer, Integer>(branch.accountbalancemap);
							branch.globalstateclock = new ArrayList<Integer>(branch.clock);
							branch.snapshot_in_progress = true;
							branch.markerstatus.set(branch.server_no, true);
							for(Map.Entry<Integer, String[]> e : branch.servermap.entrySet()) {
								if(e.getKey() != branch.server_no) {
									prepareSocketStream(e.getKey());
									MarkerMessage fwdmarker = new MarkerMessage(branch.server_no);
									branch.server_comms_oos.get(e.getKey()).writeObject(fwdmarker);
								}
							}
						}
					}
					
				}
				
				if(temp instanceof SnapshotInit) {
					SnapshotInit snapshotinit = (SnapshotInit) temp;
					while(branch.snapshot_in_progress) {
					}
					for(int i=0; i<branch.number_of_servers; i++) {
						branch.infomsg_status.set(i, false);
					}
					System.out.println("Initiating Snapshot ...");
					branch.globalstatemap = new HashMap<Integer, Integer>(branch.accountbalancemap);
					branch.globalstateclock = new ArrayList<Integer>(branch.clock);
					branch.snapshot_in_progress = true;
					branch.markerstatus.set(branch.server_no, true);
					branch.snapshot_client_hostname = snapshotinit.client_hostname;
					branch.snapshot_client_port = snapshotinit.client_listening_port;
					for(Map.Entry<Integer, String[]> e : branch.servermap.entrySet()) {
						if(e.getKey() != branch.server_no) {
							prepareSocketStream(e.getKey());
							MarkerMessage markermsg = new MarkerMessage(branch.server_no);
							branch.server_comms_oos.get(e.getKey()).writeObject(markermsg);
						}
					}
				}
				
				if(temp instanceof InformationMessage) {
					InformationMessage infomsg_client = (InformationMessage) temp;
					System.out.println("Information message received from : "+infomsg_client.server_no);
					if(!branch.infomsg_status.get(infomsg_client.server_no)) {
						Socket infosocket_client = new Socket(branch.snapshot_client_hostname, branch.snapshot_client_port);
						ObjectOutputStream infomsg_clientos = new ObjectOutputStream(infosocket_client.getOutputStream());
						infomsg_clientos.writeObject(infomsg_client);
						branch.infomsg_status.set(infomsg_client.server_no, true);
					}
				}
			} catch (IOException e1) {
				//e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				//e1.printStackTrace();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	
	private void prepareSocketStream(int server_no) throws UnknownHostException, IOException {
		if(branch.server_comms.get(server_no)==null) {
			String hostname = branch.servermap.get(server_no)[0];
			int port = Integer.parseInt(branch.servermap.get(server_no)[1]);
			branch.server_comms.set(server_no, new Socket(hostname, port));
			branch.server_comms_oos.set(server_no, new ObjectOutputStream(
					branch.server_comms.get(server_no).getOutputStream()));
		}
	}

}
