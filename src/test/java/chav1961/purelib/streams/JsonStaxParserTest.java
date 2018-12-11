package chav1961.purelib.streams;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.streams.JsonStaxParser.LexType;

public class JsonStaxParserTest {
	@Test
	public void basicTest() throws IOException {
		Assert.assertArrayEquals(parse("100"),new LexType[]{LexType.INTEGER_VALUE});
		Assert.assertArrayEquals(parse("100.0"),new LexType[]{LexType.REAL_VALUE});
		Assert.assertArrayEquals(parse("\"123\""),new LexType[]{LexType.STRING_VALUE});
		Assert.assertArrayEquals(parse("true"),new LexType[]{LexType.BOOLEAN_VALUE});
		Assert.assertArrayEquals(parse("false"),new LexType[]{LexType.BOOLEAN_VALUE});
		Assert.assertArrayEquals(parse("null"),new LexType[]{LexType.NULL_VALUE});
	}

	@Test
	public void arrayTest() throws IOException {
		Assert.assertArrayEquals(parse("[]"),new LexType[]{LexType.START_ARRAY,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100]"),new LexType[]{LexType.START_ARRAY,LexType.INTEGER_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100,200]"),new LexType[]{LexType.START_ARRAY,LexType.INTEGER_VALUE,LexType.LIST_SPLITTER,LexType.INTEGER_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100.0]"),new LexType[]{LexType.START_ARRAY,LexType.REAL_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100.0,200.0]"),new LexType[]{LexType.START_ARRAY,LexType.REAL_VALUE,LexType.LIST_SPLITTER,LexType.REAL_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[\"123\"]"),new LexType[]{LexType.START_ARRAY,LexType.STRING_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[\"123\",\"456\"]"),new LexType[]{LexType.START_ARRAY,LexType.STRING_VALUE,LexType.LIST_SPLITTER,LexType.STRING_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[true]"),new LexType[]{LexType.START_ARRAY,LexType.BOOLEAN_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[true,false]"),new LexType[]{LexType.START_ARRAY,LexType.BOOLEAN_VALUE,LexType.LIST_SPLITTER,LexType.BOOLEAN_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[null]"),new LexType[]{LexType.START_ARRAY,LexType.NULL_VALUE,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[null,null]"),new LexType[]{LexType.START_ARRAY,LexType.NULL_VALUE,LexType.LIST_SPLITTER,LexType.NULL_VALUE,LexType.END_ARRAY});
	}

	@Test
	public void objectTest() throws IOException {
		Assert.assertArrayEquals(parse("{}"),new LexType[]{LexType.START_OBJECT,LexType.END_OBJECT});
		Assert.assertArrayEquals(parse("{\"x\":null}"),new LexType[]{LexType.START_OBJECT,LexType.NAME,LexType.NAME_SPLITTER,LexType.NULL_VALUE,LexType.END_OBJECT});
		Assert.assertArrayEquals(parse("{\"x\":null,\"y\":null}"),new LexType[]{LexType.START_OBJECT,LexType.NAME,LexType.NAME_SPLITTER,LexType.NULL_VALUE,LexType.LIST_SPLITTER,LexType.NAME,LexType.NAME_SPLITTER,LexType.NULL_VALUE,LexType.END_OBJECT});
	}

	@Test
	public void nestedTest() throws IOException {
		Assert.assertArrayEquals(parse("[{}]"),new LexType[]{LexType.START_ARRAY,LexType.START_OBJECT,LexType.END_OBJECT,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[[]]"),new LexType[]{LexType.START_ARRAY,LexType.START_ARRAY,LexType.END_ARRAY,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[{\"x\":null},{\"x\":null}]"),new LexType[]{LexType.START_ARRAY,LexType.START_OBJECT,LexType.NAME,LexType.NAME_SPLITTER,LexType.NULL_VALUE,LexType.END_OBJECT,LexType.LIST_SPLITTER,LexType.START_OBJECT,LexType.NAME,LexType.NAME_SPLITTER,LexType.NULL_VALUE,LexType.END_OBJECT,LexType.END_ARRAY});
		Assert.assertArrayEquals(parse("{\"x\":[],\"y\":[100]}"),new LexType[]{LexType.START_OBJECT,LexType.NAME,LexType.NAME_SPLITTER,LexType.START_ARRAY,LexType.END_ARRAY,LexType.LIST_SPLITTER,LexType.NAME,LexType.NAME_SPLITTER,LexType.START_ARRAY,LexType.INTEGER_VALUE,LexType.END_ARRAY,LexType.END_OBJECT});
	}
	
	
	private LexType[] parse(final String parsing) throws IOException {
		final List<LexType>	result =  new ArrayList<>();
		
		try(final Reader			rdr = new StringReader(parsing);
			final JsonStaxParser	pars = new JsonStaxParser(rdr)) {
			
			for (LexType item : pars) {
				result.add(item);
			}
		}
		return result.toArray(new LexType[result.size()]);
	}
}
