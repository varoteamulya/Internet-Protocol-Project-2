
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mahek Chheda
 */
public class selective_repeat_client {
    String hostname="127.0.0.1";
    int n,mss,seq;
    static int strt=0,end=0,port;
    static DatagramSocket clientsocket;
    File name;
    byte [][] fs;
    static InetAddress ip;
    boolean done=false;
    packets p[];
    boolean ack[];
    long rtt=0;
    selective_repeat_client(String host,int a,String f,int b,int c)
    {
        hostname=host;
        port=a;
        n=b;
        //buffer=n;
        mss=c;
        try
        {
            clientsocket=new DatagramSocket();
            name=new File(f);
            ip=InetAddress.getByName(hostname);
        }
        catch(SocketException s)
        {
            System.err.println(s);
        }
        catch(UnknownHostException h)
        {
            System.err.println(h);
        }
    }
    public static void main(String args[])
    {
//        String arg[]=new String [5];
//        arg[0]="127.0.0.1";
//        arg[1]="10000";
//        arg[2]="11.txt";
//        arg[3]="1";
//        arg[4]="500";
        selective_repeat_client arc=new selective_repeat_client(args[0],Integer.parseInt(args[1]),args[2],Integer.parseInt(args[3]),Integer.parseInt(args[4]));
        System.out.println("Preparing file for transfer");
        arc.filesplit();
        System.out.println("Sending File...Please wait");
        arc.execute();
       
        System.out.println("Time taken="+arc.rtt);
        System.out.println("File Transfer Complete!!");
    }
    void filesplit()
    {
        int i=(int)name.length()/mss;
        int j;
        System.out.println("File Size::"+name.length()+"bytes");
        fs=new byte[i+1][mss];
        p=new packets[i+1];
        ack=new boolean[i+1];
        try
        {
            byte [] bytearray  = new byte [(int)name.length()];
            FileInputStream fin = new FileInputStream(name);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(bytearray,0,bytearray.length);
            for(j=0;j<bytearray.length;j++)
            {
              fs[j/mss][j%mss]=bytearray[j];
              //System.out.println(j);
            }
            i=j/mss;
            while(j%mss!=0)
            {
                j++;
                fs[i][j%mss]=0;
                //System.out.println(j);
            }
            j=0;
        }
        catch(FileNotFoundException e)
        {
            System.err.println();
        }
        catch(IOException er)
        {
            System.err.println(er);
        }
    }
    void execute()
    {
        long strt=System.currentTimeMillis();
        send();
        one:while((selective_repeat_client.strt*mss)<(int)name.length())
        {
            send();
            byte [] receivedata=new byte[1024];
            DatagramPacket receive=new DatagramPacket(receivedata,receivedata.length);
            try
            {
                clientsocket.setSoTimeout(1000);
                selective_repeat_client.clientsocket.receive(receive);
                String unpack=new String(receive.getData());
                if(unpack.substring(48,64).equals("1010101010101010"))
                {
                    String seqtemp=unpack.substring(0,32);
                    int seqn=bintodeci(seqtemp);
                    ack[seqn]=true;
                    window();
                    p[seqn].stop();
                }
            }
            catch(IOException ioe)
            {
                // System.err.println(ioe);
                 continue one;
            }
         }
        long end=System.currentTimeMillis();
        rtt=end-strt;
        endfile();
    }
    void send()
    {
     //  System.out.println("current available buffer="+(arq_client.end-arq_client.strt));
       while(((selective_repeat_client.end-selective_repeat_client.strt)<n&&fs.length>selective_repeat_client.end))
       {
           p[end]=new packets(end,checksum(fs[end]),fs[end]);
           ack[end]=false;
           p[selective_repeat_client.end].start();
           selective_repeat_client.end++;
        }  
    }
    void endfile()
    {
        try
        {
           String temp=Integer.toBinaryString(selective_repeat_client.strt+1);
            for(int i=temp.length();i<32;i++)
                temp="0"+temp;
            byte emp[]=new byte[mss];
            String check=checksum(emp);
            String eof=temp+check+"0000000000000000"+(new String(emp));
            byte b[]=eof.getBytes();
            DatagramPacket p=new DatagramPacket(b,b.length,selective_repeat_client.ip,10000);
            clientsocket.send(p);
            //System.out.println("EOF sent");
        }
        catch(IOException ioe){
            System.err.print(ioe);
        }
    }
    String checksum(byte [] b)
    {
       byte sum1=0,sum2=0;
       for(int i=0;i<b.length;i=i+2)
       {
           sum1+=b[i];
           if((i+1)<b.length)
                sum2+=b[i+1];
       }
       String res=Byte.toString(sum1),res1=Byte.toString(sum2);
       for(int i=res.length();i<8;i++)
           res="0"+res;
       for(int i=res1.length();i<8;i++)
           res1="0"+res1;
       return res+res1;
    }
    int bintodeci(String str){
    double j=0;
    for(int i=0;i<str.length();i++){
        if(str.charAt(i)== '1'){
         j=j+ Math.pow(2,str.length()-1-i);
     }

    }
    return (int) j;
}
    void window()
    {
        for(int i=strt;strt<fs.length&&ack[i];i++)
        {
                strt++;
        }
    }
}
class packets extends Thread
{
    int seq_no;
    String checksum;
    byte [] data;
    packets(int a, String b,byte [] c)
    {
        seq_no=a;
        checksum=b;
        data=c;
    }
    public void run()
    {
        while(this.isAlive())
        {
           DatagramPacket p;
           String sequ=getseq(seq_no);
           String type="0101010101010101";
           String header=sequ+checksum+type;
           byte[] senddata;
           senddata=header.getBytes();
           byte[] pack= new byte[data.length+senddata.length];
           for(int i=0;i<pack.length;i++)
            {
                if(i<senddata.length)
                    pack[i]=senddata[i];
                else
                    pack[i]=data[i-senddata.length];
            }
            p= new DatagramPacket(pack,pack.length,selective_repeat_client.ip,10000);
            try
            {
                selective_repeat_client.clientsocket.send(p);
                //System.out.println("Packet sent with seq number"+seq_no);
                sleep(100);
                System.out.println("Timed out, Sequence Number="+seq_no);
            }
            catch(IOException ioe)
            {
                //System.out.println(ioe);
            }
            catch(InterruptedException ie)
            {
                //System.out.println(ie);
            }
        }
    }
    String getseq(int n)
    {
        String temp=Integer.toBinaryString(n);
        for(int i=temp.length();i<32;i++)
            temp="0"+temp;
        return temp;
    }
}
