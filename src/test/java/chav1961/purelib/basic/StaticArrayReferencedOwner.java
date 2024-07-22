package chav1961.purelib.basic;

public class StaticArrayReferencedOwner {
	public static int[] value = null;
	private static int[] privateValue = null;

	public StaticArrayReferencedOwner() {
		privateValue = null;
	}
	
	public int[] getPrivateValue(){return privateValue;}
	public void setPrivateValue(final int[] newValue){privateValue = newValue;}
}
