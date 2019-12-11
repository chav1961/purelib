package chav1961.purelib.i18n;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.LocalizerFactory.PostProcessCallback;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleSpecificTextSetter;
import chav1961.purelib.i18n.interfaces.Localizer;

public class LocalizerFactoryTest {

	@Test
	public void getLocalizerTest() throws NullPointerException, IOException {
		final Localizer	l = LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test"));
		
		Assert.assertNotNull(l);
		Assert.assertEquals(l,LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test")));
		
		try {LocalizerFactory.getLocalizer(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":unknown:/"));
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument scheme)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void buildLocalizerTest() throws NullPointerException, IOException, LocalizationException {
		Assert.assertNull(LocalizerFactory.buildLocalizerForInstance(new Object()));
		try {LocalizerFactory.buildLocalizerForInstance(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final PseudoLocalizerCheck	plc = new PseudoLocalizerCheck(); 
		final Localizer				child = LocalizerFactory.buildLocalizerForInstance(plc);
		
		Assert.assertNotNull(child);
		Assert.assertTrue(child.containsKey("key1"));
		Assert.assertTrue(child.containsKey("key2"));
		
		Assert.assertNull(LocalizerFactory.buildLocalizerForInstance(new PseudoLocalizerErr1()));
		Assert.assertNull(LocalizerFactory.buildLocalizerForInstance(new PseudoLocalizerErr2()));
		try{LocalizerFactory.buildLocalizerForInstance(new PseudoLocalizerErr3());
			Assert.fail("Mandatory exception was not detected (unknown keys in the annotations)");
		} catch (LocalizationException exc) {
		}
		try{LocalizerFactory.buildLocalizerForInstance(new PseudoLocalizerErr4());
			Assert.fail("Mandatory exception was not detected (annotation for illegal field class)");
		} catch (LocalizationException exc) {
		}
	}

	@Test
	public void fillLocalizerTest() throws NullPointerException, IOException, LocalizationException {
		final PseudoLocalizerCheck	plc = new PseudoLocalizerCheck(); 
		final Localizer				l = LocalizerFactory.buildLocalizerForInstance(plc);
		
		l.setCurrentLocale(new Locale.Builder().setLanguage("en").build());

		LocalizerFactory.fillLocalizedContent(l,plc);
		Assert.assertEquals(plc.f1.getText(),l.getValue("key1"));
		Assert.assertEquals(plc.f1.getToolTipText(),l.getValue("key2"));
		Assert.assertEquals(plc.text,l.getValue("key1"));
		Assert.assertEquals(plc.tooltip,l.getValue("key2"));
		Assert.assertEquals(plc.f3.getText(),l.getValue("key1"));
		Assert.assertEquals(plc.f3.getToolTipText(),l.getValue("key2"));
		Assert.assertEquals(plc.f4.getText(),l.getValue("key1"));
		Assert.assertEquals(plc.f4.getToolTipText(),l.getValue("key2"));
		
		try{LocalizerFactory.fillLocalizedContent(null,plc);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{LocalizerFactory.fillLocalizedContent(l,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{LocalizerFactory.fillLocalizedContent(l,new PseudoLocalizerErr3());
			Assert.fail("Mandatory exception was not detected (unknown keys in the annotations)");
		} catch (LocalizationException exc) {
		}
		try{LocalizerFactory.fillLocalizedContent(l,new PseudoLocalizerErr4());
			Assert.fail("Mandatory exception was not detected (annotation for illegal field class)");
		} catch (LocalizationException exc) {
		}
		
		try{LocalizerFactory.fillLocalizedContent(l,plc,null,(localizer, instance, f, value)->value);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{LocalizerFactory.fillLocalizedContent(l,plc,(localizer,instance,f,text,tooltip,toFill,postprocess)->{},null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
	}
}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test")
class PseudoLocalizerCheck {
	public String					text = "", tooltip = "";
	
@LocaleResource(value="key1",tooltip="key2")
	public final JLabel				f1 = new JLabel();

@LocaleResource(value="key1",tooltip="key2")
	public final LocaleSpecificTextSetter	f2 = new LocaleSpecificTextSetter() {
		@Override public void setLocaleSpecificText(String text) {PseudoLocalizerCheck.this.text = text;}
		@Override public void setLocaleSpecificToolTipText(String toolTip) {PseudoLocalizerCheck.this.tooltip = toolTip;}
	}; 

	@LocaleResource(value="key1",tooltip="key2")
	public final JTextField			f3 = new JTextField();

	@LocaleResource(value="key1",tooltip="key2")
	public final JButton			f4 = new JButton();
}

class PseudoLocalizerErr1 {
}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test")
class PseudoLocalizerErr2 {
}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test")
class PseudoLocalizerErr3 {
	
@LocaleResource(value="unknown",tooltip="key2")
	public final JLabel				f1 = new JLabel();

@LocaleResource(value="key1",tooltip="unknown")
	public final JLabel				f2 = new JLabel();
}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test")
class PseudoLocalizerErr4 {
	
@LocaleResource(value="key1",tooltip="key2")
	public final Object	f1 = new Object();
}
