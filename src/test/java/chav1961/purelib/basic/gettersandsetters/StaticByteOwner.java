package chav1961.purelib.basic.gettersandsetters;

public class StaticByteOwner {
	public static byte value = Byte.MIN_VALUE;
	private static byte privateValue = Byte.MIN_VALUE;

	public StaticByteOwner(){
		privateValue = Byte.MIN_VALUE;
	}
	
	public byte getPrivateValue(){return privateValue;}
	public void setPrivateValue(final byte newValue){privateValue = newValue;}
}
