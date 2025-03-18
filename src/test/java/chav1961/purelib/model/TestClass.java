package chav1961.purelib.model;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.ui.interfaces.Format;

@LocaleResource(value="test",tooltip="")
public class TestClass {
	@Format("m")
	public byte testByte = 10;
	@Format("m")
	public short testShort = 10;
	@Format("m")
	public int testInt = 10;
	@Format("m")
	public long testLong = 10;
	@Format("m")
	public float testFloat = 10;
	@Format("m")
	public double testDouble = 10;
	@Format("m")
	public char testChar = 10;
	@Format("m")
	public boolean testBoolean = true;
	@Format("m")
	public String testString = null;
	
	public void testMethod() {
	}
}
