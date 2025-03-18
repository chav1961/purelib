package chav1961.purelib.ui.swing.useful.svg;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.CSSUtils.Unit;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ConvertorInterface;
import chav1961.purelib.basic.interfaces.OnlineFloatGetter;
import chav1961.purelib.basic.interfaces.OnlineObjectGetter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.AbstractPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.FillPolicy;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.LinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicLinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.RectPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicRectPainter;

@Tag("OrdinalTestCategory")
public class SVGPainterTest {
	private static final ConvertorInterface	CONV = 	new ConvertorInterface() {
														@Override
														public <T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException {
															return SVGUtils.convertTo(awaited, source.toString());
														}
													};	
	
	@Test
	public void basicTest() {
		final BufferedImage image = new BufferedImage(100,200,BufferedImage.TYPE_INT_ARGB);
		final Graphics2D 	g2d = image.createGraphics();		
		final SVGPainter	painter = new SVGPainter(100,200,Unit.PIXEL,FillPolicy.FILL_BOTH,new SVGPainter.LinePainter(0,0,100,200,Color.BLACK,new BasicStroke(1.0f)));
		
		Assert.assertEquals(100,painter.getWidth());
		Assert.assertEquals(200,painter.getHeight());
		
		painter.paint(g2d,200,400);
		Assert.assertArrayEquals(new int[] {0,0,0,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {255,255,255,255},image.getData().getPixel((int)1,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,255},image.getData().getPixel((int)99,(int)199,(int[])null));
		
		try{new SVGPainter(0,200,Unit.PIXEL,FillPolicy.FILL_BOTH,new SVGPainter.LinePainter(0,0,100,200,Color.BLACK,new BasicStroke(1.0f)));
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new SVGPainter(100,0,Unit.PIXEL,FillPolicy.FILL_BOTH,new SVGPainter.LinePainter(0,0,100,200,Color.BLACK,new BasicStroke(1.0f)));
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new SVGPainter(100,200,null,FillPolicy.FILL_BOTH,new SVGPainter.LinePainter(0,0,100,200,Color.BLACK,new BasicStroke(1.0f)));
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new SVGPainter(100,200,Unit.PIXEL,null,new SVGPainter.LinePainter(0,0,100,200,Color.BLACK,new BasicStroke(1.0f)));
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new SVGPainter(100,0,Unit.PIXEL,FillPolicy.FILL_BOTH,(AbstractPainter[])null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new SVGPainter(100,0,Unit.PIXEL,FillPolicy.FILL_BOTH);
			Assert.fail("Mandatory exception was not detected (empty 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new SVGPainter(100,0,Unit.PIXEL,FillPolicy.FILL_BOTH,(AbstractPainter)null);
			Assert.fail("Mandatory exception was not detected (nuls in 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void linePainterTest() throws ContentException {
		BufferedImage 	image = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
		Graphics2D 		g2d = image.createGraphics();		

		// Static line painter
		final LinePainter	lp = new LinePainter(0,0,100,100,Color.GREEN,new BasicStroke(1));
		
		lp.paint(g2d);
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,0},image.getData().getPixel((int)0,(int)1,(int[])null));
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)99,(int)99,(int[])null));
		
		try{new LinePainter(0,0,100,100,null,new BasicStroke(1));
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new LinePainter(0,0,100,100,Color.GREEN,null);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
		
		try{lp.paint(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// Dynamic line painter with static getters
		image = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();		
		final DynamicLinePainter	dlp = new DynamicLinePainter(
															OnlineFloatGetter.forValue(0)
															,OnlineFloatGetter.forValue(0)
															,OnlineFloatGetter.forValue(100)
															,OnlineFloatGetter.forValue(100)
															,OnlineObjectGetter.<Color>forValue(Color.GREEN)
															,OnlineObjectGetter.<Stroke>forValue(new BasicStroke(1))
												);
		dlp.paint(g2d);
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,0},image.getData().getPixel((int)0,(int)1,(int[])null));
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)99,(int)99,(int[])null));

		// Dynamic line painter with dynamic getters
		image = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();		
		final DynamicLinePainter	ddlp = new DynamicLinePainter(
															SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
															,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
															,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
															,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
															,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
															,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
												);
		ddlp.paint(g2d);
		Assert.assertArrayEquals(new int[] {255,255,255,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,0},image.getData().getPixel((int)0,(int)1,(int[])null));
		Assert.assertArrayEquals(new int[] {255,255,255,255},image.getData().getPixel((int)99,(int)99,(int[])null));
		
		try{new DynamicLinePainter(null
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicLinePainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,null
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicLinePainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,null
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicLinePainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,null
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicLinePainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,null
						,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicLinePainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
						,null
			);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
 
		try{dlp.paint(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void rectPainterTest() throws ContentException {
		BufferedImage 	image = new BufferedImage(101,101,BufferedImage.TYPE_INT_ARGB);
		Graphics2D 		g2d = image.createGraphics();		

		// Static rect painter
		final RectPainter	rp = new RectPainter(0,0,100,100,Color.GREEN,new BasicStroke(1));
		
		rp.paint(g2d);
		
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,0},image.getData().getPixel((int)1,(int)2,(int[])null));
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)100,(int)100,(int[])null));

		image = new BufferedImage(101,101,BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();		
		final RectPainter	rpf = new RectPainter(0,0,100,100,Color.GREEN,Color.BLACK,new BasicStroke(1));
		
		rpf.paint(g2d);
		
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,255},image.getData().getPixel((int)1,(int)2,(int[])null));
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)100,(int)100,(int[])null));
		
		try{new RectPainter(0,0,100,100,null,new BasicStroke(1));
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new RectPainter(0,0,100,100,Color.GREEN,null);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
		
		try{rp.paint(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// Dynamic rect painter with static getters
		image = new BufferedImage(101,101,BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();		
		final DynamicRectPainter	drp = new DynamicRectPainter(
															OnlineFloatGetter.forValue(0)
															,OnlineFloatGetter.forValue(0)
															,OnlineFloatGetter.forValue(100)
															,OnlineFloatGetter.forValue(100)
															,OnlineObjectGetter.<Color>forValue(Color.GREEN)
															,OnlineObjectGetter.<Stroke>forValue(new BasicStroke(1))
												);
		drp.paint(g2d);
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,0},image.getData().getPixel((int)1,(int)2,(int[])null));
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)100,(int)100,(int[])null));

		image = new BufferedImage(101,101,BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();		
		final DynamicRectPainter	drpf = new DynamicRectPainter(
															OnlineFloatGetter.forValue(0)
															,OnlineFloatGetter.forValue(0)
															,OnlineFloatGetter.forValue(100)
															,OnlineFloatGetter.forValue(100)
															,OnlineObjectGetter.<Color>forValue(Color.GREEN)
															,OnlineObjectGetter.<Color>forValue(Color.RED)
															,OnlineObjectGetter.<Stroke>forValue(new BasicStroke(1))
												);
		drpf.paint(g2d);
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {255,0,0,255},image.getData().getPixel((int)1,(int)2,(int[])null));
		Assert.assertArrayEquals(new int[] {0,255,0,255},image.getData().getPixel((int)100,(int)100,(int[])null));
		
		// Dynamic rect painter with dynamic getters
		image = new BufferedImage(101,101,BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();		
		final DynamicRectPainter	ddrp = new DynamicRectPainter(
															SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
															,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
															,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
															,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
															,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
															,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
												);
		ddrp.paint(g2d);
		Assert.assertArrayEquals(new int[] {255,255,255,255},image.getData().getPixel((int)0,(int)0,(int[])null));
		Assert.assertArrayEquals(new int[] {0,0,0,0},image.getData().getPixel((int)1,(int)2,(int[])null));
		Assert.assertArrayEquals(new int[] {255,255,255,255},image.getData().getPixel((int)100,(int)100,(int[])null));
		
		try{new DynamicRectPainter(null
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicRectPainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,null
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicRectPainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,null
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicRectPainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
							,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
							,null
							,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
							,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicRectPainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,null
						,SVGUtils.buildOnlineObjectGetter(Stroke.class,"${1}",(s)->s,CONV)
			);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new DynamicRectPainter(SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${0}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,"${100}",(s)->s)
						,SVGUtils.buildOnlineObjectGetter(Color.class,"${white}",(s)->s,CONV)
						,null
			);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
 
		try{drp.paint(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
