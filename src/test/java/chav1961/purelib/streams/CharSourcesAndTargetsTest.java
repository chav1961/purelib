package chav1961.purelib.streams;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.streams.charsource.ArrayCharSource;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.streams.charsource.StringCharSource;
import chav1961.purelib.streams.chartarget.ArrayCharTarget;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class CharSourcesAndTargetsTest {
	public static final String		CHAR_SOURCE_STRING = "test string";

	@Test
	public void basicCharacterSourceTest() throws IOException, ContentException {
		try(final Reader	rdr = new StringReader(CHAR_SOURCE_STRING)) {
			testCharSource(new ReaderCharSource(rdr,true));
		}
		try(final Reader	rdr = new StringReader(CHAR_SOURCE_STRING)) {
			testCharSource(new ReaderCharSource(rdr,false));
		}
		testCharSource(new StringCharSource(CHAR_SOURCE_STRING));
		testCharSource(new ArrayCharSource(CHAR_SOURCE_STRING.toCharArray(),0));
	}

	@Test
	public void basicCharacterTargetTest() throws IOException, PrintingException {
		try(final Writer		wr = new StringWriter()) {
			testCharTarget(new WriterCharTarget(wr, true));
		}
		try(final Writer		wr = new StringWriter()) {
			testCharTarget(new WriterCharTarget(wr, false));
		}
		try(final OutputStream	os = new ByteArrayOutputStream();
			final PrintStream	wr = new PrintStream(os)) {
			testCharTarget(new WriterCharTarget(wr, true));
		}
		try(final OutputStream	os = new ByteArrayOutputStream();
			final PrintStream	wr = new PrintStream(os)) {
			testCharTarget(new WriterCharTarget(wr, false));
		}
		testCharTarget(new StringBuilderCharTarget(new StringBuilder()));
		testCharTarget(new ArrayCharTarget(new char[100],0));
	}

	private void testCharSource(final CharacterSource src) throws ContentException {
		final StringBuilder	sb = new StringBuilder();
		char				actual;
		
		while ((actual = src.next()) != CharacterSource.EOF) {
			Assert.assertEquals(src.last(),actual);
			sb.append(actual);
		}
		Assert.assertEquals(sb.toString(),CHAR_SOURCE_STRING);
		Assert.assertEquals(src.totalRead(),CHAR_SOURCE_STRING.length());
	}
	
	private void testCharTarget(final CharacterTarget dest) throws PrintingException {
		for (int index = 0; index < 10; index++) {
			dest.put('h');
			dest.put("u");
			dest.put("j".toCharArray());
		}
		Assert.assertEquals(dest.totalWritten(),30);
	}
}
