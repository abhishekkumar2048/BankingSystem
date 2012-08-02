import java.io.Serializable;


public class SnapshotInit implements Serializable {
	
	String client_hostname;
	int client_listening_port;
	
	public SnapshotInit(String client_hostname, int client_listening_port) {
		this.client_hostname = client_hostname;
		this.client_listening_port = client_listening_port; 
	}
}
