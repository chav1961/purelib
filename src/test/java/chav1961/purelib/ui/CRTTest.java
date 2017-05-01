package chav1961.purelib.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;

public class CRTTest {

	@Test
	public void attributesTest() {
		final CRT			window = new CRT();
		
		Assert.assertEquals(window.getScreenWidth(),window.DEFAULT_WIDTH);
		Assert.assertEquals(window.getScreenHeight(),window.DEFAULT_HEIGHT);
		
		window.clear(Color.WHITE,Color.BLACK);
		Assert.assertArrayEquals(window.readAttribute(new Point(1,1)),new Color[]{Color.WHITE,Color.BLACK});
		Assert.assertArrayEquals(window.readAttribute(new Point(window.getScreenWidth(),window.getScreenHeight())),new Color[]{Color.WHITE,Color.BLACK});
		
		final Color[][][]	attr = window.readAttribute(new Rectangle(1,1,window.DEFAULT_WIDTH,window.DEFAULT_HEIGHT));
		
		Assert.assertEquals(attr[0][0][0],Color.WHITE);
		Assert.assertEquals(attr[window.DEFAULT_WIDTH-1][window.DEFAULT_HEIGHT-1][1],Color.BLACK);
		
		try{window.clear(null,Color.BLACK);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.clear(Color.WHITE,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{window.writeAttribute((Point)null,new Color[]{Color.WHITE,Color.BLACK});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.writeAttribute(new Point(1,1),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.writeAttribute(new Point(1,1),new Color[0]);
			Assert.fail("Mandatory exception was not detected (illegal 2-nd argument size)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.writeAttribute(new Point(1,1),new Color[]{Color.BLACK,null});
			Assert.fail("Mandatory exception was not detected (null inside 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{window.writeAttribute((Rectangle)null,new Color[]{Color.WHITE,Color.BLACK});
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.writeAttribute(new Rectangle(1,1,1,1),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.writeAttribute(new Rectangle(1,1,1,1),new Color[0]);
			Assert.fail("Mandatory exception was not detected (illegal 2-nd argument size)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.writeAttribute(new Rectangle(1,1,1,1),new Color[]{Color.BLACK,null});
			Assert.fail("Mandatory exception was not detected (null inside 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{window.readAttribute(0,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.readAttribute(1,window.DEFAULT_HEIGHT+1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{window.readAttribute((Point)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.readAttribute((Rectangle)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void contentTest() {
		final CRT		window = new CRT();
		final char[]	content = new char[window.DEFAULT_WIDTH*window.DEFAULT_HEIGHT];
		
		for (int x = 0; x < window.DEFAULT_WIDTH; x++) {
			for (int y = 0; y < window.DEFAULT_HEIGHT; y++) {
				content[y*window.DEFAULT_WIDTH+x] = (char)('A'+y);
			}
		}
		
		window.writeContent(new Rectangle(1,1,window.DEFAULT_WIDTH,window.DEFAULT_HEIGHT),content);
		Assert.assertEquals(window.readContent(1,1),'A');
		Assert.assertEquals(window.readContent(window.DEFAULT_WIDTH,window.DEFAULT_HEIGHT),'Y');
		Assert.assertArrayEquals(window.readContent(new Rectangle(1,1,window.DEFAULT_WIDTH,window.DEFAULT_HEIGHT)),content);

		try{window.readContent(0,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{window.readContent(1,window.DEFAULT_HEIGHT+1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{window.readContent((Point)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{window.readContent((Rectangle)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

//	@Test
	public void liveTest() throws InterruptedException {
		final JFrame	frame = new JFrame();
		final CRT		window = new CRT();
		
		frame.getContentPane().add(window,BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(800,600));
		frame.pack();
		frame.setVisible(true);
		
		Thread.sleep(500);	frame.setTitle("Clear WHITE/BLACK");
		window.clear(Color.WHITE,Color.BLACK);
		Thread.sleep(500);	frame.setTitle("Clear WHITE/YELLOW");
		window.clear(Color.WHITE,Color.YELLOW);
		Thread.sleep(500);	frame.setTitle("Clear RED/BLACK");
		window.clear(Color.RED,Color.BLACK);
		Thread.sleep(500);	frame.setTitle("Hello World");
		window.writeContent(new Rectangle(1,1,80,25),"Hello world!");
		Thread.sleep(2000);	frame.setTitle("Dispose");
		frame.dispose();
	}
}
