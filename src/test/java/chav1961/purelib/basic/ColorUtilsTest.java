package chav1961.purelib.basic;

import java.awt.Color;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class ColorUtilsTest {
	@Test
	public void colorsTest() {
		Assert.assertEquals(Color.BLACK,ColorUtils.colorByName("black",Color.BLACK));
		Assert.assertEquals(Color.GREEN,ColorUtils.colorByName("unknown",Color.GREEN));
		Assert.assertEquals("Green",ColorUtils.nameByColor(Color.GREEN,"green"));
		Assert.assertEquals("unknown",ColorUtils.nameByColor(new Color(1,2,3),"unknown"));
		
		try{ColorUtils.colorByName(null,Color.gray);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ColorUtils.colorByName("",Color.gray);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{ColorUtils.nameByColor(null,"unknown");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}		
	}

}
