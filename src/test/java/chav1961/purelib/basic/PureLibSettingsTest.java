package chav1961.purelib.basic;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

public class PureLibSettingsTest {

	@Test
	public void colorsTest() {
		Assert.assertEquals(PureLibSettings.colorByName("green",Color.GREEN),Color.GREEN);
		Assert.assertEquals(PureLibSettings.colorByName("unknown",Color.GREEN),Color.GREEN);
		Assert.assertEquals(PureLibSettings.nameByColor(Color.GREEN,"green"),"green");
		Assert.assertEquals(PureLibSettings.nameByColor(new Color(1,2,3),"unknown"),"unknown");
		
		try{PureLibSettings.colorByName(null,Color.gray);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PureLibSettings.colorByName("",Color.gray);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{PureLibSettings.nameByColor(null,"unknown");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}		
	}
}
