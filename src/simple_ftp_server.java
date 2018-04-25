
import java.net.*;
import java.io.*;
import java.util.*;

public class simple_ftp_server {
    DatagramSocket serverSocket;
    static int portno;
    static InetAddress ipAddress;
    String filename;
    FileOutputStream fstream ;
    int acknowledge=0;
    boolean test=true;
    static BufferedOutputStream bstream;
    
    public static void main(String[] args) throws IOException
    { 
        simple_ftp_server ftpServer=new simple_ftp_server();
        System.out.println("Starting the server and waiting for the connection");
        int length=0,current=0;
        float random;
      int portno=Integer.parseInt(args[0]);
      String fileName = args[1];
      File file=new File(fileName);
      float p=Float.parseFloat(args[2]);
      try {
      try {
		ftpServer.serverSocket = new DatagramSocket(portno);
	  } catch (SocketException e) {
		  e.printStackTrace();
	  } 
      byte[] dataReceived;
      byte[] sendData  = new byte[10000];
      Random ran=new Random();
      FileOutputStream fstream;
	  fstream = new FileOutputStream(file);
	  bstream=new BufferedOutputStream(fstream);
	  
      continueLoop:while(ftpServer.test) 
        { 
          dataReceived = new byte[2048]; 
          DatagramPacket packetReceived =new DatagramPacket(dataReceived, dataReceived.length);
          try {
          ftpServer.serverSocket.receive(packetReceived);
          }catch(IOException e) {
        	  System.out.println("The connection is lost");
          }
          byte [] db=new byte[packetReceived.getLength()-64];
          System.arraycopy(dataReceived,64, db,0,db.length);
          byte [] received=packetReceived.getData();
          binaryToDecimal btd = new binaryToDecimal();
          int sequenceno=btd.binaryToDecimal(received);
          String check=new String(Arrays.copyOfRange(received, 32, 48));
          String packetType=new String(Arrays.copyOfRange(received,48,64));
          byte [] data=Arrays.copyOfRange(received, 64,received.length);
          
          random=ran.nextFloat();
          if(random<=p)
          {
              System.out.println("Packet loss, Sequence number="+sequenceno);
              continue continueLoop;
          }
          ftpServer.ipAddress=packetReceived.getAddress();
          ftpServer.portno=packetReceived.getPort();
          checksum chk = new checksum();
          String errorData=chk.checksum(data);
          if(errorData.equals(check))
          {
                if(ftpServer.acknowledge==sequenceno)
                {
                	ftpServer.acknowledge++;
                    if(packetType.equals("0101010101010101"))
                    {
                    	    try {
                        fstream.write(db);
                    	    }catch (Exception e) {
						    System.out.println("The connection might be gone");		
						}
                        ftpServer.serverSocket.send(ftpServer.createPacket(ftpServer.acknowledge));
                    }
	                 else {
	                    	ftpServer.test=false;
	                 }
                }
           }
        }
      }catch(Exception e) {
    	    System.out.println("The connection might be lost");
      }
      bstream.flush();
      bstream.close();
      System.out.println(fileName+" contents have been written");
    }
     
    
    DatagramPacket createPacket(int seq)
    {
        DatagramPacket packetCreation=null;
        packetCreation pck = new packetCreation();
        String packet = pck.createPacket(seq);
        byte []send=packet.getBytes();
        packetCreation=new DatagramPacket(send,send.length,ipAddress,portno);
        return packetCreation;
    }


}
