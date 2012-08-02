import java.io.Serializable;


public class AccBalanceResponse implements Serializable {
	int account_no;
	int balance;
	boolean completed;
	
	public AccBalanceResponse(int account_no, int balance, boolean completed) {
		this.account_no = account_no;
		this.balance = balance;
		this.completed = completed;
	}
}
