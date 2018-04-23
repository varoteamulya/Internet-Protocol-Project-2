import java.net.*;
import java.nio.channels.AcceptPendingException;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.io.*;

public class selective_repeat_client {
  String host = "127.0.0.1";
  int port, n,mss,windowSize;
  static int startTime = 0;
  static int endTime = 0;
  static File file;
  static byte [][] filesystem;
  static DatagramSocket clientSocket;
  long roundTripTime = 0;
  int count = 0;
  int buffersize = 0 ;
  static InetAddress ip;
  static packet pack[];
  static boolean [] acknowledge;
  long delay = 0;
  
  
  public selective_repeat_client(String host,int port,String filename,int winSize,int mss) {
	  this.host = host;
	  this.port = port;
	  this.file = new File(filename);
	  this.windowSize = winSize;
	  this.mss = mss;
	  try
      {
          clientSocket=new DatagramSocket();
          file=new File(filename);
          ip=InetAddress.getByName(host);
      }catch(Exception e) {
    	  
      }
  }
  
  public static void main(String[] args) throws IOException {
	  //Storing the command line inputs in the variables
	  String serverhostname = args[0];
	  int portNo = Integer.parseInt(args[1]);
	  String filename = args[2];
	  int windowSize = Integer.parseInt(args[3]);
	  int mss = Integer.parseInt(args[4]);
	  selective_repeat_client ftpClient = new selective_repeat_client(serverhostname,portNo,filename,windowSize,mss);
	  
	  System.out.println("The file is getting transferred");
	  //formatting the file in the required manner
	  int num = (int) (file.length()/mss);
	  int i;
	  filesystem = new byte[num + 1][mss];
	  pack = new packet[num + 1];
	  acknowledge = new boolean[num + 1];
	  byte [] bytedata = new byte[(int)file.length()];
	  FileInputStream fstream;
	try {
		fstream = new FileInputStream(file);
		BufferedInputStream bstream = new BufferedInputStream(fstream);
		bstream.read(bytedata,0,bytedata.length);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	for(i = 0; i < bytedata.length ; i++) {
		  filesystem[i/mss][i%mss] = bytedata[i]; 
	}
	num = i/mss;
	while(i%mss != 0) {
		  i++;
		  filesystem[num][i%mss] = 0;
	}
	i=0;
	
	System.out.println("The packet is being sent");
	ftpClient.performOperation();
	System.out.println("Time taken" + ftpClient.roundTripTime);
	System.out.println("The file is transferred");
	  
  }
  
   void performOperation() throws IOException {
	  //considering the start time of the system.
	  long startTime = System.currentTimeMillis();
	  System.out.println("The start time is "+startTime);
	  System.out.println("count:" + count);
	  System.out.println("mss:" + mss);
	  System.out.println("file:" + (int)file.length());
	  System.out.println("Buffer:" + buffersize);
	  System.out.println("window:" + windowSize);
	  send();
	  System.out.println("hii1");
	  
	  continueLoop:while((selective_repeat_client.startTime*mss) < (int)file.length()) {
		      System.out.println("hii2");
		      send();
		      System.out.println("hii3");
			  byte[] receivedData = new byte[1024];
			  DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
			  try {
				      clientSocket.setSoTimeout(1000);
					  selective_repeat_client.clientSocket.receive(receivePacket);
					  System.out.println("hii4");
					  String data = new String(receivePacket.getData());
					  System.out.println("hii5");
					  if(data.substring(48, 64).equals("1010101010101010")) {
							  System.out.println("The packet received is acknowledgement packet");
							  String acknowledgementNo = data.substring(0,32);
							  int ackNo = binaryToDecimal(acknowledgementNo);
							  acknowledge[ackNo] = true;
							  slide();
		                       pack[ackNo].stop();
				      }
			  
		       }
		      catch(IOException sto)
		      {
		          continue continueLoop;
		      }
	  }

	  long endTime = System.currentTimeMillis();
	  System.out.println("The end time is "+endTime);
	  roundTripTime = endTime - startTime;
	  lastPacket();
  }
  
  int binaryToDecimal(String acknowledgementNo) {
		 double j = 0;
		 for(int i=0;i<acknowledgementNo.length();i++) {
			 if(acknowledgementNo.charAt(i) == '1') {
				 j = j + Math.pow(2,acknowledgementNo.length() - 1 - i);
			 }
		 }
		 return (int)j;
  }
  
  void slide() {
	  for(int i=startTime;startTime < filesystem.length && acknowledge[i];i++)
      {
              startTime++;
      }
  }
  
  String checksum(byte [] b) {
		byte sum_1 = 0, sum_2 = 0;
		for(int i=0;i<b.length;i++) {
			sum_1 += b[i];
			if((i+1)<b.length) {
				sum_2 += b[i+1]; 
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
  
  void lastPacket() {
	  String lastData = Integer.toBinaryString(selective_repeat_client.startTime + 1);
	  for(int i = lastData.length(); i<32 ;i++) {
		  lastData = "0" + lastData;
	  }
	  byte endBytes[] = new byte[mss];
	  String check = checksum(endBytes);
	  String data = new String(endBytes);
	  String lastFullPacket = lastData + check + "0000000000000000" + data;
	  byte[] lastBytes = lastFullPacket.getBytes();
	  DatagramPacket lastPacket = new DatagramPacket(lastBytes,lastBytes.length,selective_repeat_client.ip,7735);
	  try {
		clientSocket.send(lastPacket);
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
  void send(){
  while(((selective_repeat_client.endTime - selective_repeat_client.startTime) < windowSize && filesystem.length > selective_repeat_client.endTime))
  {
      pack[endTime]=new packet(checksum(filesystem[endTime]),endTime,filesystem[endTime]);
      acknowledge[endTime]=false;
      pack[selective_repeat_client.endTime].start();
      selective_repeat_client.endTime++;
  }
  }
  
  
}





class packet extends Thread{
	String checksum;
	int sequenceNo;
	byte[] data;
	packet(String x, int no, byte[] d){
		this.checksum = x;
		this.sequenceNo = no;
		this.data = d;
	}
	
	public void run() {
		while(this.isAlive()) {
			DatagramPacket packet;
			String seq=Integer.toBinaryString(sequenceNo);
			  for(int i=seq.length();i<32;i++) {
				  seq = "0" + seq;
			  }
			  String packetType = "0101010101010101";
			  String packetHeader = sequenceNo + checksum + packetType;
			  byte[] sendData = packetHeader.getBytes();
			  byte[] packetBytes = new byte[data.length + sendData.length];
			  for(int i=0;i<packetBytes.length;i++) {
				  if(i < sendData.length) {
					  packetBytes[i] = sendData[i];
				  }
				  else {
					  packetBytes[i]=data[i-sendData.length];
				  }
			  }
			  int port = 7735;
			  packet = new DatagramPacket(packetBytes, packetBytes.length, selective_repeat_client.ip, port);
			  try
	            {
	                selective_repeat_client.clientSocket.send(packet);
	                System.out.println("Packet sent with seq number"+sequenceNo);
	                sleep(100);
	                System.out.println("Timed out, Sequence Number="+sequenceNo);
	            }
	            catch(IOException ioe)
	            {
	                System.out.println(ioe);
	            }
	            catch(InterruptedException ie)
	            {
	                System.out.println(ie);
	            }
		}
	}
}


