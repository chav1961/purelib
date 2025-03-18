package chav1961.purelib.ui.swing;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.UITestCategory;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;


public class MapMenuWithMetaTest {
	final JFrame		frame = new JFrame();

	volatile boolean	focusGained = false, focusLost = false, action = false;
	
	@BeforeEach
	public void prepare() {
		frame.getContentPane().setLayout(new GridLayout(1,1));
		frame.setLocationRelativeTo(null);
	}

	@AfterEach
	public void unprepare() {
		frame.dispose();
	}
	
	@Tag("OrdinalTestCategory")
	@Test
	public void basicTest() throws EnvironmentException, ContentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("Application.xml"));
		final ContentNodeMetadata		root = mdi.byUIPath(URI.create("ui:/model/navigation.top.mapMenuTest"));
		final Properties				props = Utils.mkProps("size","453x356",
															"area1","M 8 5 L 191 5 L 191 140 L 8 140 Z",
															"area2","M 224 5 L 438 5 L 438 140 L 224 140 Z",
															"area3","M 213 167 L 304 210 L 304 294 L 213 337 L 115 294 L 115 210 Z");
		final JComponentMonitor			mon = new JComponentMonitor() {
											@Override
											public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
												return true;
											}
										};
		final JMapMenuWithMeta			mmm = new JMapMenuWithMeta(root, mon, props);
		
		Assert.assertEquals(root, mmm.getNodeMetadata());
		Assert.assertEquals(mdi.byUIPath(URI.create("ui:/model/navigation.top.mapMenuTest/area1")), mmm.getNodeMetadata(20,20));
		Assert.assertNull(mmm.getNodeMetadata(0,0));
		
		Assert.assertEquals("null", mmm.getRawDataFromComponent());
		Assert.assertNull(mmm.getValueFromComponent());
		Assert.assertNull(mmm.getChangedValueFromComponent());
		Assert.assertEquals(Object.class, mmm.getValueType());
		
		mmm.assignValueToComponent("test");
		
		Assert.assertEquals("test",mmm.getRawDataFromComponent());
		Assert.assertEquals("test",mmm.getValueFromComponent());
		Assert.assertEquals(String.class, mmm.getValueType());

		Assert.assertNull(mmm.standardValidation("test"));

		Assert.assertFalse(mmm.isInvalid());
		mmm.setInvalid(true);
		Assert.assertTrue(mmm.isInvalid());
		
		try {new JMapMenuWithMeta(null, mon, props);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JMapMenuWithMeta(root, null, props);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JMapMenuWithMeta(root, mon, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try {new JMapMenuWithMeta(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), mon, props);
			Assert.fail("Mandatory exception was not detected (icon is missing in root metadata)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JMapMenuWithMeta(root, mon, Utils.mkProps("size","unknown"));
			Assert.fail("Mandatory exception was not detected (illegal number in size)");
		} catch (SyntaxException exc) {
		}
		try {new JMapMenuWithMeta(root, mon, Utils.mkProps("size","20 20"));
			Assert.fail("Mandatory exception was not detected (missing 'x')");
		} catch (SyntaxException exc) {
		}
		try {new JMapMenuWithMeta(root, mon, Utils.mkProps("size","20 x unknown"));
			Assert.fail("Mandatory exception was not detected (illegal number in size)");
		} catch (SyntaxException exc) {
		}
		try {new JMapMenuWithMeta(root, mon, Utils.mkProps("size","20 x 20 pix"));
			Assert.fail("Mandatory exception was not detected (unparsed tail)");
		} catch (SyntaxException exc) {
		}
		try {new JMapMenuWithMeta(root, mon, Utils.mkProps("size","20 x 20"));
			Assert.fail("Mandatory exception was not detected (unknown name in the properties)");
		} catch (SyntaxException exc) {
		}
	
	}

	@Tag("UITestCategory")
	@Test
	public void uiTest() throws EnvironmentException, ContentException, InterruptedException, DebuggingException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("Application.xml"));
		final ContentNodeMetadata		root = mdi.byUIPath(URI.create("ui:/model/navigation.top.mapMenuTest"));
		final Properties				props = Utils.mkProps("size","453x356",
															"area1","M 8 5 L 191 5 L 191 140 L 8 140 Z",
															"area2","M 224 5 L 438 5 L 438 140 L 224 140 Z",
															"area3","M 213 167 L 304 210 L 304 294 L 213 337 L 115 294 L 115 210 Z");
		final JComponentMonitor			mon = new JComponentMonitor() {
											@Override
											public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, Object... parameters) throws ContentException {
												Assert.assertNotNull(metadata);
												switch (event) {
													case Action			:
														action = true;
														break;
													case FocusGained	:
														focusGained = true;
														Assert.assertEquals(1,parameters.length);
														break;
													case FocusLost		:
														focusLost = true;
														Assert.assertEquals(1,parameters.length);
														break;
													default :
														return false;
												}
												return true;
											}
										};
		final JMapMenuWithMeta			mmm = new JMapMenuWithMeta(root, mon, props);
		final SwingUnitTest				sut = new SwingUnitTest(frame);
		
		frame.setSize(new Dimension(640,480));
		frame.getContentPane().add(mmm);
		frame.setVisible(true);
		SwingTestingUtils.syncRequestFocus(frame);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(action);
		
		sut.use(mmm);
		Thread.sleep(500);
		
		sut.move(20, 20).await();
		Assert.assertTrue(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(action);
		
		Thread.sleep(500);
		
		sut.move(320, 240).await();
		Assert.assertTrue(focusGained);
		Assert.assertTrue(focusLost);
		Assert.assertFalse(action);

		Thread.sleep(500);
		
		sut.click(320,240,MouseEvent.BUTTON1, 2).await();
		Assert.assertTrue(focusGained);
		Assert.assertTrue(focusLost);
		Assert.assertTrue(action);

		Thread.sleep(1000);
		
		frame.setVisible(false);
	}
}
