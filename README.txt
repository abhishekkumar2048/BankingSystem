
Distributed banking system capable of taking snapshots using Chandy-Lamport`s algorithm.

- Abhishek Kumar (axk091420)
- Kinnari Dholakia (kxd111730)


Assumptions:
- All transactions amounts and balances are integer values.
- The topology is strongly connected.
- Server with server# = 0 is responsible for initiating snapshot algorithm when requested by the client.
- Account numbers are 5 digit integers.

XML config files:
- Server elements have three attributes: server_no, hostname, port.
- Account element inside each server element has 2 attributes: acc_no, balance.

Testing Mechanism:
- The vector clock values of all servers at their global state is compared to determine if the snapshot is consistent.

How to compile ?
- Server: javac BankBranch.java
- Client: javac Client.java

How to run ?
- Server: java BankBranch <server#> 
	<server#> should start from 0,1,2,3,4,...
	and it should be consistent with the xml config file. (data/servers.xml) 
- Client: java Client




  