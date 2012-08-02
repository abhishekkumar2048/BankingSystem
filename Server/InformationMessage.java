import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class InformationMessage implements Serializable {
	int server_no;
	HashMap<Integer, Integer> accountbalancemap;
	ArrayList<Integer> clock;
	ArrayList<Integer> amount;
	
	@SuppressWarnings("unchecked")
	public InformationMessage(int server_no, HashMap<Integer, Integer> accountbalancemap, 
			ArrayList<Integer> clock, ArrayList<Integer> amount) {
		this.server_no = server_no;
		this.accountbalancemap = new HashMap<Integer, Integer>(accountbalancemap);
		this.clock = new ArrayList<Integer>(clock);
		this.amount = new ArrayList<Integer>(amount);
	}
	
}
