package chav1961.purelib.basic.gettersandsetters;

public class StaticReferencedOwner {
	public static String value = null;
	private static String privateValue = null;
	
	public StaticReferencedOwner(){
		privateValue = null;
	}
	
	public String getPrivateValue(){return privateValue;}
	public void setPrivateValue(final String newValue){privateValue = newValue;}
}
