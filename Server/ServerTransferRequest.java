import java.io.Serializable;
import java.util.ArrayList;


public class ServerTransferRequest implements Serializable {
	int server_no;
	int account_no;
	int amount;
	ArrayList<Integer> piggyback_clock;
	
	public ServerTransferRequest(int server_no, int account_no, int amount, ArrayList<Integer> piggyback_clock) {
		this.server_no = server_no;
		this.account_no = account_no;
		this.amount = amount;
		this.piggyback_clock = new ArrayList<Integer>(piggyback_clock);
	}
}
