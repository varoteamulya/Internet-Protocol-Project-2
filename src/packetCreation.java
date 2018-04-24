import java.net.DatagramPacket;

public class packetCreation {
	
   String createPacket(int seq) {
       String packet=Integer.toBinaryString(seq);
       for(int i=packet.length();i<32;i++) {
       	packet="0"+packet;
       }
       packet+="00000000000000001010101010101010";
       return packet;
   }
   
}
