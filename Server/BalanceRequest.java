import java.io.Serializable;


public class BalanceRequest implements Serializable {
	int account_no;
	
	public BalanceRequest(int account_no) {
		this.account_no = account_no;
	}

}
