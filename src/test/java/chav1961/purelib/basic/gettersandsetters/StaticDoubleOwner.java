package chav1961.purelib.basic.gettersandsetters;

public class StaticDoubleOwner {
	public static double value = Double.MIN_VALUE;
	private static double privateValue = Double.MIN_VALUE;
	
	public StaticDoubleOwner(){
		privateValue = Double.MIN_VALUE;
	}
	
	public double getPrivateValue(){return privateValue;}
	public void setPrivateValue(final double newValue){privateValue = newValue;}
}
