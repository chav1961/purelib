package chav1961.purelib.i18n;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.i18n.interfaces.LocalizedString;
import chav1961.purelib.i18n.interfaces.MutableLocalizedString;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.streams.JsonStaxPrinter;

@Tag("OrdinalTestCategory")
public class MutableLocalizerTest {
	@Test
	public void basicTest() {
		try(final XMLLocalizer	pl = new XMLLocalizer(URI.create("./src/test/resources/chav1961/purelib/i18n/test.xml"))) {
			final LocalizedString		ls = pl.getLocalizedString("key1");

			Assert.assertEquals("key1", ls.getId());
			Assert.assertEquals(pl, ls.getLocalizer());
			Assert.assertEquals("value1", ls.getValue(SupportedLanguages.en.getLocale()));
			Assert.assertEquals("значение1", ls.getValue(SupportedLanguages.ru.getLocale()));
		}
	}

	@Test
	public void mutableTest() throws PrintingException, IOException {
		try(final MutableJsonLocalizer	pl = new MutableJsonLocalizer(URI.create("./src/test/resources/chav1961/purelib/i18n/test.json"))) {
			final LocalizedString		ls = pl.getLocalizedString("key1");
			
			Assert.assertEquals("key1", ls.getId());
			Assert.assertEquals(pl, ls.getLocalizer());
			Assert.assertEquals("value1", ls.getValue(SupportedLanguages.en.getLocale()));
			Assert.assertEquals("значение1", ls.getValue(SupportedLanguages.ru.getLocale()));
			Assert.assertTrue(ls instanceof MutableLocalizedString);
			
			final MutableLocalizedString	mls = (MutableLocalizedString)ls;
			
			Assert.assertTrue(mls.isLanguageSupported(SupportedLanguages.en.getLocale()));

			mls.removeValue(SupportedLanguages.en.getLocale());
			Assert.assertFalse(mls.isLanguageSupported(SupportedLanguages.en.getLocale()));
			
			mls.addValue(SupportedLanguages.en.getLocale(), "${key3}");
			Assert.assertTrue(mls.isLanguageSupported(SupportedLanguages.en.getLocale()));
			Assert.assertEquals("value1", ls.getValue(SupportedLanguages.en.getLocale()));
			
			mls.setValue(SupportedLanguages.en.getLocale(), "${key3} assa");
			Assert.assertTrue(mls.isLanguageSupported(SupportedLanguages.en.getLocale()));
			Assert.assertEquals("value1 assa", ls.getValue(SupportedLanguages.en.getLocale()));

			mls.setId("key4");
			Assert.assertTrue(mls.isLanguageSupported(SupportedLanguages.en.getLocale()));
			Assert.assertEquals("key4", ls.getId());
			Assert.assertEquals("value1 assa", ls.getValue(SupportedLanguages.en.getLocale()));

			try(final Writer			wr = new StringWriter();
				final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
				
				prn.setNewLineAppended(true);
				pl.saveContent(prn);
				prn.flush();
			}
		}
	}
}
