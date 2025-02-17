package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class LuceneStyledMatherTest {

	@Test
	public void parserTest() throws SyntaxException {
		final List<LuceneStyledMatcher.Lexema>	lex = new ArrayList<>();
		
		LuceneStyledMatcher.parse((": ~ ( ) [ ] { } + - ^ \"assa\" ass\\*a a?* 10 3.5 to and or not"+LuceneStyledMatcher.EOF).toCharArray(), 0, lex);
		Assert.assertEquals(21, lex.size());
		Assert.assertEquals(LuceneStyledMatcher.LexType.COLON, lex.get(0).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.FUZZY, lex.get(1).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.OPEN, lex.get(2).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.CLOSE, lex.get(3).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.OPENB, lex.get(4).type); 
		Assert.assertEquals(LuceneStyledMatcher.LexType.CLOSEB, lex.get(5).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.OPENF, lex.get(6).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.CLOSEF, lex.get(7).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.PLUS, lex.get(8).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.MINUS, lex.get(9).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.BOOST, lex.get(10).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.PHRASE, lex.get(11).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.SINGLE_TERM, lex.get(12).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.WILDCARD_TERM, lex.get(13).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.INTEGER, lex.get(14).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.FLOAT, lex.get(15).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.TO_WORD, lex.get(16).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.AND_WORD, lex.get(17).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.OR_WORD, lex.get(18).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.NOT_WORD, lex.get(19).type);
		Assert.assertEquals(LuceneStyledMatcher.LexType.EOF, lex.get(20).type);
		
		try {
			LuceneStyledMatcher.parse(("?"+LuceneStyledMatcher.EOF).toCharArray(), 0, lex);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
		try {
			LuceneStyledMatcher.parse(("\""+LuceneStyledMatcher.EOF).toCharArray(), 0, lex);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
		try {
			LuceneStyledMatcher.parse(("\\"+LuceneStyledMatcher.EOF).toCharArray(), 0, lex);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
	}

	
}
