
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
    
    public static int checksumCalculationString(String data) {
		String valueString = new String();
		int value, i, ans = 0;
		for (i = 0; i < data.length() - 2; i = i + 2) {
			value = (int) (data.charAt(i));
			valueString = Integer.toHexString(value);
			value = (int) (data.charAt(i + 1));
			valueString = valueString + Integer.toHexString(value);
			value = Integer.parseInt(valueString, 16);
			ans = ans + value;
		}
		if (data.length() % 2 == 0) {
			value = (int) (data.charAt(i));
			valueString = Integer.toHexString(value);
			value = (int) (data.charAt(i + 1));
			valueString = valueString + Integer.toHexString(value);
			value = Integer.parseInt(valueString, 16);
		} else {
			value = (int) (data.charAt(i));
			valueString = "00" + Integer.toHexString(value);
			value = Integer.parseInt(valueString, 16);
		}
		ans = ans + value;
		valueString = Integer.toHexString(ans);
		if (valueString.length() > 4) {
			int carry = Integer.parseInt(("" + valueString.charAt(0)), 16);
			valueString = valueString.substring(1, 5);
			ans = Integer.parseInt(valueString, 16);
			ans = ans + carry;
		}
		ans = Integer.parseInt("FFFF", 16) - ans;
		return ans;
	}
}
