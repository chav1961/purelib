package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.LuceneStyledMatcher.Lemma;
import chav1961.purelib.basic.LuceneStyledMatcher.LemmaType;
import chav1961.purelib.basic.LuceneStyledMatcher.NodeType;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;

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

	@Test
	public void builderTest() throws SyntaxException {
		SyntaxNode<NodeType, SyntaxNode<?, ?>>	root = buildTree("assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].cargo instanceof char[]);

		root = buildTree("\"assa\"");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.PHRASE_EQUALS, root.children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].cargo instanceof char[]);

		root = buildTree("200");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].cargo instanceof Integer);

		root = buildTree("200.0");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].cargo instanceof Float);

		root = buildTree("ass*a");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.MATCHES, root.children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].cargo instanceof char[]);

		root = buildTree("test:assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].type);
		Assert.assertEquals("test", root.children[0].cargo);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].cargo instanceof char[]);

		root = buildTree("assa~");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.FUSSY_MATCHES, root.children[0].type);
		Assert.assertEquals(1.0f, Float.intBitsToFloat((int)root.children[0].value), 0.001f);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);

		root = buildTree("assa~0.5");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.FUSSY_MATCHES, root.children[0].type);
		Assert.assertEquals(0.5f, Float.intBitsToFloat((int)root.children[0].value), 0.001f);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);

		root = buildTree("assa~10");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.PROXIMITY_MATCHES, root.children[0].type);
		Assert.assertEquals(10, root.children[0].value);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);

		root = buildTree("assa^10");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.BOOST, root.children[0].type);
		Assert.assertEquals(10, root.children[0].value);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);

		root = buildTree("+assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.MANDATORY, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);

		root = buildTree("-assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.PROHIBITED, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);

		root = buildTree("assa 1 3.5");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].cargo instanceof char[]);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[1].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[1].children[0].type);
		Assert.assertTrue(root.children[1].children[0].cargo instanceof Integer);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[2].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[2].children[0].type);
		Assert.assertTrue(root.children[2].children[0].cargo instanceof Float);

		root = buildTree("[assa to assa}");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.RANGE_MATCHES, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[1].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[1].children[0].type);
		Assert.assertTrue(root.children[0].children[1].children[0].cargo instanceof char[]);

		root = buildTree("not assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.NOT, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);

		root = buildTree("assa and assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.AND, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[1].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[1].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[1].children[0].children[0].type);
		Assert.assertTrue(root.children[1].children[0].children[0].cargo instanceof char[]);

		root = buildTree("assa or assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.OR, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[0].children[0].children[0].type);
		Assert.assertTrue(root.children[0].children[0].children[0].cargo instanceof char[]);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[1].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[1].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[1].children[0].children[0].type);
		Assert.assertTrue(root.children[1].children[0].children[0].cargo instanceof char[]);

		root = buildTree("(assa mama) or assa");
		
		Assert.assertEquals(LuceneStyledMatcher.NodeType.OR, root.type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[0].children[0].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.SEQUENCE, root.children[1].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EXTRACT, root.children[1].children[0].type);
		Assert.assertEquals(LuceneStyledMatcher.NodeType.EQUALS, root.children[1].children[0].children[0].type);
		Assert.assertTrue(root.children[1].children[0].children[0].cargo instanceof char[]);
	
		try {
			buildTree("(assa"); 
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
		}
		try {
			buildTree("[assa assa]"); 
			Assert.fail("Mandatory exception was not detected (missing TO)");
		} catch (SyntaxException exc) {
		}
		try {
			buildTree("[assa TO assa");  
			Assert.fail("Mandatory exception was not detected (missing close bracket)");
		} catch (SyntaxException exc) {
		}
		try {
			buildTree("assa^3.5");  
			Assert.fail("Mandatory exception was not detected (missing integer)");
		} catch (SyntaxException exc) {
		}
		try {
			buildTree("test:");  
			Assert.fail("Mandatory exception was not detected (missing oeprand)");
		} catch (SyntaxException exc) {
		}
	}	

	@Test
	public void lemmatizerTest() throws SyntaxException {
		Lemma[]	lemmas = LuceneStyledMatcher.lemmatize("vassya pupkin, go home! 12-00?");
	 	
		Assert.assertEquals(11, lemmas.length);
		Assert.assertEquals(LemmaType.WORD, lemmas[0].type);
		Assert.assertArrayEquals("vassya".toCharArray(), lemmas[0].valueS);
		Assert.assertEquals(LemmaType.WORD, lemmas[1].type);
		Assert.assertArrayEquals("pupkin".toCharArray(), lemmas[1].valueS);
		Assert.assertEquals(LemmaType.PUNCT, lemmas[2].type);
		Assert.assertArrayEquals(",".toCharArray(), lemmas[2].valueS);
		Assert.assertEquals(LemmaType.WORD, lemmas[3].type);
		Assert.assertArrayEquals("go".toCharArray(), lemmas[3].valueS);
		Assert.assertEquals(LemmaType.WORD, lemmas[4].type);
		Assert.assertArrayEquals("home".toCharArray(), lemmas[4].valueS);
		Assert.assertEquals(LemmaType.PUNCT, lemmas[5].type);
		Assert.assertArrayEquals("!".toCharArray(), lemmas[5].valueS);
		Assert.assertEquals(LemmaType.INTEGER, lemmas[6].type);
		Assert.assertEquals(12, lemmas[6].valueN);
		Assert.assertEquals(LemmaType.PUNCT, lemmas[7].type);
		Assert.assertArrayEquals("-".toCharArray(), lemmas[7].valueS);
		Assert.assertEquals(LemmaType.INTEGER, lemmas[8].type);
		Assert.assertEquals(0, lemmas[8].valueN);
		Assert.assertEquals(LemmaType.PUNCT, lemmas[9].type);
		Assert.assertArrayEquals("?".toCharArray(), lemmas[9].valueS);
		Assert.assertEquals(LemmaType.EOF, lemmas[10].type);
	}	
	
	@Test
	public void matcherTest() throws SyntaxException {
		Assert.assertTrue(execute("assa", "assa"));
		Assert.assertFalse(execute("asas", "assa"));
	}
	
	private SyntaxNode<NodeType, SyntaxNode<?, ?>> buildTree(final String expr) throws SyntaxException {
		final List<LuceneStyledMatcher.Lexema>	lex = new ArrayList<>();
		
		LuceneStyledMatcher.parse(CharUtils.terminateAndConvert2CharArray(expr, LuceneStyledMatcher.EOF), 0, lex);
		final LuceneStyledMatcher.Lexema[]		lexList = lex.toArray(new LuceneStyledMatcher.Lexema[lex.size()]);
		final SyntaxNode<NodeType, SyntaxNode<?, ?>>	root = new SyntaxNode<>(0, 0, NodeType.ROOT, 0, null);
		
		Assert.assertEquals(lexList.length - 1, LuceneStyledMatcher.buildTree(lexList, 0, root));
		return root;
	}

	private boolean execute(final String expr, final String source) throws SyntaxException {
		return LuceneStyledMatcher.match(buildTree(expr), source);
	}
}
