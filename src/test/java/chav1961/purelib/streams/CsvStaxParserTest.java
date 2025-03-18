package chav1961.purelib.streams;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.CsvStaxParserLexType;

@Tag("OrdinalTestCategory")
public class CsvStaxParserTest {
	@Test
	public void unnamedContentTest() throws IOException, SyntaxException {
		try(final Reader		rdr = new StringReader("123,123,123,true\r\n123,\"123\"\"456\",123.456,false");
			final CsvStaxParser	parser = new CsvStaxParser(rdr,1000,false,long.class,String.class,double.class,boolean.class)) {
			final int[]			lexCount = new int[CsvStaxParserLexType.values().length];
			final Object[]		awaited = {123L,"123",123.0,true,123L,"123\"456",123.456,false};
			int					awaitedCount = 0;
			
			for (CsvStaxParserLexType lex : parser) {
				lexCount[lex.ordinal()]++;
				
				switch (lex) {
					case BOOLEAN_VALUE	:
						Assert.assertEquals(parser.booleanValue(),awaited[awaitedCount++]);
						break;
					case INTEGER_VALUE	:
						Assert.assertEquals(parser.intValue(),awaited[awaitedCount++]);
						break;
					case NULL_VALUE		:
						break;
					case REAL_VALUE		:
						Assert.assertEquals(parser.realValue(),awaited[awaitedCount++]);
						break;
					case STRING_VALUE	:
						Assert.assertEquals(parser.stringValue(),awaited[awaitedCount++]);
						break;
					case ERROR			:
						Assert.fail("Unwaited error in the input stream ("+parser.getLastError()+")");
					default:
						break;
				}
			}
			Assert.assertArrayEquals(new int[]{0,0,0,0,0,2,2,2,2,0,0},lexCount);
		}
	}

	@Test
	public void namedContentTest() throws IOException, SyntaxException {
		try(final Reader		rdr = new StringReader("f1,f2,f3,f4\r\n123,123,123,true\r\n123,\"123\"\"456\",123.456,false");
			final CsvStaxParser	parser = new CsvStaxParser(rdr,1000,true,long.class,String.class,double.class,boolean.class)) {
			final int[]			lexCount = new int[CsvStaxParserLexType.values().length];
			final Object[]		awaited = {"f1","f2","f3","f4",123L,"123",123.0,true,123L,"123\"456",123.456,false};
			int					awaitedCount = 0;
			
			for (CsvStaxParserLexType lex : parser) {
				lexCount[lex.ordinal()]++;
				
				switch (lex) {
					case NAME			:
						Assert.assertEquals(parser.name(),awaited[awaitedCount++]);
						break;
					case BOOLEAN_VALUE	:
						Assert.assertEquals(parser.booleanValue(),awaited[awaitedCount++]);
						break;
					case INTEGER_VALUE	:
						Assert.assertEquals(parser.intValue(),awaited[awaitedCount++]);
						break;
					case NULL_VALUE		:
						break;
					case REAL_VALUE		:
						Assert.assertEquals(parser.realValue(),awaited[awaitedCount++]);
						break;
					case STRING_VALUE	:
						Assert.assertEquals(parser.stringValue(),awaited[awaitedCount++]);
						break;
					case ERROR			:
						Assert.fail("Unwaited error in the input stream ("+parser.getLastError()+")");
					default:
						break;
				}
			}
			Assert.assertArrayEquals(new int[]{0,4,0,0,0,2,2,2,2,0,0},lexCount);
		}
	}

	@Test
	public void buferSwitchTest() throws IOException, SyntaxException {
		try(final Reader		rdr = new StringReader("123,123,123,true\r\n123,\"123\"\"456\",123.456,false");
			final CsvStaxParser	parser = new CsvStaxParser(rdr,1,false,long.class,String.class,double.class,boolean.class)) {
			final int[]			lexCount = new int[CsvStaxParserLexType.values().length];
			final Object[]		awaited = {123L,"123",123.0,true,123L,"123\"456",123.456,false};
			int					awaitedCount = 0;
			
			for (CsvStaxParserLexType lex : parser) {
				lexCount[lex.ordinal()]++;
				
				switch (lex) {
					case BOOLEAN_VALUE	:
						Assert.assertEquals(parser.booleanValue(),awaited[awaitedCount++]);
						break;
					case INTEGER_VALUE	:
						Assert.assertEquals(parser.intValue(),awaited[awaitedCount++]);
						break;
					case NULL_VALUE		:
						break;
					case REAL_VALUE		:
						Assert.assertEquals(parser.realValue(),awaited[awaitedCount++]);
						break;
					case STRING_VALUE	:
						Assert.assertEquals(parser.stringValue(),awaited[awaitedCount++]);
						break;
					case ERROR			:
						Assert.fail("Unwaited error in the input stream ("+parser.getLastError()+")");
					default:
						break;
				}
			}
		}
	}

	@Test
	public void exceptionsTest() throws IOException, SyntaxException {
		try{new CsvStaxParser(null,1,true,long.class,String.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		
		try(final Reader		rdr = new StringReader("f1,f2\n123,123")) {
			
			try{new CsvStaxParser(rdr,0,false,long.class,String.class);
				Assert.fail("Mandatory exception was not detected (non-positive 2-nd argument)");
			} catch (IllegalArgumentException exc) {			
			}
			
			try{new CsvStaxParser(rdr,1000,false,(Class<?>[])null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (IllegalArgumentException exc) {			
			}
			try{new CsvStaxParser(rdr,1000,false);
				Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
			} catch (IllegalArgumentException exc) {			
			}
			
			try{new CsvStaxParser(rdr,1000,false,String.class,null);
				Assert.fail("Mandatory exception was not detected (null inside 3-rd argument)");
			} catch (IllegalArgumentException exc) {			
			}

			try{new CsvStaxParser(rdr,1000,false,List.class);
				Assert.fail("Mandatory exception was not detected (invalid class inside 3-rd argument)");
			} catch (IllegalArgumentException exc) {			
			}
			
			try(final CsvStaxParser	parser = new CsvStaxParser(rdr,1000,true,long.class,String.class)) {
				final Iterator<CsvStaxParserLexType>	iterator = parser.iterator();	

				Assert.assertTrue(iterator.hasNext());
				Assert.assertEquals(CsvStaxParserLexType.NAME,iterator.next());
				try{parser.stringValue();						
					Assert.fail("Mandatory exception was not detected (read string on name)");
				} catch (IllegalStateException exc) {
				}

				Assert.assertTrue(iterator.hasNext());
				Assert.assertEquals(CsvStaxParserLexType.NAME,iterator.next());
				
				try{parser.name(null,1,10);						
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{parser.name(new char[0],1,10);						
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{parser.name(new char[1],0,10);						
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				
				Assert.assertTrue(iterator.hasNext());
				Assert.assertEquals(CsvStaxParserLexType.INTEGER_VALUE,iterator.next());
				
				try{parser.name();						
					Assert.fail("Mandatory exception was not detected (read name on long)");
				} catch (IllegalStateException exc) {
				}
				try{parser.booleanValue();						
					Assert.fail("Mandatory exception was not detected (read boolean on long)");
				} catch (IllegalStateException exc) {
				}
				try{parser.realValue();						
					Assert.fail("Mandatory exception was not detected (read real on long)");
				} catch (IllegalStateException exc) {
				}
				try{parser.stringValue();						
					Assert.fail("Mandatory exception was not detected (read string on long)");
				} catch (IllegalStateException exc) {
				}
				
				Assert.assertTrue(iterator.hasNext());
				Assert.assertEquals(CsvStaxParserLexType.STRING_VALUE,iterator.next());

				try{parser.intValue();						
					Assert.fail("Mandatory exception was not detected (read integer on string)");
				} catch (IllegalStateException exc) {
				}
				
				try{parser.stringValue(null,1,10);						
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{parser.stringValue(new char[0],1,10);						
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{parser.stringValue(new char[1],0,10);						
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}
}
