package chav1961.purelib.basic.gettersandsetters;

public class StaticCharOwner {
	public static char value = Character.MIN_VALUE;
	private static char privateValue = Character.MIN_VALUE;

	public StaticCharOwner(){
		privateValue = Character.MIN_VALUE;
	}
	
	public char getPrivateValue(){return privateValue;}
	public void setPrivateValue(final char newValue){privateValue = newValue;}
}
