package chav1961.purelib.ui.swing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.DummyLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;


public class SwingUtilsTest {
	@Test
	public void walkingTest() {
		final StringBuilder	sb = new StringBuilder();
		final JPanel		root = new JPanel();
		final JPanel		sub1 = new JPanel(), sub2 = new JPanel();
		final JPanel		sub11 = new JPanel(), sub12 = new JPanel(), sub21 = new JPanel(), sub22 = new JPanel();
	
		root.setName("root");
		sub1.setName("sub1");	sub2.setName("sub2");
		sub11.setName("sub11");	sub12.setName("sub12");	sub21.setName("sub21");	sub22.setName("sub22");
		sub1.add(sub11);		sub1.add(sub12);		sub2.add(sub21);		sub2.add(sub22);
		root.add(sub1);			root.add(sub2);
		
		sb.setLength(0);
		SwingUtils.walkDown(root,(mode,component)->{
			switch (mode) {
				case ENTER	:
					sb.append(component.getName()).append('{');
					break;
				case EXIT	:
					sb.append('}');
					break;
			}
			return	ContinueMode.CONTINUE;
		});
		Assert.assertEquals("root{sub1{sub11{}sub12{}}sub2{sub21{}sub22{}}}",sb.toString());
		
		Assert.assertEquals(sub22,SwingUtils.findComponentByName(root,"sub22"));
		Assert.assertNull(SwingUtils.findComponentByName(root,"unknown"));

		final JMenu			rootMenu = new JMenu("root");
		final JMenu			sub1Menu = new JMenu("sub1"), sub2Menu = new JMenu("sub2");
		final JMenuItem		sub11Menu = new JMenuItem("sub11"), sub12Menu = new JMenuItem("sub12"), sub21Menu = new JMenuItem("sub21"), sub22Menu = new JMenuItem("sub22");

		sub1Menu.add(sub11Menu);	sub1Menu.add(sub12Menu);	sub2Menu.add(sub21Menu);	sub2Menu.add(sub22Menu);
		rootMenu.add(sub1Menu);		rootMenu.add(sub2Menu);
		
		sb.setLength(0);
		SwingUtils.walkDown(rootMenu,(mode,component)->{
			switch (mode) {
				case ENTER	:
					sb.append(((JMenuItem)component).getText()).append('{');
					break;
				case EXIT	:
					sb.append('}');
					break;
			}
			return	ContinueMode.CONTINUE;
		});
		Assert.assertEquals("root{sub1{sub11{}sub12{}}sub2{sub21{}sub22{}}}",sb.toString());
		
		try{SwingUtils.walkDown(null,(mode,component)->{return ContinueMode.CONTINUE;});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{SwingUtils.walkDown(rootMenu,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}

		try{SwingUtils.findComponentByName(null,"name");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{SwingUtils.findComponentByName(root,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{SwingUtils.findComponentByName(root,"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
	}
	
	@Test
	public void prepareRendererTest() throws LocalizationException {
		final JComponentMonitor 		monitor = new JComponentMonitor(){
											@Override
											public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponent component, Object... parameters) throws ContentException {
												return true;
											}
										};

		for (Class<?> item : new Class<?>[]{boolean.class,String.class,int.class,double.class,Date.class,ContinueMode.class,File.class,URI.class}) {
			final FieldFormat			format = new FieldFormat(item,"");
			final ContentNodeMetadata	meta = new MutableContentNodeMetadata("name",item,"test",URI.create(Localizer.LOCALIZER_SCHEME+":prop:./chav1961/purelib/i18n/localization"),"testSet1","testSet2","testSet3",format,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"));
			final JComponent			component = SwingUtils.prepareRenderer(meta, format, monitor);
			
			Assert.assertNotNull(component);
			Assert.assertTrue(component instanceof NodeMetadataOwner);
			Assert.assertTrue(component instanceof JComponentInterface);
		}
	}

	@Test
	public void windowPropertiesTest() throws IOException, URISyntaxException, InterruptedException {
		final Dimension	screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final JDialog	dlg = new JDialog();
		
		SwingUtils.centerMainWindow(dlg);
		Assert.assertTrue(dlg.getWidth() >= screenSize.getWidth()/2);
		Assert.assertTrue(dlg.getHeight() >= screenSize.getHeight()/2);
		Assert.assertTrue(dlg.getX() <= screenSize.getWidth()/4);
		Assert.assertTrue(dlg.getY() <= screenSize.getHeight()/4);
		
		try{SwingUtils.centerMainWindow((JDialog)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.centerMainWindow(dlg,2.0f);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		final JFrame	frame = new JFrame();
		
		SwingUtils.centerMainWindow(frame);
		Assert.assertTrue(frame.getWidth() >= 3*screenSize.getWidth()/4);
		Assert.assertTrue(frame.getHeight() >= 3*screenSize.getHeight()/4);
		Assert.assertTrue(frame.getX() <= screenSize.getWidth()/8);
		Assert.assertTrue(frame.getY() <= screenSize.getHeight()/8);
		
		try{SwingUtils.centerMainWindow((JFrame)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.centerMainWindow(frame,2.0f);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	
//	@Test
	public void showCreoleHelpWindowTest() throws IOException, URISyntaxException, InterruptedException {
		final JDialog	dlg = new JDialog();
		
		SwingUtils.showCreoleHelpWindow(dlg,SwingUtilsTest.class.getResource("helpcontent.cre").toURI());
		Thread.sleep(5000);
	}
}
