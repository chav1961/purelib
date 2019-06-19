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
import org.junit.Test;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleDescriptor;

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
		
		pl.setCurrentLocale(new Locale("en"));
		pl.setCurrentLocale(new Locale("ru"));
		pl.setCurrentLocale(new Locale("en"));
		count = 0;
		for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
			count++;
		}
		Assert.assertEquals(count, 4);
		Assert.assertEquals(pl.getValue("key1"),"value1");
		Assert.assertEquals(pl.getValue("key2"),"value2");
		Assert.assertEquals(pl.getValue("key3"),"HELP_test");
		Assert.assertEquals(pl.getValue("key4"),"HELP_test");
		
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

		pl.setCurrentLocale(new Locale("ru"));
		count = 0;
		for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
			count++;
		}
		Assert.assertEquals(count, 4);
		Assert.assertEquals(pl.getValue("key1"),"значение1");
		Assert.assertEquals(pl.getValue("key2"),"значение2");
		Assert.assertEquals(pl.getValue("key3"),"HELP_проверка");
		Assert.assertEquals(pl.getValue("key4"),"HELP_проверка");
		
		try(final Reader	content = pl.getContent("key3");
			final Writer	wr = new StringWriter()) {

			Utils.copyStream(content,wr);
			Assert.assertEquals(wr.toString(),"HELP_проверка");
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
		pl.setCurrentLocale(new Locale("en"));
		Assert.assertEquals(callCount[0],1);
		callCount[0] = 0;
		pl.setCurrentLocale(new Locale("en"));
		Assert.assertEquals(callCount[0],0);
		
		pl.removeLocaleChangeListener(lcl);
		callCount[0] = 0;
		pl.setCurrentLocale(new Locale("ru"));
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
		try {pl.setCurrentLocale(new Locale("zz"));
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
		
		try{root.add(null);
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
		
		try{root.push(null);
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
		
		root.setCurrentLocale(new Locale("en"));
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

	@Test
	public void fileSystemLocalizerTest() throws LocalizationException, IOException {
		try(final FileSystemLocalizer	fsl = new FileSystemLocalizer("fsys:file:./src/test/resources/chav1961/purelib/i18n#/test")) {
			int		count;
			
			fsl.setCurrentLocale(new Locale("en"));
			count = 0;
			for (@SuppressWarnings("unused") String item : fsl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals(fsl.getValue("key1"),"value1");
			Assert.assertEquals(fsl.getValue("key2"),"value2");
			
			fsl.setCurrentLocale(new Locale("ru"));
			count = 0;
			for (@SuppressWarnings("unused") String item : fsl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals(fsl.getValue("key1"),"значение1");
			Assert.assertEquals(fsl.getValue("key2"),"значение2");
		}
	}

	@Test
	public void propertiesLocalizerTest() throws LocalizationException, IOException {
		try(final PropertiesLocalizer	pl = new PropertiesLocalizer("chav1961/purelib/i18n/test")) {
			int		count;
			
			pl.setCurrentLocale(new Locale("en"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals(pl.getValue("key1"),"value1");
			Assert.assertEquals(pl.getValue("key2"),"value2");
			
			pl.setCurrentLocale(new Locale("ru"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals(pl.getValue("key1"),"значение1");
			Assert.assertEquals(pl.getValue("key2"),"значение2");
		}
	}

	@Test
	public void xmlLocalizerTest() throws LocalizationException, IOException {
		try(final XMLLocalizer	pl = new XMLLocalizer(URI.create("./src/test/resources/chav1961/purelib/i18n/test.xml"))) {
			int		count;
			
			pl.setCurrentLocale(new Locale("en"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals(pl.getValue("key1"),"value1");
			Assert.assertEquals(pl.getValue("key2"),"value2");
			
			pl.setCurrentLocale(new Locale("ru"));
			count = 0;
			for (@SuppressWarnings("unused") String item : pl.availableKeys()) {
				count++;
			}
			Assert.assertEquals(count, 3);
			Assert.assertEquals(pl.getValue("key1"),"значение1");
			Assert.assertEquals(pl.getValue("key2"),"значение2");
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
				content.put("key1","значение1");
				content.put("key2","значение2");
				content.put("key3","uri(проверка)");
				content.put("key4","uri(проверка?mime=text/html)");
				break;
		}
		
	}

	@Override
	protected String getHelp(final String helpId) throws LocalizationException, IllegalArgumentException {
		return "HELP_"+helpId;
	}

	@Override
	public String getLocalizerId() {
		return "AnyId";
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
	protected String getHelp(String helpId) throws LocalizationException, IllegalArgumentException {
		return helpId;
	}

	@Override
	public String toString() {
		return "SingleKeyLocalizer [key=" + key + ", enValue=" + enValue + ", ruValue=" + ruValue + ", value=" + value + "]";
	}

	@Override
	public String getLocalizerId() {
		return "AnyId";
	}	
}
