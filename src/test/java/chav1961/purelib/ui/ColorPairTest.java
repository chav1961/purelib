package chav1961.purelib.ui;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class ColorPairTest {
	@Test
	public void basicTest() {
		final ColorPair	cp1 = new ColorPair(Color.WHITE,Color.BLACK), cp2 = new ColorPair(Color.WHITE,Color.BLACK), cp3 = new ColorPair(Color.BLACK,Color.WHITE);
		
		Assert.assertEquals(Color.WHITE,cp1.getForeground());
		Assert.assertEquals(Color.BLACK,cp1.getBackground());
		Assert.assertEquals(cp1,cp2);
		Assert.assertEquals(cp1.toString(),cp2.toString());
		Assert.assertFalse(cp1.equals(cp3));

		cp1.setForeground(Color.MAGENTA);
		Assert.assertEquals(Color.MAGENTA,cp1.getForeground());
		
		cp1.setBackground(Color.CYAN);
		Assert.assertEquals(Color.CYAN,cp1.getBackground());
		
		try{new ColorPair(null,Color.white);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new ColorPair(Color.white,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{cp1.setForeground(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{cp1.setBackground(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
