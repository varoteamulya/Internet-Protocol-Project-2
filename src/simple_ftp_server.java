
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
    
    public static void main(String[] args) throws IOException
    { 
        simple_ftp_server ftpServer=new simple_ftp_server();
        int length=0,current=0;
        float random;
      int portno=Integer.parseInt(args[0]);
      String fileName = args[1];
      File file=new File(fileName);
      float p=Float.parseFloat(args[2]);
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
	  BufferedOutputStream bstream=new BufferedOutputStream(fstream);
      continueLoop:while(ftpServer.test) 
        { 
          dataReceived = new byte[2048]; 
          DatagramPacket packetReceived =new DatagramPacket(dataReceived, dataReceived.length);
          ftpServer.serverSocket.receive(packetReceived);
          byte [] db=new byte[packetReceived.getLength()-64];
          System.arraycopy(dataReceived,64, db,0,db.length);
          byte [] received=packetReceived.getData();
          int sequenceno=ftpServer.binaryToDecimal(received,32);
          String chck=new String(Arrays.copyOfRange(received, 32, 48));
          String type=new String(Arrays.copyOfRange(received,48,64));
          byte [] data=Arrays.copyOfRange(received, 64,received.length);
          random=ran.nextFloat();
          if(random<=p)
          {
              System.out.println("Packet loss, Sequence number="+sequenceno);
              continue continueLoop;
          }
          ftpServer.ipAddress=packetReceived.getAddress();
          ftpServer.portno=packetReceived.getPort();
          String errorData=ftpServer.checksum(data);
          if(errorData.equals(chck))
          {
                if(ftpServer.acknowledge==sequenceno)
                {
                	ftpServer.acknowledge++;
                    if(type.equals("0101010101010101"))
                    {
                        fstream.write(db);
                        DatagramPacket acknowledgementPackte=ftpServer.createPacket(ftpServer.acknowledge);
                        ftpServer.serverSocket.send(acknowledgementPackte);
                    }
	                 else {
	                    	ftpServer.test=false;
	                 }
                }
           }
        }
      bstream.flush();
      bstream.close();
      System.out.println(fileName+" contents have been written");
    }
    
    
    String checksum(byte [] byteData)
    {
       byte sum_1=0,sum_2=0;
       for(int i=0;i<byteData.length;i=i+2)
       {
           sum_1+=byteData[i];
           if((i+1)<byteData.length)
            sum_2+=byteData[i+1];
       }
       String result1=Byte.toString(sum_1);
       String result2=Byte.toString(sum_2);
       for(int i=result1.length();i<8;i++)
    	     result1="0"+result1;
       for(int i=result2.length();i<8;i++)
    	     result2="0"+result2;
       return result1+result2;
    }
    
    
    
    DatagramPacket createPacket(int seq)
    {
        DatagramPacket packetCreation=null;
        String packet=Integer.toBinaryString(seq);
        for(int i=packet.length();i<32;i++) {
        	packet="0"+packet;
        }
        packet+="00000000000000001010101010101010";
        byte []send=packet.getBytes();
        packetCreation=new DatagramPacket(send,send.length,ipAddress,portno);
        return packetCreation;
    }

   
    int binaryToDecimal(byte [] st,int n){
	     String str=new String(Arrays.copyOfRange(st, 0, 32)); 
	     double j=0;
	     for(int i=0;i<str.length();i++){
	        if(str.charAt(i)== '1'){
	         j=j+ Math.pow(2,str.length()-1-i);
	     }

	    }
	    return (int) j;
    }

}
