package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.TestingUtils;
import chav1961.purelib.testing.UITestCategory;
import chav1961.purelib.ui.ColorPair;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class VisualControlsTest implements JComponentMonitor {
	final JFrame		root = new JFrame();
	final JTextField	text = new JTextField();
	final Locale		en = Locale.forLanguageTag("en");
	final Locale		ru = Locale.forLanguageTag("ru");

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
		TestingUtils.err().println("Event="+event);
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
	
	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJButtonWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JButtonWithMeta			butt = new JButtonWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);

		Assert.assertEquals(itemMeta,butt.getNodeMetadata());
		Assert.assertNull(butt.getRawDataFromComponent());
		Assert.assertNull(butt.getValueFromComponent());
		Assert.assertEquals(false,butt.getChangedValueFromComponent());
		Assert.assertEquals(Boolean.class,butt.getValueType());
		Assert.assertNull(butt.standardValidation("test"));
		Assert.assertFalse(butt.isInvalid());
		
		try {new JButtonWithMeta(null, PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JButtonWithMeta(itemMeta, null, this);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JButtonWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJButtonWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JButtonWithMeta			butt = new JButtonWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(butt);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(action);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).click(MouseEvent.BUTTON1,1);
		Assert.assertTrue(focusGained);
		Assert.assertFalse(focusLost);
//		Assert.assertTrue(action);
		
		root.setVisible(false);
		Thread.sleep(100);
		Assert.assertTrue(focusLost);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJCheckBoxWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0];
		final JCheckBoxWithMeta			butt = new JCheckBoxWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);

		Assert.assertEquals(itemMeta,butt.getNodeMetadata());
		Assert.assertEquals("false",butt.getRawDataFromComponent());
		Assert.assertEquals(false,butt.getValueFromComponent());
		Assert.assertEquals(false,butt.getChangedValueFromComponent());
		Assert.assertEquals(Boolean.class,butt.getValueType());
		
		butt.setInvalid(true);
		Assert.assertTrue(butt.isInvalid());
		butt.setInvalid(false);
		Assert.assertFalse(butt.isInvalid());
		
		try {new JCheckBoxWithMeta(null, PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JCheckBoxWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0], PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JCheckBoxWithMeta(itemMeta, null, this);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JCheckBoxWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJCheckBoxWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0];
		final JCheckBoxWithMeta			butt = new JCheckBoxWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(butt);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).click(MouseEvent.BUTTON1,1).select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(butt.isSelected());
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJColorPairPickerWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/pair"))[0];
		final JColorPairPickerWithMeta	picker = new JColorPairPickerWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);
		
		Assert.assertEquals(itemMeta,picker.getNodeMetadata());
		Assert.assertEquals("{#ffffff,#000000}",picker.getRawDataFromComponent());
		Assert.assertEquals(new ColorPair(Color.white,Color.black),picker.getValueFromComponent());
		Assert.assertEquals(new ColorPair(Color.white,Color.black),picker.getChangedValueFromComponent());
		Assert.assertEquals(ColorPair.class,picker.getValueType());
		
		picker.setInvalid(true);
		Assert.assertTrue(picker.isInvalid());
		picker.setInvalid(false);
		Assert.assertFalse(picker.isInvalid());

		try {new JColorPairPickerWithMeta(null, PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JColorPairPickerWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JColorPairPickerWithMeta(itemMeta, null, this);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JColorPairPickerWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
//	@Test
	public void uiJColorPairPickerWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/pair"))[0];
		final JColorPairPickerWithMeta	picker = new JColorPairPickerWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this) {
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
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString());
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()+'/'+JColorPairPickerWithMeta.FOREGROUND_NAME).click(MouseEvent.BUTTON1,1);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()+'/'+JColorPairPickerWithMeta.BACKGROUND_NAME).click(MouseEvent.BUTTON1,1);
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals(new ColorPair(Color.RED,Color.GREEN),picker.getValueFromComponent());
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJColorPickerWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/color"))[0];
		final JColorPickerWithMeta		picker = new JColorPickerWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);
		
		Assert.assertEquals(itemMeta,picker.getNodeMetadata());
		Assert.assertEquals("#000000",picker.getRawDataFromComponent());
		Assert.assertEquals(Color.black,picker.getValueFromComponent());
		Assert.assertEquals(Color.black,picker.getChangedValueFromComponent());
		Assert.assertEquals(Color.class,picker.getValueType()); 
		
		picker.setInvalid(true);
		Assert.assertTrue(picker.isInvalid());
		picker.setInvalid(false);
		Assert.assertFalse(picker.isInvalid());

		try {new JColorPickerWithMeta(null, PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JColorPickerWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JColorPickerWithMeta(itemMeta, null, this);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JColorPickerWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
//	@Test
	public void uiJColorPickerWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/color"))[0];
		final JColorPickerWithMeta		picker = new JColorPickerWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this) {
											private static final long serialVersionUID = 1L;
											
											@Override
											protected Color chooseColor(final Localizer localizer, final Color initialColor, final boolean isForeground) throws HeadlessException, LocalizationException {
												return Color.RED;
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
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString());
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()+'/'+JColorPickerWithMeta.COLOR_NAME).click(MouseEvent.BUTTON1,1);
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals(Color.RED,picker.getValueFromComponent());
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJDateFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final Date						nullDate = new Date(0);
		final String					nullFormatted = DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.getDefault()).format(nullDate);
		final JDateFieldWithMeta		date = new JDateFieldWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);
		
		Assert.assertEquals(itemMeta,date.getNodeMetadata());
		Assert.assertEquals(nullDate.toString(),date.getRawDataFromComponent());
		Assert.assertEquals(nullDate,date.getValueFromComponent());
		Assert.assertEquals(nullDate,date.getChangedValueFromComponent());
		Assert.assertEquals(Date.class,date.getValueType());
		
		date.setInvalid(true);
		Assert.assertTrue(date.isInvalid());
		date.setInvalid(false);
		Assert.assertFalse(date.isInvalid());

		try {new JDateFieldWithMeta(null, PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JDateFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JDateFieldWithMeta(itemMeta, null, this);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JDateFieldWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Category(UITestCategory.class)
	@Test
	public void uiJDateFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JDateFieldWithMeta		date = new JDateFieldWithMeta(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(date);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString());
		sut.keys(SwingUtils.KS_DROPDOWN);
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
//		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		
		root.setVisible(false);
	}


	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJEnumFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/contMode"))[0];
		final JEnumFieldWithMeta		date = new JEnumFieldWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,date.getNodeMetadata());
		Assert.assertEquals("CONTINUE",date.getRawDataFromComponent());
		Assert.assertEquals(ContinueMode.CONTINUE,date.getValueFromComponent());
		Assert.assertEquals(ContinueMode.CONTINUE,date.getChangedValueFromComponent());
		Assert.assertEquals(ContinueMode.class,date.getValueType());
		
		date.setInvalid(true);
		Assert.assertTrue(date.isInvalid());
		date.setInvalid(false);
		Assert.assertFalse(date.isInvalid());

		try {new JEnumFieldWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JEnumFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JEnumFieldWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJEnumFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/contMode"))[0];
		final JEnumFieldWithMeta		enumField = new JEnumFieldWithMeta(itemMeta, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(enumField);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString());
		sut.keys(SwingUtils.KS_DROPDOWN,KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0,false),SwingUtils.KS_ACCEPT);
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals(ContinueMode.SIBLINGS_ONLY,enumField.getValueFromComponent());
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJFileFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/file"))[0];
		final JFileFieldWithMeta		file = new JFileFieldWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,file.getNodeMetadata());
		Assert.assertEquals(null,file.getRawDataFromComponent());
		Assert.assertEquals(null,file.getValueFromComponent());
//		Assert.assertEquals("",file.getChangedValueFromComponent());
		Assert.assertEquals(File.class,file.getValueType());
		
		file.setInvalid(true);
		Assert.assertTrue(file.isInvalid());
		file.setInvalid(false);
		Assert.assertFalse(file.isInvalid());

		
		try {new JFileFieldWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JFileFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JFileFieldWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJFileFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/file"))[0];
		final File						selectedFile = new File("test");
		final JFileFieldWithMeta		file = new JFileFieldWithMeta(itemMeta, this) {
											private static final long serialVersionUID = 1134427067018687415L;
											@Override
											protected File chooseFile(Localizer localizer, File initialFile) throws HeadlessException, LocalizationException {
												return selectedFile;
											}
											};
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(file);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString());
		sut.keys(SwingUtils.KS_DROPDOWN);
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJFormattedTextFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface		metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata			itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/text"))[0];
		final JFormattedTextFieldWithMeta	text = new JFormattedTextFieldWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,text.getNodeMetadata());
		Assert.assertEquals(null,text.getRawDataFromComponent());
		Assert.assertEquals(null,text.getValueFromComponent());
		Assert.assertEquals(null,text.getChangedValueFromComponent());
		Assert.assertEquals(String.class,text.getValueType());
		
		text.setInvalid(true);
		Assert.assertTrue(text.isInvalid());
		text.setInvalid(false);
		Assert.assertFalse(text.isInvalid());

		try {new JFormattedTextFieldWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JFormattedTextFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JFormattedTextFieldWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJFormattedTextFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface		metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata			itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/text"))[0];
		final JFormattedTextFieldWithMeta	text = new JFormattedTextFieldWithMeta(itemMeta, this);
		final SwingUnitTest					sut = new SwingUnitTest(root);
		
		root.getContentPane().add(text);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).keys("VALUE\n");
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Thread.sleep(100);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals("VALUE",text.getValue());
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJIntegerFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/intValue"))[0];
		final JIntegerFieldWithMeta		text = new JIntegerFieldWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,text.getNodeMetadata());
		Assert.assertEquals(null,text.getRawDataFromComponent());
		Assert.assertEquals(null,text.getValueFromComponent());
		Assert.assertEquals(null,text.getChangedValueFromComponent());
		Assert.assertEquals(int.class,text.getValueType());
		
		text.setInvalid(true);
		Assert.assertTrue(text.isInvalid());
		text.setInvalid(false);
		Assert.assertFalse(text.isInvalid());

		Assert.assertNotNull(text.standardValidation("number:ilegal"));
		Assert.assertNull(text.standardValidation("12345"));
		
		try {new JIntegerFieldWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JIntegerFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JIntegerFieldWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJIntegerFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/intValue"))[0];
		final JIntegerFieldWithMeta		text = new JIntegerFieldWithMeta(itemMeta, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);
		
		root.getContentPane().add(text);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).keys("12345\n");
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Thread.sleep(100);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals(12345L,text.getValue());
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJNumericFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/doubleValue"))[0];
		final JNumericFieldWithMeta		text = new JNumericFieldWithMeta(itemMeta, this);

		text.localeChanged(en,ru);
		
		Assert.assertEquals(itemMeta,text.getNodeMetadata());
		Assert.assertEquals(null,text.getRawDataFromComponent());
		Assert.assertEquals(null,text.getValueFromComponent());
		Assert.assertEquals(null,text.getChangedValueFromComponent());
		Assert.assertEquals(double.class,text.getValueType());
		
		text.setInvalid(true);
		Assert.assertTrue(text.isInvalid());
		text.setInvalid(false);
		Assert.assertFalse(text.isInvalid());
		
		Assert.assertNotNull(text.standardValidation("number:ilegal"));
		Assert.assertNull(text.standardValidation("123,456"));
		
		try {new JNumericFieldWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JNumericFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JNumericFieldWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Category(UITestCategory.class)
	@Test
	public void uiJNumericFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/doubleValue"))[0];
		final JNumericFieldWithMeta		text = new JNumericFieldWithMeta(itemMeta, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);

		text.localeChanged(en,ru);
		
		root.getContentPane().add(text);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).keys("123,456\n");
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Thread.sleep(100);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals(123.456,text.getValue());
		
		Assert.assertEquals("123,456",text.getText());
		text.localeChanged(ru,en);
		Assert.assertEquals("123.456",text.getText());
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJTextFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/text"))[0];
		final JTextFieldWithMeta		text = new JTextFieldWithMeta(itemMeta, this);

		Assert.assertEquals(itemMeta,text.getNodeMetadata());
		Assert.assertEquals(null,text.getRawDataFromComponent());
		Assert.assertEquals(null,text.getValueFromComponent());
		Assert.assertEquals("",text.getChangedValueFromComponent());
		Assert.assertEquals(String.class,text.getValueType());
		
		text.setInvalid(true);
		Assert.assertTrue(text.isInvalid());
		text.setInvalid(false);
		Assert.assertFalse(text.isInvalid());
		
		try {new JTextFieldWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JTextFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JTextFieldWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJTextFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/text"))[0];
		final JTextFieldWithMeta		text = new JTextFieldWithMeta(itemMeta, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);

		text.localeChanged(en,ru);
		
		root.getContentPane().add(text);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).keys("test\n");
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals("test",text.getText());
		
		root.setVisible(false);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void basicJDateSelectionDialogTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JDateSelectionDialog		dsd = new JDateSelectionDialog(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);

		Assert.assertEquals(itemMeta,dsd.getNodeMetadata());
		Assert.assertNotNull(dsd.getRawDataFromComponent());
		Assert.assertNull(dsd.getValueFromComponent());
		Assert.assertNotNull(dsd.getChangedValueFromComponent());
		Assert.assertEquals(Date.class,dsd.getValueType());
		Assert.assertNotNull(dsd.standardValidation("2002"));
		
		dsd.setInvalid(true);
		Assert.assertTrue(dsd.isInvalid());
		dsd.setInvalid(false);
		Assert.assertFalse(dsd.isInvalid());
		
		try {new JDateSelectionDialog(null, PureLibSettings.PURELIB_LOCALIZER, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JDateSelectionDialog(itemMeta, null, this);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new JDateSelectionDialog(itemMeta, PureLibSettings.PURELIB_LOCALIZER, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJDateSelectionDialogTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JDateSelectionDialog		dsd = new JDateSelectionDialog(itemMeta, PureLibSettings.PURELIB_LOCALIZER, this);
		final SwingUnitTest				sut = new SwingUnitTest(root);

		dsd.localeChanged(en,ru);
		
		root.getContentPane().add(dsd);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(focusGained);
		Assert.assertFalse(focusLost);
		Assert.assertFalse(validation);
		Assert.assertFalse(saving);
		Assert.assertTrue(loading);
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).keys("\t\t\t");
		sut.select("TEXT"); // Change value and remove focus from control
		Assert.assertTrue(focusGained);
		Assert.assertTrue(validation);
		Assert.assertTrue(saving);
		Assert.assertTrue(focusLost);
		Assert.assertEquals(Date.class,dsd.getValueFromComponent().getClass());
		
		root.setVisible(false);
	}

}
