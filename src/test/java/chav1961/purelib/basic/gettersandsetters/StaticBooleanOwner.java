package chav1961.purelib.basic.gettersandsetters;

public class StaticBooleanOwner {
	public static boolean value = false;
	private static boolean privateValue = false;
	
	public StaticBooleanOwner(){
		privateValue = false;
	}
	
	public boolean getPrivateValue(){return privateValue;}
	public void setPrivateValue(final boolean newValue){privateValue = newValue;}
}
