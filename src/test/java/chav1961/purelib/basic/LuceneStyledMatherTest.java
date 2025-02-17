package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class LuceneStyledMatherTest {

	@Test
	public void parserTest() throws SyntaxException {
		final List<LuceneStyledMatcher.Lexema>	lex = new ArrayList<>();
		
		LuceneStyledMatcher.parse((""+LuceneStyledMatcher.EOF).toCharArray(), 0, lex);
	}

	
}
