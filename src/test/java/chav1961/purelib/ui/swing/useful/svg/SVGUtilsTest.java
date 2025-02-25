package chav1961.purelib.ui.swing.useful.svg;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ConvertorInterface;
import chav1961.purelib.basic.interfaces.OnlineBooleanGetter;
import chav1961.purelib.basic.interfaces.OnlineCharGetter;
import chav1961.purelib.basic.interfaces.OnlineDoubleGetter;
import chav1961.purelib.basic.interfaces.OnlineFloatGetter;
import chav1961.purelib.basic.interfaces.OnlineIntGetter;
import chav1961.purelib.basic.interfaces.OnlineLongGetter;
import chav1961.purelib.basic.interfaces.OnlineObjectGetter;
import chav1961.purelib.basic.interfaces.OnlineStringGetter;

@Tag("OrdinalTestCategory")
public class SVGUtilsTest {
	@Test
	public void extractPointsTest() throws SyntaxException {
		Assert.assertEquals(1,SVGUtils.extractPoints(" 10 20 ", 1.0f).length);
		Assert.assertEquals(1,SVGUtils.extractPoints(" -3.5 9 ", 1.0f).length);
		
		final Point2D[]	result = SVGUtils.extractPoints(" -3.5 9 ", 1.0f);
		
		Assert.assertEquals(result[0].getX(),-3.5,0.0001); 
		Assert.assertEquals(result[0].getY(),9,0.0001);

		try{SVGUtils.extractPoints(null, 1.0f);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.extractPoints("", 1.0f);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{SVGUtils.extractPoints(" ", 1.0f);
			Assert.fail("Mandatory exception was not detected (no any numbers in the string)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractPoints(" 10", 1.0f);
			Assert.fail("Mandatory exception was not detected (odd numbers in the string)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractPoints(" 10x 20", 1.0f);
			Assert.fail("Mandatory exception was not detected (illegal number in the string)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void extractCommandsTest() throws SyntaxException {
		// M and it's chain
		GeneralPath	path = SVGUtils.extractCommands("M 10 20", 1.0f);
		
		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 20 m 5 5", 1.0f);
		
		Assert.assertEquals(15,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(25,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("m 5 5 -5 -5", 1.0f);
		
		Assert.assertEquals(0,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(0,path.getCurrentPoint().getY(),0.0001);
		
		// L and it's chain
		path = SVGUtils.extractCommands("M 10 20 L 30 40", 1.0f);
		
		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 20 l 20 20", 1.0f);
		
		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 20 L 20 20 -20 -30", 1.0f);
		
		Assert.assertEquals(-20,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(-30,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 20 l 20 20 -20 -30", 1.0f);
		
		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(10,path.getCurrentPoint().getY(),0.0001);

		// L, H, V and it's chain
		path = SVGUtils.extractCommands("M 10 20 H 20 V 20", 1.0f);

		Assert.assertEquals(20,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 20 h 20 v 20", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);

		// M, L, Z
		path = SVGUtils.extractCommands("M 10 20 L 30 30 Z", 1.0f);

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);
	
		// M, A
		path = SVGUtils.extractCommands("M 10 20 A 20 20 0 0 0 30 30", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 20 a 20 20 0 0 0 30 30", 1.0f);

		Assert.assertEquals(40,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(50,path.getCurrentPoint().getY(),0.0001);

		// M, C and it's chain 
		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20", 1.0f);

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 c 0 10 10 10 0 10", 1.0f);

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20 20 20 30 20 30 30", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 c 0 10 10 10 0 10 0 10 10 10 0 10", 1.0f);

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		// M, C, S and it's chain 
		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20 S 30 30 30 10", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(10,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 10 C 10 20 20 20 10 20 s 10 10 10 10 10 10 10 10", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		// M, Q and it's chain 
		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20", 1.0f);

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 q 0 10 0 10", 1.0f);

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(20,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 20 20 30 30", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 q 0 10 0 10 0 10 0 10", 1.0f);

		Assert.assertEquals(10,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		// M, Q, T and it's chain 
		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 T 30 30", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 T 30 30 40 40", 1.0f);

		Assert.assertEquals(40,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 t 20 10", 1.0f);

		Assert.assertEquals(30,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(30,path.getCurrentPoint().getY(),0.0001);

		path = SVGUtils.extractCommands("M 10 10 Q 10 20 10 20 t 20 10 20 10", 1.0f);

		Assert.assertEquals(50,path.getCurrentPoint().getX(),0.0001);
		Assert.assertEquals(40,path.getCurrentPoint().getY(),0.0001);
		
		// Exceptions
		try{SVGUtils.extractCommands(null, 1.0f);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.extractCommands("", 1.0f);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{SVGUtils.extractCommands(" ", 1.0f);
			Assert.fail("Mandatory exception was not detected (no any commands)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractCommands("@", 1.0f);
			Assert.fail("Mandatory exception was not detected (unknown command)");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractCommands("M 10 10 Z @", 1.0f);
			Assert.fail("Mandatory exception was not detected (some content after 'Z')");
		} catch (SyntaxException exc) {
		}
		try{SVGUtils.extractCommands("M 10 Z", 1.0f);
			Assert.fail("Mandatory exception was not detected (not a number in the command )");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void substitutionsTest() throws SyntaxException {
		Assert.assertFalse(SVGUtils.hasSubstitutionInside("missing"));
		Assert.assertTrue(SVGUtils.hasSubstitutionInside("${present}"));
		
		try{SVGUtils.hasSubstitutionInside(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final Map<String,Object>	content = Utils.mkMap("key1","value1","key2","${subst}","key3","value3");
		
		Assert.assertFalse(SVGUtils.hasAnySubstitutions(content,"key1","key3"));
		Assert.assertTrue(SVGUtils.hasAnySubstitutions(content,"key1","key2"));

		try{SVGUtils.hasAnySubstitutions(null,"key1","key3");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SVGUtils.hasAnySubstitutions(content,(String[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.hasAnySubstitutions(content);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.hasAnySubstitutions(content,"test",null);
			Assert.fail("Mandatory exception was not detected (null inside 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void buildGettersTest() throws ContentException {
		final String[]				currentValue = new String[1];
		// Boolean getter
		final OnlineBooleanGetter	obg = SVGUtils.buildOnlineGetter(OnlineBooleanGetter.class,"${value}",(ss)->currentValue[0]);
		
		currentValue[0] = "false";
		Assert.assertFalse(obg.isImmutable());
		Assert.assertFalse(obg.get());

		currentValue[0] = "true";
		Assert.assertFalse(obg.isImmutable());
		Assert.assertTrue(obg.get());

		Assert.assertTrue(SVGUtils.buildOnlineGetter(OnlineBooleanGetter.class,"true",(ss)->currentValue[0]).isImmutable());
		
		// Char getter
		final OnlineCharGetter		ocg = SVGUtils.buildOnlineGetter(OnlineCharGetter.class,"${value}",(ss)->currentValue[0]);
		
		currentValue[0] = "C";
		Assert.assertFalse(ocg.isImmutable());
		Assert.assertEquals('C',ocg.get());

		currentValue[0] = "";
		Assert.assertFalse(ocg.isImmutable());
		Assert.assertEquals(' ',ocg.get());
 
		Assert.assertTrue(SVGUtils.buildOnlineGetter(OnlineCharGetter.class,"C",(ss)->currentValue[0]).isImmutable());
		
		// Double getter
		final OnlineDoubleGetter	odg = SVGUtils.buildOnlineGetter(OnlineDoubleGetter.class,"${value}",(ss)->currentValue[0]);
		
		currentValue[0] = "10";
		Assert.assertFalse(odg.isImmutable());
		Assert.assertEquals(10,odg.get(),0.001);

		Assert.assertTrue(SVGUtils.buildOnlineGetter(OnlineDoubleGetter.class,"100",(ss)->currentValue[0]).isImmutable());
		
		// Float getter
		final OnlineFloatGetter		ofg = SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${value}",(ss)->currentValue[0]);
		
		currentValue[0] = "100";
		Assert.assertFalse(ofg.isImmutable());
		Assert.assertEquals(100f,ofg.get(),0.001f);

		Assert.assertTrue(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"200",(ss)->currentValue[0]).isImmutable());
		
		// Int getter
		final OnlineIntGetter		oig = SVGUtils.buildOnlineGetter(OnlineIntGetter.class,"${value}",(ss)->currentValue[0]);
		
		currentValue[0] = "1";
		Assert.assertFalse(oig.isImmutable());
		Assert.assertEquals(1,oig.get());

		Assert.assertTrue(SVGUtils.buildOnlineGetter(OnlineIntGetter.class,"300",(ss)->currentValue[0]).isImmutable());
		
		// Long getter
		final OnlineLongGetter		olg = SVGUtils.buildOnlineGetter(OnlineLongGetter.class,"${value}",(ss)->currentValue[0]);
		
		currentValue[0] = "-5";
		Assert.assertFalse(olg.isImmutable());
		Assert.assertEquals(-5,olg.get());

		Assert.assertTrue(SVGUtils.buildOnlineGetter(OnlineLongGetter.class,"400",(ss)->currentValue[0]).isImmutable());
		
		// String getter
		final OnlineStringGetter	osg = SVGUtils.buildOnlineGetter(OnlineStringGetter.class,"${value}",(ss)->currentValue[0]);
		
		currentValue[0] = "test";
		Assert.assertFalse(osg.isImmutable());
		Assert.assertEquals("test",osg.get());

		Assert.assertTrue(SVGUtils.buildOnlineGetter(OnlineStringGetter.class,"500",(ss)->currentValue[0]).isImmutable());
		
		// Object getter
		final OnlineObjectGetter<String>	oog = SVGUtils.buildOnlineObjectGetter(String.class,"${value}",(ss)->currentValue[0],new ConvertorInterface() {
																@Override
																public <T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException {
																	return (T)source;
																}
															});
		
		currentValue[0] = "test";
		Assert.assertFalse(oog.isImmutable());
		Assert.assertEquals("test",oog.get());

		Assert.assertTrue(SVGUtils.buildOnlineObjectGetter(String.class,"test",(ss)->currentValue[0],new ConvertorInterface() {
			@Override
			public <T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException {
				return (T)source;
			}
		}).isImmutable());
		 
		// Exceptions
		try{SVGUtils.buildOnlineGetter(null,"${value}",(ss)->currentValue[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SVGUtils.buildOnlineGetter(OnlineStringGetter.class,null,(ss)->currentValue[0]);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.buildOnlineGetter(OnlineStringGetter.class,"",(ss)->currentValue[0]);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.buildOnlineGetter(OnlineStringGetter.class,"${value}",null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try{SVGUtils.buildOnlineObjectGetter(null,"${value}",(ss)->currentValue[0],new ConvertorInterface() {
				@Override
				public <T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException {
					return (T)source;
				}
			});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SVGUtils.buildOnlineObjectGetter(String.class,null,(ss)->currentValue[0],new ConvertorInterface() {
				@Override
				public <T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException {
					return (T)source;
				} 
			});
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.buildOnlineObjectGetter(String.class,"",(ss)->currentValue[0],new ConvertorInterface() {
				@Override
				public <T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException {
					return (T)source;
				}
			});
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.buildOnlineObjectGetter(String.class,"${value}",null,new ConvertorInterface() {
				@Override
				public <T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException {
					return (T)source;
				}
			});
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{SVGUtils.buildOnlineObjectGetter(String.class,"${value}",(ss)->currentValue[0],null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void convertorTest() throws ContentException {
		final AffineTransform	at = new AffineTransform();
		final Point2D[]			points = new Point2D[] {new Point2D.Float(10,10), new Point2D.Float(20,20)};
		final GeneralPath		gp = new GeneralPath();
		
		Assert.assertEquals(Color.BLACK,SVGUtils.convertTo(Color.class,"black"));
		
		Assert.assertEquals(new BasicStroke(1),SVGUtils.convertTo(Stroke.class,"1.0f"));
		
		at.scale(2.0,2.0);
		Assert.assertEquals(at,SVGUtils.convertTo(AffineTransform.class,"scale(2,2)"));

		Assert.assertArrayEquals(points,SVGUtils.convertTo(Point2D[].class,"10 10 20 20"));
		
		gp.moveTo(10, 10);
		gp.lineTo(20,20);
		Assert.assertEquals(gp.getBounds(),SVGUtils.convertTo(GeneralPath.class,"M 10 10 L 20 20").getBounds());

		Assert.assertEquals(new Font("Arial",Font.PLAIN,12),SVGUtils.convertTo(Font.class,"12pt \"Arial\""));
		
		try{SVGUtils.convertTo(null,"M 10 10 L 20 20");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{SVGUtils.convertTo(GeneralPath.class,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SVGUtils.convertTo(GeneralPath.class,"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{SVGUtils.convertTo(String.class,"test");
			Assert.fail("Mandatory exception was not detected (unsupported 1-st argument)");
		} catch (SyntaxException exc) {
		}
	}

}
