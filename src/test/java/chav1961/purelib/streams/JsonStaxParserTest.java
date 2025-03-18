package chav1961.purelib.streams;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

@Tag("OrdinalTestCategory")
public class JsonStaxParserTest {
	@Test
	public void basicTest() throws IOException {
		Assert.assertArrayEquals(parse("100"),new JsonStaxParserLexType[]{JsonStaxParserLexType.INTEGER_VALUE});
		Assert.assertArrayEquals(parse("100.0"),new JsonStaxParserLexType[]{JsonStaxParserLexType.REAL_VALUE});
		Assert.assertArrayEquals(parse("\"123\""),new JsonStaxParserLexType[]{JsonStaxParserLexType.STRING_VALUE});
		Assert.assertArrayEquals(parse("true"),new JsonStaxParserLexType[]{JsonStaxParserLexType.BOOLEAN_VALUE});
		Assert.assertArrayEquals(parse("false"),new JsonStaxParserLexType[]{JsonStaxParserLexType.BOOLEAN_VALUE});
		Assert.assertArrayEquals(parse("null"),new JsonStaxParserLexType[]{JsonStaxParserLexType.NULL_VALUE});
	}

	@Test
	public void arrayTest() throws IOException {
		Assert.assertArrayEquals(parse("[]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.INTEGER_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100,200]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.INTEGER_VALUE,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.INTEGER_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100.0]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.REAL_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[100.0,200.0]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.REAL_VALUE,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.REAL_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[\"123\"]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.STRING_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[\"123\",\"456\"]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.STRING_VALUE,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.STRING_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[true]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.BOOLEAN_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[true,false]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.BOOLEAN_VALUE,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.BOOLEAN_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[null]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[null,null]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.END_ARRAY});
	}

	@Test
	public void objectTest() throws IOException {
		Assert.assertArrayEquals(parse("{}"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_OBJECT,JsonStaxParserLexType.END_OBJECT});
		Assert.assertArrayEquals(parse("{\"x\":null}"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_OBJECT,JsonStaxParserLexType.NAME,JsonStaxParserLexType.NAME_SPLITTER,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.END_OBJECT});
		Assert.assertArrayEquals(parse("{\"x\":null,\"y\":null}"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_OBJECT,JsonStaxParserLexType.NAME,JsonStaxParserLexType.NAME_SPLITTER,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.NAME,JsonStaxParserLexType.NAME_SPLITTER,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.END_OBJECT});
	}

	@Test
	public void nestedTest() throws IOException {
		Assert.assertArrayEquals(parse("[{}]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.START_OBJECT,JsonStaxParserLexType.END_OBJECT,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[[]]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.END_ARRAY,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("[{\"x\":null},{\"x\":null}]"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.START_OBJECT,JsonStaxParserLexType.NAME,JsonStaxParserLexType.NAME_SPLITTER,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.END_OBJECT,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.START_OBJECT,JsonStaxParserLexType.NAME,JsonStaxParserLexType.NAME_SPLITTER,JsonStaxParserLexType.NULL_VALUE,JsonStaxParserLexType.END_OBJECT,JsonStaxParserLexType.END_ARRAY});
		Assert.assertArrayEquals(parse("{\"x\":[],\"y\":[100]}"),new JsonStaxParserLexType[]{JsonStaxParserLexType.START_OBJECT,JsonStaxParserLexType.NAME,JsonStaxParserLexType.NAME_SPLITTER,JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.END_ARRAY,JsonStaxParserLexType.LIST_SPLITTER,JsonStaxParserLexType.NAME,JsonStaxParserLexType.NAME_SPLITTER,JsonStaxParserLexType.START_ARRAY,JsonStaxParserLexType.INTEGER_VALUE,JsonStaxParserLexType.END_ARRAY,JsonStaxParserLexType.END_OBJECT});
	}
	
	
	private JsonStaxParserLexType[] parse(final String parsing) throws IOException {
		final List<JsonStaxParserLexType>	result =  new ArrayList<>();
		
		try(final Reader			rdr = new StringReader(parsing);
			final JsonStaxParser	pars = new JsonStaxParser(rdr)) {
			
			for (JsonStaxParserLexType item : pars) {
				result.add(item);
			}
		}
		return result.toArray(new JsonStaxParserLexType[result.size()]);
	}
}
