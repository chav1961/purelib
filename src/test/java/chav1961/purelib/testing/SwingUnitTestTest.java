package chav1961.purelib.testing;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.ui.swing.SwingUtils;

@Tag("OrdinalTestCategory")
public class SwingUnitTestTest {
	final JFrame		root = new JFrame();
	final JTextField	field1 = new JTextField(), field2 = new JTextField();
	final JLabel		label = new JLabel("test");

	@Before
	public void prepare() {
		root.getContentPane().setLayout(new GridLayout(3,1));
		field1.setName("field1");
		field2.setName("field2");
		label.setName("label");
		label.setFocusable(false);
		root.getContentPane().add(field1);
		root.getContentPane().add(field2);
		root.getContentPane().add(label);
		SwingUtils.centerMainWindow(root,0.1f);
		root.setVisible(true);
	}

	@After
	public void unprepare() {
		root.setVisible(false);
		root.dispose();
	}
	
	@Tag("UITestCategory")
	@Test
	public void uiBasicTest() throws EnvironmentException, DebuggingException, InterruptedException {
		final SwingUnitTest	sut = new SwingUnitTest(root);
		
		root.requestFocus();
		
		Assert.assertEquals(sut,sut.seek("field1"));
		Assert.assertEquals(field1,sut.getLastFound());
		
		Assert.assertEquals(sut,sut.use(field2));
		Assert.assertEquals(field2,sut.getLastFound());
		
		Assert.assertEquals(sut,sut.select("field1").keys("test\tnewTest"));
		Assert.assertEquals("test",field1.getText());
		Assert.assertFalse(field1.hasFocus());
		Assert.assertEquals("newTest",field2.getText());
		Assert.assertTrue(field2.hasFocus());
		
		try{new SwingUnitTest(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{sut.seek(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{sut.seek("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{sut.seek("unknown");
			Assert.fail("Mandatory exception was not detected (unknown control name)");
		} catch (DebuggingException exc) {
		}
		
		try{sut.use(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{sut.select(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{sut.select("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{sut.select("unknown");
			Assert.fail("Mandatory exception was not detected (unknown control name)");
		} catch (DebuggingException exc) {
		}
		try{sut.select("label");
			Assert.fail("Mandatory exception was not detected (control found is not focusable)");
		} catch (DebuggingException exc) {
		}

		try{sut.keys((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{sut.keys("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{sut.keys((KeyStroke[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{sut.keys();
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{sut.keys(SwingUtils.KS_ACCEPT,null);
			Assert.fail("Mandatory exception was not detected (nulls inside 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Tag("UITestCategory")
	@Test
	public void uiMouseTest() throws EnvironmentException, DebuggingException, InterruptedException {
		final SwingUnitTest	sut = new SwingUnitTest(root);
		
		field1.setText("test");
		root.requestFocus();
		
		Assert.assertNull(field1.getSelectedText());
		Assert.assertNull(field2.getSelectedText());
		
		Assert.assertEquals(sut,sut.seek("field1").click(MouseEvent.BUTTON1,2));
		Assert.assertEquals(field1,sut.getLastFound());
		Assert.assertEquals("test",field1.getSelectedText());
		Assert.assertNull(field2.getSelectedText());

		final Graphics 		g = field1.getGraphics();
		final FontMetrics 	fm = g.getFontMetrics(); 
		final int 			x = fm.stringWidth("te"); 
		
		field1.setCaretPosition(0);
		Assert.assertEquals(sut,sut.drag(MouseEvent.BUTTON1,0,10,x+1,10));
		Assert.assertEquals("te",field1.getSelectedText());

		field1.setCaretPosition(0);
		Assert.assertEquals(sut,sut.drag(MouseEvent.BUTTON1,0,10,"field1",x+1,10));
		Assert.assertEquals("te",field1.getSelectedText());
	}
}
