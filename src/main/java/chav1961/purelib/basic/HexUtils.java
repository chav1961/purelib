package chav1961.purelib.basic;

/**
 * <p>This utility class is used to convert hexadecimal representations into bytes and bytes into hexadecimal representations.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class HexUtils {
	private static final char[]	HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	/**
	 * <p>Encode byte array to it's hexadecimal representation</p>
	 * @param bytes array to encode. Can't be null.
	 * @return char representation of the byte array. Can't be null.
	 * @throws NullPointerException array to encode is null
	 */
    public static final char[] encode(final byte[] bytes) throws NullPointerException {
    	if (bytes == null) {
    		throw new NullPointerException("Array to encode can't be null");
    	}
    	else {
    		final char[]	result = new char[2 * bytes.length];
    		int				index = 0;
    		
    		for (int item : bytes) {
    			result[index++] = HEX_DIGITS[item >> 8];
    			result[index++] = HEX_DIGITS[item & 0x0F];
    		}
    		return result;
    	}
    }

    /**
     * <p>Decode character representations to byte array</p> 
     * @param str character representation to decode. Can't be null.
     * @return byte array decoded. Can't be null.
	 * @throws NullPointerException array to encode is null
	 * @throws IllegalArgumentException invalid characters in the character representation
     */
    public static final byte[] decode(final char[] str) throws NullPointerException, IllegalArgumentException {
    	if (str == null) {
    		throw new NullPointerException("Array to decode can't be null");
    	}
    	else {
    		final byte[]	result = new byte[(str.length + 1) >> 1];
    		int				from = 0, to = 0, toMax = result.length;
    		
    		if ((str.length & 0x01) != 0) {
    			result[to++] = (byte)fromHex(str[from++]);
    		}
    		while (to < toMax) {
    			result[to++] = (byte)((fromHex(str[from++]) << 4) | fromHex(str[from++])); 
    		}
    		return result;
    	}
    }    
    
    /**
     * <p>Decode character representations to byte array</p> 
     * @param str character representation to decode. Can't be null.
     * @return byte array decoded. Can't be null.
	 * @throws NullPointerException array to encode is null
	 * @throws IllegalArgumentException invalid characters in the character representation
     */
    public static final byte[] decode(final CharSequence str) throws NullPointerException, IllegalArgumentException {
    	if (str == null) {
    		throw new NullPointerException("Array to decode can't be null");
    	}
    	else {
    		final byte[]	result = new byte[(str.length() + 1) >> 1];
    		int				from = 0, to = 0, toMax = result.length;
    		
    		if ((str.length() & 0x01) != 0) {
    			result[to++] = (byte)fromHex(str.charAt(from++));
    		}
    		while (to < toMax) {
    			result[to++] = (byte)((fromHex(str.charAt(from++)) << 4) | fromHex(str.charAt(from++))); 
    		}
    		return result;
    	}
    }

    private static int fromHex(char hexDigit) {
    	if (hexDigit >= '0' && hexDigit <= '9') {
    		return hexDigit - '0';
    	}
    	else if (hexDigit >= 'A' && hexDigit <= 'F') {
    		return hexDigit - 'A' + 10;
    	}
    	else if (hexDigit >= 'a' && hexDigit <= 'f') {
    		return hexDigit - 'a' + 10;
    	}
    	else {
    		throw new IllegalArgumentException("Wrong hex digit representation ["+hexDigit+"], content=[0x"+Integer.toHexString(hexDigit)+"]");
    	}
    }
}
