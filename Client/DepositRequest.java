import java.io.Serializable;


public class DepositRequest implements Serializable {
	int account_no;
	int amount;
	
	public DepositRequest(int account_no, int amount) {
		this.account_no = account_no;
		this.amount = amount;
	}
}
