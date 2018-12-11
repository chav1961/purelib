package chav1961.purelib.basic.gettersandsetters;

public class LongOwner {
	public long value = Long.MIN_VALUE;
	private long privateValue = Long.MIN_VALUE;
	
	public long getPrivateValue(){return privateValue;}
	public void setPrivateValue(final long newValue){privateValue = newValue;}
}
