import java.util.*;

import javax.xml.crypto.Data;

import java.net.*;
import java.io.*;

//Server code in which the Go-back-N protocol is implemented
public class selective_repeat_server {
     static DatagramSocket serverSocket;
     static int portNo;
	int windowSize;
	int start=0;
     InetAddress ipAddress;
     static String fileName;
     boolean test = false;
     boolean [] acknowledge;
     byte [][]buffer;
     static File file;
     
     
     public static void main(String[] args) {
    	     
    	      selective_repeat_server ftpServer = new selective_repeat_server();
    	      ftpServer.portNo = Integer.parseInt(args[0]);
    	      ftpServer.fileName = args[1];
    	      ftpServer.file =  new File(fileName);
    	      System.out.println("The server is waiting for the conneciton");
    	      float prob = Float.parseFloat(args[2]);
    	      ftpServer.windowSize = Integer.parseInt(args[3]);
    	      ftpServer.buffer = new byte[ftpServer.windowSize][1024];
    	      ftpServer.acknowledge = new boolean[ftpServer.windowSize];
    	      System.out.println("The info is setup");
    	      try {
				serverSocket = new DatagramSocket(portNo);
				 System.out.println("The socket is created");
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
    	      try {
				Random ran = new Random();
 				float random;
 				
				continueLoop: while(!ftpServer.test) {
					System.out.println("Inside while1");
					byte []dataReceived = new  byte[2048];
					System.out.println("Inside while2");
					DatagramPacket packetReceived = new DatagramPacket(dataReceived, dataReceived.length);
					System.out.println("Inside while3");
					ftpServer.serverSocket.receive(packetReceived);
					System.out.println("Inside while4");
                     byte[] data = new byte[packetReceived.getLength() - 64];	
                     System.out.println("Inside while5");
                     System.arraycopy(dataReceived,64,data, 0, data.length);
                     System.out.println("packet length"+packetReceived.getLength());
                     byte[] received = packetReceived.getData();
                     System.out.println("Data length of received packet=="+received.length);
                     int sequenceNo = ftpServer.binaryToDecimal(received, 32);
                     String check = new String(Arrays.copyOfRange(received, 32, 48));
     				 String packetType = new String(Arrays.copyOfRange(received, 48, 64));
     				//byte[] datacheck = Arrays.copyOfRange(received, 64, received.length);
     				System.out.println("Bytes of actual data transferred"+data.length);
    				    random = ran.nextFloat();
    				    System.out.println("Randon number:" +random);
    				    System.out.println("Probability:" +prob);
    				    if(random <= prob) {
    				    	    System.out.println("Packet loss, sequence number = "+sequenceNo);
    				    	    continue continueLoop;
    				    }
    				    ftpServer.ipAddress = packetReceived.getAddress();
    				    ftpServer.portNo = packetReceived.getPort();
    				    String checksum = ftpServer.checksum(data);
    				    if(checksum.equals(check) && ftpServer.putPacket(data,sequenceNo)){
    							if(packetType.equals("0101010101010101"))
    		                    {
    								ftpServer.writeIntoDocument();
    		                        DatagramPacket ackpacket = ftpServer.createAckPacket(sequenceNo);
    							    ftpServer.serverSocket.send(ackpacket);
    		                    }
    							else
    							{
    								ftpServer.test = true;
    								System.out.println("End of the file");
    								ftpServer.writeIntoDocument();
    							}
				
    			}
		   }	

			System.out.println("The packet has been received");
    	       }catch (SocketException e) {
				e.printStackTrace();
			}
    	        catch(IOException e) {
    	        	    e.printStackTrace();
    	        }
    	       System.out.println("The contents of trhe file "+file.getName()+"have been written in to the file");
     }
         
     
 DatagramPacket createAckPacket(int sequence) {
	 DatagramPacket apacket = null;
	 String tempPacket = Integer.toBinaryString(sequence);
	 for(int i= tempPacket.length();i < 32 ; i++) {
		 tempPacket = "0" + tempPacket;
	 }
	 tempPacket+="00000000000000001010101010101010";
	 byte[] sendAck = tempPacket.getBytes();
	 apacket = new DatagramPacket(sendAck, sendAck.length,ipAddress,portNo);
	 return apacket;
 }
 
 void writeIntoDocument() {
	 for(;start < windowSize && acknowledge[start]; start = (start+1)%windowSize) {
		 FileOutputStream fstream = null;
		try {
			fstream = new FileOutputStream(file,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		 acknowledge[start] = false;
		 try {
			 fstream.write(buffer[start]);
		} catch (IOException e) {
		e.printStackTrace();
		}	 
	 }
 }
 
 boolean putPacket(byte[] data, int seq) {
	 int x = seq%windowSize;
	 if(!acknowledge[x]) {
		 buffer[x] = data;
		 acknowledge[x] = true;
	 }
	 return acknowledge[x];
	 
 }
 
 String checksum(byte [] info) {
	byte sum_1 = 0, sum_2 = 0;
	for(int i=0;i<info.length;i++) {
		sum_1 += info[i];
		if((i+1)<info.length) {
			sum_2 += info[i+1]; 
		}
	}
	String result1 = Byte.toString(sum_1);
	String result2 = Byte.toString(sum_2);
	for(int i=result1.length();i<8;i++)
		result1="0"+result1;
    for(int i=result2.length();i<8;i++)
    	    result2="0"+result2;
	return result1 + result2;
	 
 }
 
 int binaryToDecimal(byte[] byteStr, int n) {
	 String str = new String(Arrays.copyOfRange(byteStr, 0, 32));
	 double j = 0;
	 for(int i=0;i<str.length();i++) {
		 if(str.charAt(i) == '1') {
			 j = j + Math.pow(2,str.length() - 1 - i);
		 }
	 }
	 return (int)j;
 }
     
}

