
import java.io.*;
import java.net.*;

public class simple_ftp_client {
    String hostname="127.0.0.1";
    int port,n,mss,count=0,buffer=0,seq,ack=0;
    DatagramSocket clientsocket;
    File name;
    byte [][] filesystem;
    InetAddress ip;
    boolean done=false;
    long roundTripTime=0;
    simple_ftp_client(String host,int a,String f,int b,int c) throws SocketException, UnknownHostException
    {
        hostname=host;
        port=a;
        n=b;
        mss=c;
        clientsocket=new DatagramSocket();
        name=new File(f);
        ip=InetAddress.getByName(hostname);
    }
    public static void main(String []args) throws NumberFormatException, IOException
    {
        long avgdelay=0;
       simple_ftp_client ftpServer=new simple_ftp_client(args[0],Integer.parseInt(args[1]),args[2],Integer.parseInt(args[3]),Integer.parseInt(args[4]));
       ftpServer.filesplit();
       ftpServer.roundTripTime=ftpServer.execute();
       System.out.println("Average Delay : "+(ftpServer.roundTripTime-avgdelay));
    }
    long execute()
    {
        long strt,end;                      
        strt=System.currentTimeMillis();       
        while((count*mss)<(int)name.length())
        {
            while(buffer<n&&filesystem.length>count)
            {
                DatagramPacket send=rdt_send(count);
                try
                {
                    clientsocket.send(send);
                    buffer++;
                    count++;
                }
                catch(IOException e)
                {
                    System.err.println(e);
                }
            }
            byte [] receivedData=new byte[1024];
            DatagramPacket packetReceived=new DatagramPacket(receivedData,receivedData.length);
            done=false;
            try
            {
                clientsocket.setSoTimeout(100);
                one:while(!done)
                {
                    clientsocket.receive(packetReceived);
                    String unpack=new String(packetReceived.getData());
                    if(unpack.substring(48,64).equals("1010101010101010"))
                    {
                        String seqtemp=unpack.substring(0,32);
                        int seqn=binaryToDecimal(seqtemp);
                        ack=seqn;
                        if(ack==count)
                        {
                            done=true;
                            count=ack;
                            buffer=0;
                            break one;
                        }
                    }
                }
            }
            catch(SocketTimeoutException sto)
            {
                System.out.println("Timeout, Sequence number="+(ack));
                buffer=count-ack;
                count=ack;
            }
            catch(IOException ioe)
            {
                System.err.println(ioe);
            }
        }
        end=System.currentTimeMillis();                
        lastData();
        return end-strt;                               
        
    }
    
    void filesplit() throws IOException
    {
        int i=(int)name.length()/mss;
        int j;
        System.out.println("File Size::"+name.length()+"bytes");
        filesystem=new byte[i+1][mss];
        byte [] bytearray  = new byte [(int)name.length()];
        FileInputStream fin = new FileInputStream(name);
        BufferedInputStream bin = new BufferedInputStream(fin);
        bin.read(bytearray,0,bytearray.length);
        for(j=0;j<bytearray.length;j++)
        {
         	filesystem[j/mss][j%mss]=bytearray[j];
        }
        i=j/mss;
        while(j%mss!=0)
        {
            j++;
            filesystem[i][j%mss]=0;
        }
     }
    

    int binaryToDecimal(String sequenceNo)
    {
        double j=0;
        for(int i=0;i<sequenceNo.length();i++)
        {
            if(sequenceNo.charAt(i)== '1')
            {
                j=j+ Math.pow(2,sequenceNo.length()-1-i);
            }
        }
        return (int) j;
    }
    
    
    String calculateChecksum(byte [] byteData)
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
    
    DatagramPacket rdt_send(int sequence)
    {
       DatagramPacket packet;
       String seq=Integer.toBinaryString(sequence);
       for(int i=seq.length();i<32;i++)
    	   seq="0"+seq;
       String checksum=calculateChecksum(filesystem[sequence]);
       String type="0101010101010101";
       String header=seq+checksum+type;
       byte[] senddata;
       senddata=header.getBytes();
       byte[] pack= new byte[mss+senddata.length];
       for(int i=0;i<pack.length;i++)
       {
           if(i<senddata.length)
               pack[i]=senddata[i];
           else
               pack[i]=filesystem[sequence][i-senddata.length];
       }
       packet= new DatagramPacket(pack,pack.length,ip,port);
       return packet;
    }
    
    void lastData()
    {
        try
        {
           String temp=Integer.toBinaryString(count);
            for(int i=temp.length();i<32;i++)
                temp="0"+temp;
            byte emp[]=new byte[mss];
            String check=calculateChecksum(emp);
            String eof=temp+check+"0000000000000000"+(new String(emp));
            byte b[]=eof.getBytes();
            DatagramPacket p=new DatagramPacket(b,b.length,ip,7735);
            clientsocket.send(p);
        }
        catch(IOException ioe){
            System.err.print(ioe);
        }
    }
    
   
}
