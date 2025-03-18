package chav1961.purelib.streams;

import java.io.IOException;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.CsvSaxHandler;

@Tag("OrdinalTestCategory")
public class CsvSaxParserTest {
	private static final String		SOURCE_1 = "name1, name2,name3 ,\"name4\"\n"+
												"value 1.1, value 1.2,value 1.3 ,\"value 1.4\"\n"+
												"10,+10.0e-2,- 10,\" 10\"\n"+
												"\"value\n3.1\n\",\"value,3.2\",\"value\"\"3.3\",\"value 3.4\"\n";
	private static final String		SOURCE_2 = "name1, name2,name3 ,\"name4\n"+
												"value 1.1, value 1.2,value 1.3 ,\"value 1.4\"\n"+
												"10,+10.0e-2,- 10,\" 10\"\n"+
												"\"value\n3.1\n\",\"value,3.2\",\"value\"\"3.3\",\"value 3.4\"\n";
	private static final String		SOURCE_3 = "name1, name2,name3 ,\"name4\"\n"+
												"value 1.1, value 1.2,value 1.3 \n"+
												"10,+10.0e-2,- 10,\" 10\"\n"+
												"\"value\n3.1\n\",\"value,3.2\",\"value\"\"3.3\",\"value 3.4\"\n";
	private static final String		SOURCE_4 = "name1, name2,name3 ,\"name4\"\n"+
												"value 1.1, value 1.2,value 1.3 ,\"value 1.4\",value 1.5\n"+
												"10,+10.0e-2,- 10,\" 10\"\n"+
												"\"value\n3.1\n\",\"value,3.2\",\"value\"\"3.3\",\"value 3.4\"\n";
	private static final Object[]	RESULT_NAMES_1 = {"name1","name2","name3","name4"};
	private static final Object[][]	RESULT_DATA_1 = {{"value 1.1"," value 1.2","value 1.3 ","value 1.4"}
													,{10L,10.0e-2,"- 10",10L}
													,{"value\n3.1\n","value,3.2","value\"3.3","value 3.4"}};
	
	@Test
	public void basicTest() throws IOException, SyntaxException {
		try(final Reader	rdr = new StringReader(SOURCE_1)) {
			testAndCheck(rdr,true,RESULT_NAMES_1,RESULT_DATA_1);
		}

		try(final Reader	rdr = new StringReader(SOURCE_2)) {
			testAndCheck(rdr,true,RESULT_NAMES_1,RESULT_DATA_1);
			
			Assert.fail("Mandatory exception was not detected (unquoted name)");
		} catch (SyntaxException exc) {
		}

		try(final Reader	rdr = new StringReader(SOURCE_3)) {
			testAndCheck(rdr,true,RESULT_NAMES_1,RESULT_DATA_1);
			
			Assert.fail("Mandatory exception was not detected (different fields)");
		} catch (SyntaxException exc) {
		}
		try(final Reader	rdr = new StringReader(SOURCE_4)) {
			testAndCheck(rdr,true,RESULT_NAMES_1,RESULT_DATA_1);
			
			Assert.fail("Mandatory exception was not detected (different fields)");
		} catch (SyntaxException exc) {
		}
		
	}

	@Test
	public void exceptionsTest() throws IOException, SyntaxException {
		final CsvSaxHandler	handler = new CsvSaxHandler() {
										@Override public void value(int position, char[] value, int from, int len) throws ContentException {}
										@Override public void value(int position, String value) throws ContentException {}
										@Override public void value(int position, double value) throws ContentException {}
										@Override public void value(int position, long value) throws ContentException {}
										@Override public void value(int position) throws ContentException {}
										@Override public void startDoc() throws ContentException {}
										@Override public void startData() throws ContentException {}
										@Override public void startCaption() throws ContentException {}
										@Override public void name(int position, char[] name, int from, int len) throws ContentException {}
										@Override public void name(int position, String name) throws ContentException {}
										@Override public void endDoc() throws ContentException {}
										@Override public void endData() throws ContentException {}
										@Override public void endCaption() throws ContentException {}
									};
		
		try{new CsvSaxParser(null, false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	
		try{new CsvSaxParser(handler, false).parse((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new CsvSaxParser(handler, false).parse((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new CsvSaxParser(handler, false).parse((Reader)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new CsvSaxParser(handler, false).parse("test".toCharArray(),100,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	private void testAndCheck(final Reader rdr, final boolean firstLineIsNames, final Object[] namesAwaited, final Object[][] dataAwaited) throws SyntaxException, IOException {
		final int[]		pairs = new int[]{0,0,0};
		final int[]		line = new int[]{0};
		
		final CsvSaxParser	parser = new CsvSaxParser(new CsvSaxHandler() {
			@Override
			public void value(final int position, final char[] value, final int from, final int len) throws ContentException {
				Assert.fail("Unwaited call - string conversion was required, but doesn't");
			}
			
			@Override
			public void value(final int position, final String value) throws ContentException {
				Assert.assertEquals(dataAwaited[line[0]][position],value);
			}
			
			@Override
			public void value(final int position, final double value) throws ContentException {
				Assert.assertEquals(dataAwaited[line[0]][position],Double.valueOf(value));
			}
			
			@Override
			public void value(final int position, final long value) throws ContentException {
				Assert.assertEquals(dataAwaited[line[0]][position],Long.valueOf(value));
			}
			
			@Override
			public void value(final int position) throws ContentException {
				Assert.assertNull(dataAwaited[line[0]][position]);
			}
			
			@Override
			public void name(final int position, final char[] name, final int from, final int len) throws ContentException {
				Assert.fail("Unwaited call - string conversion was required, but doesn't");
			}
			
			@Override
			public void name(final int position, final String name) throws ContentException {
				Assert.assertEquals(namesAwaited[position],name);
			}
			
			@Override
			public void startDoc() throws ContentException {
				pairs[0]++;
			}
			
			@Override
			public void startData() throws ContentException {
				pairs[2]++;
			}
			
			@Override
			public void startCaption() throws ContentException {
				pairs[1]++;
			}
			
			@Override
			public void endDoc() throws ContentException {
				pairs[0]--;
			}
			
			@Override
			public void endData() throws ContentException {
				pairs[2]--;
				line[0]++;
			}
			
			@Override
			public void endCaption() throws ContentException {
				pairs[1]--;
			}
		}, firstLineIsNames);

		parser.parse(rdr);
		
		Assert.assertEquals(3,line[0]);
		Assert.assertEquals(0,pairs[0]);	// Unpaired startDoc/endDoc
		Assert.assertEquals(0,pairs[1]);	// Unpaired startCaption/endCaption
		Assert.assertEquals(0,pairs[2]);	// Unpaired startData/endData
	}
}
