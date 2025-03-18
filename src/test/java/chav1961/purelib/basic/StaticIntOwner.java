package chav1961.purelib.basic;

public class StaticIntOwner {
	public static int value = Integer.MIN_VALUE;
	private static int privateValue = Integer.MIN_VALUE;
	
	public StaticIntOwner(){
		privateValue = Integer.MIN_VALUE;
	}
	
	public int getPrivateValue(){return privateValue;}
	public void setPrivateValue(final int newValue){privateValue = newValue;}
}
