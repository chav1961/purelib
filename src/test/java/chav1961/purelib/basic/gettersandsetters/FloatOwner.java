package chav1961.purelib.basic.gettersandsetters;

public class FloatOwner {
	public float value = Float.MIN_VALUE;
	private float privateValue = Float.MIN_VALUE;
	
	public float getPrivateValue(){return privateValue;}
	public void setPrivateValue(final float newValue){privateValue = newValue;}
}
