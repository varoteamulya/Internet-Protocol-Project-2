
import java.io.*;
import java.net.*;
import java.util.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mahek Chheda
 */
public class selective_repeat_server {
    DatagramSocket serverSocket;
    static int portn;
    static InetAddress ip;
    String filename,data="";
    FileOutputStream fos ;
    int strt=0,n;
    File f;
    byte [][]buffer;
    boolean ack[];
    selective_repeat_server(int a,String name)
    {
        try
        {
            serverSocket=new DatagramSocket(a);
            f=new File(name);
        }
        catch(SocketException se)
        {
            System.err.println(se);
        }
    }
    public static void main(String args[])
    {
//        String arg[]=new String[3];
//        arg[0]="10000";
//        arg[1]="20.txt";
//        arg[2]="0.01";
        selective_repeat_server ars=new selective_repeat_server(Integer.parseInt(args[0]),args[1]);
        float p=Float.parseFloat(args[2]);
        ars.n=Integer.parseInt(args[3]);
        ars.buffer=new byte [ars.n][1024];
        ars.ack=new boolean[ars.n];
        Random rs=new Random();
        float r=0;
        boolean done=false;
        one:while(!done)
        {
            try
            {
                //System.out.println("Waiting for the packets");
                byte []receivedata=new byte[2048];
                DatagramPacket receive=new DatagramPacket(receivedata,receivedata.length);
                ars.serverSocket.receive(receive);
                //System.out.println("Packet received");
                 byte [] data=new byte[receive.getLength()-64];
                 System.arraycopy(receivedata,64, data,0,data.length);
                byte [] received=receive.getData();
                String temp1=new String(received);
//                if(ars.eof(temp1.substring(0, 16)))
//                {
//                    done=false;
//                    continue one;
//                }
                ip=receive.getAddress();
                portn=receive.getPort();
                int seqno=ars.bintodeci(received, 32);
                r=rs.nextFloat();
                if(r<=p)
                {
                    System.out.println("Packet loss, Sequence number="+seqno);
                    continue one;
                }
                String chck=new String(Arrays.copyOfRange(received, 32, 48));
                String type=new String(Arrays.copyOfRange(received,48, 64));   
                String temp=ars.checksum(data);
                if(temp.equals(chck)&&ars.insert(seqno,data))
                {
                    //System.out.println("Checksum validated");
                    if(type.equals("0101010101010101"))
                    {
                       ars.writeintofile();
                       ars.serverSocket.send( ars.generate(seqno));
                      // System.out.println("Acknowledgement sent for"+seqno);
                    }
                    else
                    {
                        done=true;
                        //System.out.println("EOF reached");
                        ars.writeintofile();
                    }
                }
            }
            catch(IOException ioe)
            {
                System.out.println(ioe+"");
            }
        }
        System.out.println("File "+ars.f.getName()+"has been downloaded");
       
    }
    String checksum(byte [] b)
    {
       byte sum1=0,sum2=0;
       //System.out.println(b.length+" size of byte received");
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
    boolean eof(String a)
    {
        boolean status=false;
        if(a.equals("0000000000000000"))
        {
            status=true;
            writeintofile();
        }
        return status;
    }
    DatagramPacket generate(int seq)
    {
        DatagramPacket p=null;
        String sequ=getseq(seq);
        sequ+="00000000000000001010101010101010";
        byte []send=sequ.getBytes();
        try
        {
            p=new DatagramPacket(send,send.length,ip,portn);
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
        return p;
    }
    String getseq(int n)
    {
        String temp=Integer.toBinaryString(n);
        for(int i=temp.length();i<32;i++)
            temp="0"+temp;
        return temp;
    }
    int bintodeci(byte [] st,int n)
    {
        String str=new String(Arrays.copyOfRange(st, 0, 32)); 
        //System.out.println(str);
        double j=0;
        for(int i=0;i<str.length();i++){
            if(str.charAt(i)== '1')
            {
                j=j+ Math.pow(2,str.length()-1-i);
            }
    }
    //    System.out.println("bintodeci"+(int)j);
        return (int) j;
    }
    void writeintofile()
    {
        for(;strt<n&&ack[strt];strt=(strt+1)%n)
        {
            //data1=data1.trim();
           FileOutputStream stream;
           try
            {
                stream = new FileOutputStream(f, true);
//                //System.out.println("Writing into file");
                ack[strt]=false;
                stream.write(buffer[strt]);                
            }
            catch(Exception fe)
            {
                System.out.println(fe);
            }   
         }
    }
    
    
boolean insert(int seqn,byte [] data)
    {
        int temp=seqn%n;
        if(!ack[temp])
        {
            buffer[temp]=data;
            ack[temp]=true;
        }
        return ack[temp];
    }
}
