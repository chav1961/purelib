package chav1961.purelib.ui.swing.terminal;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import org.junit.Assert;
import org.junit.Test;


public class PseudoConsoleTest {
	@Test
	public void basicTest() {
		final PseudoConsole	pc = new PseudoConsole(10,10);
		
		Assert.assertEquals(10,pc.width);
		Assert.assertEquals(10,pc.height); 

		pc.writeAttribute(1,1,Color.RED,Color.BLUE);
		Assert.assertArrayEquals(new Color[]{Color.RED,Color.BLUE},pc.readAttribute(1,1));
		Assert.assertArrayEquals(new Color[]{Color.GREEN,Color.BLACK},pc.readAttribute(2,2));

		try{pc.writeAttribute(0,1,Color.RED,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.writeAttribute(11,1,Color.RED,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.writeAttribute(1,0,Color.RED,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.writeAttribute(1,11,Color.RED,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.writeAttribute(1,1,null,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{pc.writeAttribute(1,1,Color.RED,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		
		pc.writeAttribute(new Point(3,3),Color.RED,Color.BLUE);
		Assert.assertArrayEquals(new Color[]{Color.RED,Color.BLUE},pc.readAttribute(new Point(3,3)));
		Assert.assertArrayEquals(new Color[]{Color.GREEN,Color.BLACK},pc.readAttribute(new Point(4,4)));

		try{pc.writeAttribute((Point)null,Color.RED,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{pc.writeAttribute(new Point(1,1),null,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{pc.writeAttribute(new Point(1,1),Color.RED,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		
		pc.writeAttribute(new Rectangle(5,5,2,2),Color.RED,Color.BLUE);
		Assert.assertArrayEquals(new Color[][][]{
				new Color[][]{new Color[]{Color.RED,Color.BLUE},new Color[]{Color.RED,Color.BLUE}}
				,new Color[][]{new Color[]{Color.RED,Color.BLUE},new Color[]{Color.RED,Color.BLUE}}}
		,pc.readAttribute(new Rectangle(5,5,2,2)));
		Assert.assertArrayEquals(new Color[][][]{
			new Color[][]{new Color[]{Color.GREEN,Color.BLACK},new Color[]{Color.GREEN,Color.BLACK}}
			,new Color[][]{new Color[]{Color.GREEN,Color.BLACK},new Color[]{Color.GREEN,Color.BLACK}}}
		,pc.readAttribute(new Rectangle(7,7,2,2)));

		
		try{pc.writeAttribute((Rectangle)null,Color.RED,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{pc.writeAttribute(new Rectangle(5,5,2,2),null,Color.BLUE);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{pc.writeAttribute(new Rectangle(5,5,2,2),Color.RED,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		pc.writeContent(1,1,'a');
		Assert.assertEquals('a',pc.readContent(1,1));
		Assert.assertEquals(' ',pc.readContent(2,2));

		try{pc.writeContent(0,1,'a');
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.writeContent(11,1,'a');
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.writeContent(1,0,'a');
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.writeContent(1,11,'a');
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		pc.writeContent(new Point(3,3),'b');
		Assert.assertEquals('b',pc.readContent(new Point(3,3)));
		Assert.assertEquals(' ',pc.readContent(new Point(4,4)));

		try{pc.writeContent((Point)null,'a');
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		pc.writeContent(new Rectangle(5,5,2,2),"cdef".toCharArray());
		Assert.assertArrayEquals("cdef".toCharArray(),pc.readContent(new Rectangle(5,5,2,2)));
		Assert.assertArrayEquals("    ".toCharArray(),pc.readContent(new Rectangle(7,7,2,2)));
		
		try{pc.writeContent((Rectangle)null,"cdef".toCharArray());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{pc.writeContent(new Rectangle(5,5,2,2),(char[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		pc.writeContent(new Rectangle(5,5,2,2),"cdef");
		Assert.assertArrayEquals("cdef".toCharArray(),pc.readContent(new Rectangle(5,5,2,2)));
		Assert.assertArrayEquals("    ".toCharArray(),pc.readContent(new Rectangle(7,7,2,2)));

		try{pc.writeContent((Rectangle)null,"cdef");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{pc.writeContent(new Rectangle(5,5,2,2),(String)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		pc.scrollUp(Color.YELLOW,Color.WHITE);
	}
}
