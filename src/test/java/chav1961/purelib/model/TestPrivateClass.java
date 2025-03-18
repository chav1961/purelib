package chav1961.purelib.model;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.ui.interfaces.Format;

@LocaleResource(value="test",tooltip="")
public class TestPrivateClass {
	@Format("m")
	byte testByte = 10;
	@Format("m")
	short testShort = 10;
	@Format("m")
	int testInt = 10;
	@Format("m")
	long testLong = 10;
	@Format("m")
	float testFloat = 10;
	@Format("m")
	double testDouble = 10;
	@Format("m")
	char testChar = 10;
	@Format("m")
	boolean testBoolean = true;
	@Format("m")
	String testString = null;
	
	void testMethod() {
	}
}
