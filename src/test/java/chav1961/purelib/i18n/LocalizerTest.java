package chav1961.purelib.i18n;

import java.io.IOException;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleDescriptor;
import chav1961.purelib.i18n.internal.FileSystemLocalizer;
import chav1961.purelib.i18n.internal.MutableJsonLocalizer;
import chav1961.purelib.i18n.internal.XMLLocalizer;

@Tag("OrdinalTestCategory")
public class LocalizerTest {

	@Test
	public void basicFunctionalityTest() throws LocalizationException, IOException {
		final PseudoLocalizer	pl = new PseudoLocalizer();
		int		count;
		
		Assert.assertEquals(pl.currentLocale().getLanguage(),Locale.getDefault().getLanguage());
		count = 0;
		for (@SuppressWarnings("unused") LocaleDescriptor item : pl.supportedLocales()) {
			count++;
		}
		Assert.assertEquals(count, 2);
		
		pl.setCurrentLocale(Locale.of("en"));
		pl.setCurrentLocale(Locale.of("ru"));
		pl.setCurrentLocale(Locale.of("en"));
		count = 0;
		for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
			count++;
		}
		Assert.assertEquals(4, count);
		Assert.assertEquals("value1", pl.getValue("key1"));
		Assert.assertEquals("value2", pl.getValue("key2"));
		Assert.assertEquals("HELP_test", pl.getValue("key3"));
		Assert.assertEquals("HELP_test", pl.getValue("key4"));

		try {pl.getValue("");
			Assert.fail("Mandatory exception was not detected (null or empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {pl.getValue("unknown");
			Assert.fail("Mandatory exception was not detected (unknown key to get value for)");
		} catch (LocalizationException exc) {
		}
		
		Assert.assertEquals(pl.currentLocale().getLanguage(),"en");
		Assert.assertEquals(pl.currentLocale().getDescription(),"English");
		Assert.assertNotNull(pl.currentLocale().getIcon());

		pl.setCurrentLocale(Locale.of("ru"));
		count = 0;
		for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
			count++;
		}
		Assert.assertEquals(count, 4);
		Assert.assertEquals(pl.getValue("key1"),"Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ1");
		Assert.assertEquals(pl.getValue("key2"),"Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ2");
		Assert.assertEquals(pl.getValue("key3"),"HELP_Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ");
		Assert.assertEquals(pl.getValue("key4"),"HELP_Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ");
		
		try(final Reader	content = pl.getContent("key3");
			final Writer	wr = new StringWriter()) {

			Utils.copyStream(content,wr);
			Assert.assertEquals(wr.toString(),"HELP_Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ");
		}

		Assert.assertEquals(pl.currentLocale().getLanguage(),"ru");
		Assert.assertEquals(pl.currentLocale().getDescription(),"Russian");
		Assert.assertNotNull(pl.currentLocale().getIcon());
		
		Assert.assertTrue(pl.containsKey("key1"));
		Assert.assertFalse(pl.containsKey("unknown"));
		
		try {pl.containsKey("");
			Assert.fail("Mandatory exception was not detected (null or empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		final int[]	callCount = new int[1];
		final LocaleChangeListener	lcl = (oldLocale,newLocale)->{callCount[0]++;}; 
		
		pl.addLocaleChangeListener(lcl);
		
		callCount[0] = 0;
		pl.setCurrentLocale(Locale.of("en"));
		Assert.assertEquals(callCount[0],1);
		callCount[0] = 0;
		pl.setCurrentLocale(Locale.of("en"));
		Assert.assertEquals(callCount[0],0);
		
		pl.removeLocaleChangeListener(lcl);
		callCount[0] = 0;
		pl.setCurrentLocale(Locale.of("ru"));
		Assert.assertEquals(callCount[0],0);
		
		try {pl.addLocaleChangeListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {pl.removeLocaleChangeListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
				
		try {pl.setCurrentLocale(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {pl.setCurrentLocale(Locale.of("zz"));
			Assert.fail("Mandatory exception was not detected (unsupported locale)");
		} catch (LocalizationException exc) {
		}
		
		try {pl.setParent(pl);
			Assert.fail("Mandatory exception was not detected (recursive chain in the parent)");
		} catch (LocalizationException exc) {
		}
	}

	@Test
	public void treeTest() throws LocalizationException, IOException {
		final Localizer		root = new SingleKeyLocalizer("root","rootEn","rootRu");
		Localizer			sibling, nested, nestedSibling;
		int					count;
		
		root.add(sibling = new SingleKeyLocalizer("sibling","siblingEn","siblingRu"));
		count = 0;
		for (String item : root.availableKeys()) {
			Assert.assertTrue("root".equals(item) || "sibling".equals(item));
			count++;
		}
		Assert.assertEquals(count,2);
		
		try{root.add((Localizer)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{root.add(root);
			Assert.fail("Mandatory exception was not detected (attempt to add self)");
		} catch (IllegalArgumentException exc) {			
		}
		try{root.add(new SingleKeyLocalizer("sibling","siblingEn","siblingRu"));
			Assert.fail("Mandatory exception was not detected (duplicated key in the sibling)");
		} catch (LocalizationException exc) {			
		}
		
		root.remove(sibling);
		count = 0;
		for (String item : root.availableKeys()) {
			Assert.assertTrue("root".equals(item));
			count++;
		}
		Assert.assertEquals(count,1);

		try{root.remove(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{root.remove(root);
			Assert.fail("Mandatory exception was not detected (attempt to remove self)");
		} catch (IllegalArgumentException exc) {			
		}
		try{root.remove(sibling);
			Assert.fail("Mandatory exception was not detected (sibling ot found)");
		} catch (IllegalStateException exc) {			
		}

		root.add(sibling = new SingleKeyLocalizer("sibling","siblingEn","siblingRu"));
		root.push(nested = new SingleKeyLocalizer("nested","nestedEn","nestedRu"));
		count = 0;
		for (String item : root.availableKeys()) {
			Assert.assertTrue("root".equals(item) || "sibling".equals(item));
			count++;
		}
		Assert.assertEquals(count,2);
		count = 0;
		for (String item : nested.availableKeys()) {
			Assert.assertTrue("root".equals(item) || "sibling".equals(item) || "nested".equals(item));
			count++;
		}
		Assert.assertEquals(count,3);
		
		try{root.push((Localizer)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{root.push(root);
			Assert.fail("Mandatory exception was not detected (attempt to push self)");
		} catch (IllegalArgumentException exc) {			
		}
		
		root.push(nestedSibling = new SingleKeyLocalizer("sibling","nestedSiblingEn","nestedSiblingRu"));
		count = 0;
		for (String item : nested.availableKeys()) {	// Sibling blinds the same parent key 
			Assert.assertTrue("root".equals(item) || "sibling".equals(item) || "nested".equals(item));
			count++;
		}
		Assert.assertEquals(count,3);
		
		root.setCurrentLocale(Locale.of("en"));
		Assert.assertEquals(nested.getValue("root"),"rootEn");
		Assert.assertEquals(nested.getValue("sibling"),"siblingEn");	// Sibling blinds the same parent key
		Assert.assertEquals(nested.getValue("nested"),"nestedEn");
		
		root.remove(nestedSibling);
		count = 0;
		for (String item : nested.availableKeys()) { 
			Assert.assertTrue("root".equals(item) || "sibling".equals(item) || "nested".equals(item));
			count++;
		}
		Assert.assertEquals(count,3);
		Assert.assertEquals(nested.getValue("root"),"rootEn");
		Assert.assertEquals(nested.getValue("sibling"),"siblingEn");
		Assert.assertEquals(nested.getValue("nested"),"nestedEn");
		
		root.pop();
		count = 0;
		for (String item : nested.availableKeys()) { 
			Assert.assertTrue("nested".equals(item));
			count++;
		}
		Assert.assertEquals(count,1);
		count = 0;
		for (String item : root.availableKeys()) { 
			Assert.assertTrue("root".equals(item) || "sibling".equals(item));
			count++;
		}
		Assert.assertEquals(count,2);
	}

	@Test
	public void walkingTest() throws LocalizationException, IOException {
//		final Localizer		root = new SingleKeyLocalizer("root","rootEn","rootRu");
	}

//	@Test not supported by Java 9
	public void fileSystemLocalizerTest() throws LocalizationException, IOException {
		try(final Localizer	fsl = Localizer.Factory.newInstance(URI.create(Localizer.LOCALIZER_SCHEME+":fsys:file:./src/test/resources/chav1961/purelib/i18n#/test"))) {
			int		count;
			
			fsl.setCurrentLocale(Locale.of("en"));
			count = 0;
			for (@SuppressWarnings("unused") String item : fsl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals(fsl.getValue("key1"), "value1");
			Assert.assertEquals(fsl.getValue("key2"), "value2");
			
			fsl.setCurrentLocale(Locale.of("ru"));
			count = 0;
			for (@SuppressWarnings("unused") String item : fsl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals("значение1", fsl.getValue("key1"));
			Assert.assertEquals("значение2", fsl.getValue("key2"));
		}
	}

	@Test
	public void xmlLocalizerTest() throws LocalizationException, IOException {
		try(final Localizer	pl = Localizer.Factory.newInstance(URI.create(Localizer.LOCALIZER_SCHEME+":xml:./src/test/resources/chav1961/purelib/i18n/test.xml"))) {
			int		count;
			
			pl.setCurrentLocale(Locale.of("en"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals("value1", pl.getValue("key1"));
			Assert.assertEquals("value2", pl.getValue("key2"));
			
			pl.setCurrentLocale(Locale.of("ru"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals("значение1", pl.getValue("key1"));
			Assert.assertEquals("значение2", pl.getValue("key2"));
		}
	}

	@Test
	public void jsonLocalizerTest() throws LocalizationException, IOException {
		try(final Localizer	pl = Localizer.Factory.newInstance(URI.create(Localizer.LOCALIZER_SCHEME+":mutablejson:./src/test/resources/chav1961/purelib/i18n/test.json"))) {
			int		count;
			
			pl.setCurrentLocale(Locale.of("en"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals("value1", pl.getValue("key1"));
			Assert.assertEquals("value2", pl.getValue("key2"));
			
			pl.setCurrentLocale(Locale.of("ru"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals("значение1", pl.getValue("key1"));
			Assert.assertEquals("значение2", pl.getValue("key2"));
		}
	}
}


class PseudoLocalizer extends AbstractLocalizer {
	protected PseudoLocalizer() throws LocalizationException, NullPointerException {
		super();
	}

	private final Map<String,String>	content = new HashMap<>();
	
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		return true;
	}

	@Override
	public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return null;
	}

	@Override
	public Iterable<String> localKeys() {
		return content.keySet();
	}

	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		return content.get(key);
	}

	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
		content.clear();
		switch (newLocale.getLanguage()) {
			case "en"	:
				content.put("key1","value1");
				content.put("key2","value2");
				content.put("key3","uri(test)");
				content.put("key4","uri(test?mime=text/html)");
				break;
			case "ru"	:
				content.put("key1","Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ1");
				content.put("key2","Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ2");
				content.put("key3","uri(Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ)");
				content.put("key4","uri(Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ?mime=text/html)");
				break;
		}
		
	}

	@Override
	public String getHelp(final String helpId, final Locale locale, final String encoding) throws LocalizationException, IllegalArgumentException {
		return "HELP_"+helpId;
	}

	@Override
	public URI getLocalizerId() {
		return URI.create("AnyId");
	}

	@Override
	public String getLocalValue(String key, Locale locale) throws LocalizationException, IllegalArgumentException {
		return getLocalValue(key);
	}

	@Override
	protected boolean isLocaleSupported(String key, Locale locale) throws LocalizationException, IllegalArgumentException {
		return "'en;ru;".contains(locale.getLanguage());
	}

	@Override
	public String getSubscheme() {
		// TODO Auto-generated method stub
		return null;
	}	
}

class SingleKeyLocalizer extends AbstractLocalizer {
	private final String 	key, enValue, ruValue;
	private String			value = null;
	
	public SingleKeyLocalizer(final String key, final String enValue, final String ruValue) throws LocalizationException, NullPointerException {
		this.key = key;
		this.enValue = enValue;
		this.ruValue = ruValue;
		loadResource(currentLocale().getLocale());
	}

	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		return false;
	}

	@Override
	public Localizer newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return null;
	}

	@Override
	public Iterable<String> localKeys() {
		return Arrays.asList(key);
	}

	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		return this.key.equals(key) ? value : null;
	}

	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
		switch (newLocale.getLanguage()) {
			case "en"	: value = enValue; break;
			case "ru"	: value = ruValue; break;
		}
	}

	@Override
	public String getHelp(final String helpId, final Locale locale, final String encoding) throws LocalizationException, IllegalArgumentException {
		return helpId;
	}

	@Override
	public String toString() {
		return "SingleKeyLocalizer [key=" + key + ", enValue=" + enValue + ", ruValue=" + ruValue + ", value=" + value + "]";
	}

	@Override
	public URI getLocalizerId() {
		return URI.create("AnyId");
	}

	@Override
	public String getLocalValue(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
		return getLocalValue(key);
	}

	@Override
	protected boolean isLocaleSupported(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
		return "'en;ru;".contains(locale.getLanguage());
	}

	@Override
	public String getSubscheme() {
		// TODO Auto-generated method stub
		return null;
	}	
}
