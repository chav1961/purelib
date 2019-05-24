package chav1961.purelib.streams;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.streams.interfaces.CsvStaxParserLexType;

public class CsvStaxParserTest {
	@Test
	public void unnamedContentTest() throws IOException {
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
			
		}
		
		
	}
}
