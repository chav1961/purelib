package chav1961.purelib.basic;

public class ByteOwner {
	public byte value = Byte.MIN_VALUE;
	private byte privateValue = Byte.MIN_VALUE;
	
	public byte getPrivateValue(){return privateValue;}
	public void setPrivateValue(final byte newValue){privateValue = newValue;}
}
