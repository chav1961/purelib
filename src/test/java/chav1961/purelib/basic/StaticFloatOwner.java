package chav1961.purelib.basic;

public class StaticFloatOwner {
	public static float value = Float.MIN_VALUE;
	private static float privateValue = Float.MIN_VALUE;
	
	public StaticFloatOwner(){
		privateValue = Float.MIN_VALUE;
	}
	
	public float getPrivateValue(){return privateValue;}
	public void setPrivateValue(final float newValue){privateValue = newValue;}
}
