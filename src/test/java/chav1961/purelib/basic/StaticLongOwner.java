package chav1961.purelib.basic;

public class StaticLongOwner {
	public static long value = Long.MIN_VALUE;
	private static long privateValue = Long.MIN_VALUE;
	
	public StaticLongOwner(){
		privateValue = Long.MIN_VALUE;
	}
	
	public long getPrivateValue(){return privateValue;}
	public void setPrivateValue(final long newValue){privateValue = newValue;}
}
