import java.io.Serializable;


public class MarkerMessage implements Serializable {
	int server_no;
	
	public MarkerMessage(int server_no) {
		this.server_no = server_no;
	}
}
