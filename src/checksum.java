
public class checksum {
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
}
