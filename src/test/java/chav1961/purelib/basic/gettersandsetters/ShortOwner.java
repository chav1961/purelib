package chav1961.purelib.basic.gettersandsetters;

public class ShortOwner {
	public short value = Short.MIN_VALUE;
	private short privateValue = Short.MIN_VALUE;
	
	public short getPrivateValue(){return privateValue;}
	public void setPrivateValue(final short newValue){privateValue = newValue;}
}
