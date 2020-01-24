package chav1961.purelib.basic;

public class DoubleOwner {
	public double value = Double.MIN_VALUE;
	private double privateValue = Double.MIN_VALUE;
	
	public double getPrivateValue(){return privateValue;}
	public void setPrivateValue(final double newValue){privateValue = newValue;}
}
