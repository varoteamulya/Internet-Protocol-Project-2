import java.util.Arrays;

public class binaryToDecimal {
    int binaryToDecimal(byte [] st){
	     String str=new String(Arrays.copyOfRange(st, 0, 32)); 
	     double j=0;
	     for(int i=0;i<str.length();i++){
	        if(str.charAt(i)== '1'){
	         j=j+ Math.pow(2,str.length()-1-i);
	     }

	    }
	    return (int) j;
   }
    
    int binaryToDecimalByString(String sequenceNo)
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
    
}
