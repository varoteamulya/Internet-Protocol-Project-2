## Steps to execute the code:

The folder has the code for both client and the server. 

1. The "simple_ftp_server.java" should be run on machine 1. The "simple_ftp_client.java" should be run on machine 2.

2. Download the required files on the intended machines. Here, for example, let machine 1 be a server and machine 2 be a client.

3. Add 1 MB file text file to the src folder of the client.

4. If the terminal is used for executing the code, then navigate to the src folder. 
   Machine 1 is server. Compile the java code by using th command "javac simple_ftp_server.java". Then execute the server code by using the command "java simple_ftp_server port# file-name p"
    where port# is the port number to which the server is listening (for this project, this port number is always 7735),
    file-name is the name of the file where the data will be written, and p is the packet loss probability.
    
    Machine 2 is client. Compile the java code by using th command "javac simple_ftp_client.java". Then execute the server code by using the command "java simple_ftp_server server-host-name server-port# file-name N MSS"
    where server-host-name is the host name where the server runs, server-port# is the port number of the server
    (i.e., 7735), file-name is the name of the file to be transferred, N is the window size, and MSS is the maximum
    segment size.
    
5. If any IDEs like eclipse is used, then run the file "simple_ftp_server.java" to start the server. And for client, run the file "simple_ftp_client.java". Before running the codes, configure the arguments by following the intended steps.

6. After the successful execution of the program and the file is being transferred, then the following ouput is observed in two machines.
Machine 1(server): Starting the server and waiting for the connection 
Machine 2(client): The average delay of the transfer of the file is *********

• Simple-FTP server: whenever a packet with sequence number X is discarded by the probabilistic loss
service, the server prints the following line:
Packet loss, sequence number = X
• Simple-FTP client: whenever a timeout occurs for a packet with sequence number Y , the client prints the following line:
Timeout, sequence number = Y
