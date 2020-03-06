package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.text.DateFormat;
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
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).click(MouseEvent.BUTTON1,1);
		Assert.assertTrue(focusGained);
		Assert.assertFalse(focusLost);
//		Assert.assertTrue(action);
		
		root.setVisible(false);
		Thread.sleep(100);
		Assert.assertTrue(focusLost);
	}

	@Test
	public void basicJCheckBoxWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0];
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
		try {new JCheckBoxWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
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
		sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).click(MouseEvent.BUTTON1,1).select("TEXT"); // Change value and remove focus from control
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
		Assert.assertNull(picker.standardValidation("{ black , white }"));
		
		try {new JColorPairPickerWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JColorPairPickerWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
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
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/pair"))[0];
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

	@Test
	public void basicJColorPickerWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/color"))[0];
		final JColorPickerWithMeta		picker = new JColorPickerWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,picker.getNodeMetadata());
		Assert.assertEquals("#000000",picker.getRawDataFromComponent());
		Assert.assertEquals(Color.black,picker.getValueFromComponent());
		Assert.assertEquals(Color.black,picker.getChangedValueFromComponent());
		Assert.assertEquals(Color.class,picker.getValueType());
		
		picker.setInvalid(true);
		Assert.assertTrue(picker.isInvalid());
		picker.setInvalid(false);
		Assert.assertFalse(picker.isInvalid());

		Assert.assertNotNull(picker.standardValidation("illegal"));
		Assert.assertNull(picker.standardValidation(" white "));
		
		try {new JColorPickerWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JColorPickerWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JColorPickerWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiJColorPickerWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/color"))[0];
		final JColorPickerWithMeta		picker = new JColorPickerWithMeta(itemMeta, this) {
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

	@Test
	public void basicJDateFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final Date						nullDate = new Date(0);
		final String					nullFormatted = DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.getDefault()).format(nullDate);
		final JDateFieldWithMeta		date = new JDateFieldWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,date.getNodeMetadata());
		Assert.assertEquals(nullDate.toString(),date.getRawDataFromComponent());
		Assert.assertEquals(nullDate,date.getValueFromComponent());
		Assert.assertEquals(nullDate,date.getChangedValueFromComponent());
		Assert.assertEquals(Date.class,date.getValueType());
		
		date.setInvalid(true);
		Assert.assertTrue(date.isInvalid());
		date.setInvalid(false);
		Assert.assertFalse(date.isInvalid());

		Assert.assertNotNull(date.standardValidation("illegal"));
		Assert.assertNull(date.standardValidation(" "+nullFormatted+" "));
		
		try {new JDateFieldWithMeta(null, this);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new JDateFieldWithMeta(metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/boolValue"))[0], this);
			Assert.fail("Mandatory exception was not detected (invalid content type for this control)");
		} catch (IllegalArgumentException exc) {
		}
		try {new JDateFieldWithMeta(itemMeta, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Category(UITestCategory.class)
	@Test
	public void uiJDateFieldWithMetaTest() throws SyntaxException, ContentException, EnvironmentException, DebuggingException, InterruptedException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0];
		final JDateFieldWithMeta		date = new JDateFieldWithMeta(itemMeta, this);
		final Date						nullDate = new Date(0);
		final String					nullFormatted = DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.getDefault()).format(nullDate);
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

		Assert.assertNotNull(date.standardValidation("illegal"));
		Assert.assertNull(date.standardValidation(" CONTINUE "));
		
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

	@Test
	public void basicJFileFieldWithMetaTest() throws SyntaxException, LocalizationException,ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/file"))[0];
		final JFileFieldWithMeta		file = new JFileFieldWithMeta(itemMeta, this);
		
		Assert.assertEquals(itemMeta,file.getNodeMetadata());
		Assert.assertEquals(null,file.getRawDataFromComponent());
		Assert.assertEquals(null,file.getValueFromComponent());
		Assert.assertEquals(null,file.getChangedValueFromComponent());
		Assert.assertEquals(File.class,file.getValueType());
		
		file.setInvalid(true);
		Assert.assertTrue(file.isInvalid());
		file.setInvalid(false);
		Assert.assertFalse(file.isInvalid());

		Assert.assertNotNull(file.standardValidation("file:illegal"));
		Assert.assertNull(file.standardValidation(" CONTINUE "));
		
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
	
}