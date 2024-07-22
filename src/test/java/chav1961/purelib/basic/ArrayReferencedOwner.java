package chav1961.purelib.basic;

public class ArrayReferencedOwner {
	public int[] value = null;
	private int[] privateValue = null;
	
	public int[] getPrivateValue(){return privateValue;}
	public void setPrivateValue(final int[] newValue){privateValue = newValue;}
}
