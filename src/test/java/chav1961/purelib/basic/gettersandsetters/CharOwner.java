package chav1961.purelib.basic.gettersandsetters;

public class CharOwner {
	public char value = Character.MIN_VALUE;
	private char privateValue = Character.MIN_VALUE;
	
	public char getPrivateValue(){return privateValue;}
	public void setPrivateValue(final char newValue){privateValue = newValue;}
}
