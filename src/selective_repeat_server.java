import java.io.ByteArrayOutputStream;
import java.io.*;
import java.net.*;
import java.util.*;

public class selective_repeat_server {
	private static packet firstPacket = null;
	public static void main(String[] args) throws IOException {
		int port = Integer.parseInt(args[0]);
		String filename = args[1];
		double pos = Double.parseDouble(args[2]);
		System.out.println("The selective repeat server is started");
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		int count = 0;
		DatagramSocket serverSocket = new DatagramSocket(port);
        DatagramPacket packetReceived = null;
		boolean test = true;
		float random;
		Random ran=new Random();
		continueLoop: while (test) {
				byte[] data = new byte[2048];
				packetReceived = new DatagramPacket(data, data.length);
				serverSocket.receive(packetReceived);
				String dataString = new String(packetReceived.getData()).substring(0, packetReceived.getLength());
				binaryToDecimal btd = new binaryToDecimal();
				int seqNumber = btd.binaryToDecimalByString(dataString.substring(0, 32));
				int checksum = btd.binaryToDecimalByString(dataString.substring(32, 48));
				String packetType = dataString.substring(48, 64);
				String dataReceived = dataString.substring(64, dataString.length());
				if (packetType.equals("0000000000000000")) {
					test = false;
					break;
				}
				random=ran.nextFloat();
				if (random <= pos) {
					System.out.println("Packet loss, sequence number = " + seqNumber);
					continue continueLoop;
				} else if (validateChecksum(dataReceived, checksum) == 0) {
					InetAddress IP = packetReceived.getAddress();
					int portNo = packetReceived.getPort();
					byte[] acknowledgement = createPacket(seqNumber);
					DatagramPacket sendPacket = new DatagramPacket(acknowledgement, acknowledgement.length, IP,portNo);
					serverSocket.send(sendPacket);
					if (seqNumber == count) {
						bstream.write(dataReceived.getBytes());
						count++;
						if (firstPacket != null) {
							packet tempPacket = firstPacket;
							while (tempPacket != null) {
								 if(tempPacket.index != count)
									 break;
								System.out.println("writing: "+tempPacket.index);
								bstream.write(tempPacket.data.getBytes());
								firstPacket = firstPacket.link;
								tempPacket = tempPacket.link;
								count++;
							}
						}
					} else if (seqNumber > count) {
						selective_repeat_server.allignPackets(seqNumber,dataReceived);
					}
				}
		}
		FileOutputStream file = null;
	    file = new FileOutputStream(filename);
		bstream.writeTo(file);
		file.close();
	    serverSocket.close();
	}

	public static byte[] createPacket(int seqNo) {
		packetCreation pck = new packetCreation();
		String packet = pck.createPacket(seqNo);
		return packet.getBytes();
	}

	
	public static int validateChecksum(String data, int oldChecksum) {
		checksum chk = new checksum();
		int checkans = chk.checksumCalculationString(data);
		checkans = Integer.parseInt("FFFF", 16) - checkans;
		int validity = checkans + oldChecksum;
		validity = Integer.parseInt("FFFF", 16) - validity;
		return validity;
	}
	
	static void allignPackets(int seqNo, String receivedData) {
		packet intermediatePacket = new packet(seqNo, receivedData);
		if (firstPacket == null)
			firstPacket = intermediatePacket;
		else {
			packet tempPacket = firstPacket;
			packet previousPacket = firstPacket;
			while (tempPacket.link != null && tempPacket.index < seqNo) {
				previousPacket = tempPacket;
				tempPacket = tempPacket.link;
			}
			if (tempPacket.index < seqNo){
				tempPacket.link = intermediatePacket;
			}
			else {
				if(previousPacket!=tempPacket)
					previousPacket.link = intermediatePacket;
				else
					firstPacket = intermediatePacket;
				    intermediatePacket.link = tempPacket;
			}
		}
	}

}
