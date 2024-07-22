package chav1961.purelib.ui.swing.useful;


import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.testing.SwingUnitTest;

public class JRangeSliderTest {
	@Tag("UITestCategory")
	@Test
	public void uiTest() throws DebuggingException, InterruptedException {
		final JFrame		frame = new JFrame();
		final JRangeSlider	slider = new JRangeSlider(0,100);
		final SwingUnitTest	sut = new SwingUnitTest(frame);
		
		slider.setName("slider");
		frame.getContentPane().add(slider);
		frame.setSize(200,55);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		final Dimension	size = slider.getSize();

		sut.use(slider);
		slider.setRangeValue(50, 50);
		sut.drag(MouseEvent.BUTTON1, size.width/2, size.height/2, size.width/4, size.height/2);
		sut.drag(MouseEvent.BUTTON1, size.width/2, size.height/2, 3*size.width/4, size.height/2);
		Assert.assertEquals(24, slider.getLowerValue());
		Assert.assertEquals(76, slider.getUpperValue());
		frame.setVisible(false);
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicTest() throws DebuggingException, InterruptedException {
		final JRangeSlider	slider = new JRangeSlider();
		
		slider.setLowerValue(0);
		slider.setUpperValue(100);
		Assert.assertEquals(0, slider.getLowerValue());
		Assert.assertEquals(100, slider.getUpperValue());
		
		slider.setRangeValue(25,75);
		Assert.assertEquals(25, slider.getLowerValue());
		Assert.assertEquals(75, slider.getUpperValue());
		Assert.assertEquals((25L << 32) | 75L, slider.getRangeValueAsLong());

		slider.setRangeValueAsLong((40L << 32) | 60);
		Assert.assertEquals(40, slider.getLowerValue());
		Assert.assertEquals(60, slider.getUpperValue());
		Assert.assertEquals((40L << 32) | 60L, slider.getRangeValueAsLong());
		
		try{new JRangeSlider(100, 0);
			Assert.fail("Mandatory exception was not detected (lower range is greater than high ranle)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
