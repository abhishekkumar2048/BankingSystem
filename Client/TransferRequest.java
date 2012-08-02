import java.io.Serializable;


public class TransferRequest implements Serializable {
	int source_account_no;
	int dest_account_no;
	int amount;
	
	public TransferRequest(int source_account_no, int dest_account_no, int amount) {
		this.source_account_no = source_account_no;
		this.dest_account_no = dest_account_no;
		this.amount= amount;
	}

}
