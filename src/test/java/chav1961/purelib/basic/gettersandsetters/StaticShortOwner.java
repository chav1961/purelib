package chav1961.purelib.basic.gettersandsetters;

public class StaticShortOwner {
	public static short value = Short.MIN_VALUE;
	private static short privateValue = Short.MIN_VALUE;
	
	public StaticShortOwner(){
		privateValue = Short.MIN_VALUE;
	}
	
	public short getPrivateValue(){return privateValue;}
	public void setPrivateValue(final short newValue){privateValue = newValue;}
}
