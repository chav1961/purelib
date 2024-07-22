package chav1961.purelib.basic;

public class IntOwner {
	public int value = Integer.MIN_VALUE;
	private int privateValue = Integer.MIN_VALUE;
	
	public int getPrivateValue(){return privateValue;}
	public void setPrivateValue(final int newValue){privateValue = newValue;}
}
