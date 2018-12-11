package chav1961.purelib.basic.gettersandsetters;

public class BooleanOwner {
	public boolean value = false;
	private boolean privateValue = false;
	
	public boolean getPrivateValue(){return privateValue;}
	public void setPrivateValue(final boolean newValue){privateValue = newValue;}
}
