package chav1961.purelib.ui.swing.useful.svg;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class SVGUtilsTest {
	@Test
	public void extractPointsTest() throws SyntaxException {
		Assert.assertEquals(1,SVGUtils.extractPoints(" 10 20 ").length);
		Assert.assertEquals(1,SVGUtils.extractPoints(" -3.5 9 ").length);
		
		final Point2D[]	result = SVGUtils.extractPoints(" -3.5 9 ");
		
		Assert.assertEquals(result[0].getX(),-3.5,0.0001);
		Assert.assertEquals(result[0].getY(),9,0.0001);

		try{SVGUtils.extractPoints(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.extractPoints("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{SVGUtils.extractPoints(" ");
			Assert.fail("Mandatory exception was not detected (no any numbers in the string)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractPoints(" 10");
			Assert.fail("Mandatory exception was not detected (odd numbers in the string)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractPoints(" 10x 20");
			Assert.fail("Mandatory exception was not detected (illegal number in the string)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void extractCommandsTest() throws SyntaxException {
		// M and it's chain
		GeneralPath	path = SVGUtils.extractCommands("M 10 20");
		
		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 20 m 5 5");
		
		Assert.assertEquals(15,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(25,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("m 5 5 -5 -5");
		
		Assert.assertEquals(0,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(0,path.getCurrentPoint().getY(),0.0001);
		
		// L and it's chain
		path = SVGUtils.extractCommands("M 10 20 L 30 40");
		
		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 20 l 20 20");
		
		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 20 L 20 20 -20 -30");
		
		Assert.assertEquals(-20,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(-30,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 20 l 20 20 -20 -30");
		
		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(10,path.getCurrentPoint().getY(),0.0001);

		// L, H, V and it's chain
		path = SVGUtils.extractCommands("M 10 20 H 20 V 20");

		Assert.assertEquals(20,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 20 h 20 v 20");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);

		// M, L, Z
		path = SVGUtils.extractCommands("M 10 20 L 30 30 Z");

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);
	
		// M, A
		path = SVGUtils.extractCommands("M 10 20 A 20 20 0 0 0 30 30");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 20 a 20 20 0 0 0 30 30");

		Assert.assertEquals(40,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(50,path.getCurrentPoint().getY(),0.0001);

		// M, C and it's chain 
		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20");

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 c 0 10 10 10 0 10");

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20 20 20 30 20 30 30");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 c 0 10 10 10 0 10 0 10 10 10 0 10");

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		// M, C, S and it's chain 
		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20 S 30 30 30 10");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(10,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20 s 10 10 10 10 10 10 10 10");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		// M, Q and it's chain 
		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20");

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 q 0 10 0 10");

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 20 20 30 30");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 q 0 10 0 10 0 10 0 10");

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		// M, Q, T and it's chain 
		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 T 30 30");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 T 30 30 40 40");

		Assert.assertEquals(40,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 t 20 10");

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 t 20 10 20 10");

		Assert.assertEquals(50,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		// Exceptions
		try{SVGUtils.extractCommands(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.extractCommands("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{SVGUtils.extractCommands(" ");
			Assert.fail("Mandatory exception was not detected (no any commands)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractCommands("@");
			Assert.fail("Mandatory exception was not detected (unknown command)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractCommands("M 10 10 Z @");
			Assert.fail("Mandatory exception was not detected (some content after 'Z')");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractCommands("M 10 Z");
			Assert.fail("Mandatory exception was not detected (not a number in the command )");
		} catch (SyntaxException exc) {
		}
	}
}
