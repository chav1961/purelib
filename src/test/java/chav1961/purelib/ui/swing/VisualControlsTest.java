package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.UITestCategory;
import chav1961.purelib.ui.ColorPair;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class VisualControlsTest implements JComponentMonitor {
	final JFrame		root = new JFrame();
	final JTextField	text = new JTextField();

	volatile boolean	focusGained = false, focusLost = false, action = false, loading = false, validation = false, saving = false;
	
	@Before
	public void prepare() {
		root.getContentPane().setLayout(new GridLayout(2,1));
		text.setName("TEXT");
		root.getContentPane().add(text);
		SwingUtils.centerMainWindow(root,0.1f);
	}

	@After
	public void unprepare() {
		root.dispose();
	}
	

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
		System.err.println("Event="+event);
		switch (event) {
			case Action			:
				action = true;
				break;
			case Exit			:
				break;
			case FocusGained	:
				focusGained = true;
				break;
			case FocusLost		:
				focusLost = true;
				break;
			case Loading		:
				loading = true;
				break;
			case Rollback		:
				break;
			case Saving			:
				saving = true;
				break;
			case Validation		:
				validation = true;
				break;
			default: throw new UnsupportedOperationException("Action ["+event+"] is not supported yet"); 
		}
		return true;
	} 
	
	@Test
	public void basicJButtonWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JButtonWithMeta			butt = new JButtonWithMeta(itemMeta, this);

		Assert.assertEquals(itemMeta,butt.getNodeMetadata());
		Assert.assertNull(butt.getRawDataFromComponent());
		Assert.assertNull(butt.getValueFromComponent());
		Assert.assertEquals(false,butt.getChangedValueFromComponent());
		Assert.assertEquals(Boolean.class,butt.getValueType());
		Assert.assertNull(butt.standardValidation("test"));
		Assert.assertFalse(butt.isInvalid());
		
		try {new JButtonWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JButtonWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJButtonWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JButtonWithMeta			butt = new JButtonWithMeta(itemMeta, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(butt);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(action);
		sut.select(itemMeta.getApplicationPath().toString()).click(MouseEvent.BUTTON1,1);
		Assert.assertTrue(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertTrue(action);
		
		root.setVisible(false);
		Thread.sleep(100);
		Assert.assertTrue(focusLost);
	}

	@Test
	public void basicJCheckBoxWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JCheckBoxWithMeta			butt = new JCheckBoxWithMeta(itemMeta, this);

		Assert.assertEquals(itemMeta,butt.getNodeMetadata());
		Assert.assertEquals("false",butt.getRawDataFromComponent());
		Assert.assertEquals(false,butt.getValueFromComponent());
		Assert.assertEquals(false,butt.getChangedValueFromComponent());
		Assert.assertEquals(Boolean.class,butt.getValueType());
		Assert.assertNull(butt.standardValidation("true"));
		Assert.assertNotNull(butt.standardValidation("invalid"));
		
		butt.setInvalid(true);
		Assert.assertTrue(butt.isInvalid());
		butt.setInvalid(false);
		Assert.assertFalse(butt.isInvalid());
		
		try {new JCheckBoxWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JCheckBoxWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJCheckBoxWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0];
		final JCheckBoxWithMeta			butt = new JCheckBoxWithMeta(itemMeta, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(butt);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(itemMeta.getApplicationPath().toString()).click(MouseEvent.BUTTON1,1).select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(butt.isSelected());
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		
		root.setVisible(false);
	}

	@Test
	public void basicJColorPairPickerWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/pair"))[0];
		final JColorPairPickerWithMeta	picker = new JColorPairPickerWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,picker.getNodeMetadata());
		Assert.assertEquals("{#ffffff,#000000}",picker.getRawDataFromComponent());
		Assert.assertEquals(new ColorPair(Color.white,Color.black),picker.getValueFromComponent());
		Assert.assertEquals(new ColorPair(Color.white,Color.black),picker.getChangedValueFromComponent());
		Assert.assertEquals(ColorPair.class,picker.getValueType());
		
		picker.setInvalid(true);
		Assert.assertTrue(picker.isInvalid());
		picker.setInvalid(false);
		Assert.assertFalse(picker.isInvalid());

		Assert.assertNotNull(picker.standardValidation("illegal"));
		Assert.assertNotNull(picker.standardValidation("a,b,c"));
		Assert.assertNotNull(picker.standardValidation("unknown"));
		Assert.assertNotNull(picker.standardValidation("black,unknown"));
		Assert.assertNull(picker.standardValidation(" black , white "));
		
		try {new JColorPairPickerWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JColorPairPickerWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJColorPairPickerWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0];
		final JColorPairPickerWithMeta	picker = new JColorPairPickerWithMeta(itemMeta, this) {
											private static final long serialVersionUID = 1L;
											
											@Override
											protected Color chooseColor(final Localizer localizer, final Color initialColor, final boolean isForeground) throws HeadlessException, LocalizationException {
												return isForeground ? Color.RED : Color.GREEN;
											}
										};
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(picker);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(itemMeta.getApplicationPath().toString());
		sut.select(itemMeta.getApplicationPath().toString()+'/'+JColorPairPickerWithMeta.FOREGROUND_NAME).click(MouseEvent.BUTTON1,1);
		sut.select(itemMeta.getApplicationPath().toString()+'/'+JColorPairPickerWithMeta.BACKGROUND_NAME).click(MouseEvent.BUTTON1,1);
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		
		root.setVisible(false);
	}
	
}