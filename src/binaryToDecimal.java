import java.util.Arrays;
/*
 The class for converting binary value to decimal. There are two methods, one takes the byte stream and the 
 other takes the string value 
 */
public class binaryToDecimal {
	//method taht takes the byte stream
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
    //Methods that takes the string
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
