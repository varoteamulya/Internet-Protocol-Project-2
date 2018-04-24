import java.io.IOException;
import java.net.*;
import java.nio.file.*;

public class selective_repeat_client {
	private static packet firstPacket;
	public static void main(String[] args) throws IOException {
		String hostname = args[0];
		int port = Integer.parseInt(args[1]);
		String filename = args[2];
		int N = Integer.parseInt(args[3]);
		int mss = Integer.parseInt(args[4]);
		int[] marker = new int[N];
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress serverIP = null;
		serverIP = InetAddress.getByName(hostname);
        Path filePath = Paths.get(filename);
		byte[] dataPacket = null;
	    dataPacket = Files.readAllBytes(filePath);
	    dataFormatting(dataPacket, mss);
		int count = 0;
		int pointer = 0;
		int ackNo = -1;
		int m = 0;
		long startTime = System.currentTimeMillis();
		while ((count * mss) < dataPacket.length) {
			for (m = 0; m < N; m++) {
				if ((count * mss) > dataPacket.length)
					break;
				if (marker[m] == 2) {
					count++;
					continue;
				}
				packet temp = firstPacket;
				while (temp!=null &&temp.index != count)
					temp = temp.link;
				if(temp==null)
					break;
				String data = temp.data;
				byte[] header = createHeader(count, data);
				byte[] dataBytes = data.getBytes();
				byte[] packetToSend = new byte[header.length + dataBytes.length];
				for (int i = 0, j = 0; i < packetToSend.length; i++) { 
					if (i < header.length)
						packetToSend[i] = header[i];
					else {
						packetToSend[i] = dataBytes[j];
						j++;
					}
				}
				DatagramPacket toReceiver = new DatagramPacket(packetToSend, packetToSend.length, serverIP, port);
				try {
					clientSocket.send(toReceiver);
					System.out.println("Packet sent : " + count);
					marker[m] = 1;
					count++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("current index: " + count + " with m: " + m);
			int timeout = 1000;
			byte[] receive = new byte[1024];
			DatagramPacket fromReceiver = new DatagramPacket(receive, receive.length);
			boolean flag = true;

			count = count - m;
			try {
				clientSocket.setSoTimeout(timeout);
				while (flag) {
					clientSocket.receive(fromReceiver);
					ackNo = ackPacketCreation(fromReceiver.getData());
					System.out.println("Ack received for : " + ackNo);
					if (ackNo != -1) {
						int index = ackNo - count;
						marker[index] = 2;
						if (index == 0) {
							while(marker[index]==2){
							for (int i = 1; i < N; i++) {
								marker[i - 1] = marker[i];
							}
							marker[N - 1] = -1;
							count++;
							}
						}
					}
				}
			} catch (SocketTimeoutException ste) {
				System.out.println("Timeout, sequence number = " + ackNo);
			}
		}
		String lastPacket = "000000000000000000000000000000000000000000000000000000000000000000000000000";
		byte[] sendeof = lastPacket.getBytes();
		DatagramPacket endPacket = new DatagramPacket(sendeof, sendeof.length, serverIP, port);
		clientSocket.send(endPacket);
		long endTime = System.currentTimeMillis();
		System.out.println("Average Delay: " + (endTime - startTime));
	}

	public static String checksumCalculation(String data) {
		checksum chk = new checksum();
		int ans = chk.checksumCalculationString(data);
		String padding = Integer.toBinaryString(ans);
		for (int h = padding.length(); h < 16; h++) {
			padding = "0" + padding;
		}
		return padding;
	}

	public static byte[] createHeader(int sequence, String data) {
		String sequenceStr = Integer.toBinaryString(sequence);
		String checksum = checksumCalculation(data);
		String fixedVal = "0101010101010101";
		for (int i = sequenceStr.length(); i < 32; i++) {
			sequenceStr = "0" + sequenceStr;
		}
		String header = sequenceStr + checksum + fixedVal;
		return header.getBytes();
	}
	
	public static void dataFormatting(byte[] dataPacket, int MSS) {
		int totalPackets = (int) Math.ceil((double) dataPacket.length / MSS);
		String dataString = new String(dataPacket);
		for (int i = 0; i < totalPackets; i++) {
			int j = MSS * (i + 1);
			if (j > dataString.length()) {
				j = dataString.length();
			}
			String seg = dataString.substring(MSS * i, j);
			packet segment = new packet(i, seg);
			if (firstPacket == null) {
				firstPacket = segment;
			} else {
				packet temp = firstPacket;
				while (temp.link != null) {
					temp = temp.link;
				}
				temp.link = segment;
			}

		}
	}

	
	public static int ackPacketCreation(byte[] data) {
		String acknowledge = "";
		for (int i = 0; i < 64; i++) {
			if (data[i] == 48) {
				acknowledge += "0";
			} else {
				acknowledge += "1";
			}
		}
		String packetType = acknowledge.substring(48, 64);
		if (packetType.equals("1010101010101010")) {
			binaryToDecimal btd = new binaryToDecimal();
			return btd.binaryToDecimalByString(acknowledge.substring(0, 32));
		}
		return -1;
	}

}
