import java.io.Serializable;


public class WithdrawRequest implements Serializable {
	int account_no;
	int amount;
	
	public WithdrawRequest(int account_no, int amount) {
		this.account_no = account_no;
		this.amount = amount;
	}
}
