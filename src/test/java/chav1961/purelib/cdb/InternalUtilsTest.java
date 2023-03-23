package chav1961.purelib.cdb;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.InternalUtils.Lexema;
import chav1961.purelib.cdb.InternalUtils.Lexema.LexType;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;
import chav1961.purelib.cdb.intern.EntityType;
import chav1961.purelib.cdb.intern.Predefines;

public class InternalUtilsTest {
	private static final SyntaxTreeInterface<TestType>	tt = new AndOrTree<>(1,1);
	
	public static enum TestType implements ModuleAccessor {
		Test1, Test2, Test3, Test4;

		@Override
		public void allowUnnamedModuleAccess(Module... unnamedModules) {
			for (Module item : unnamedModules) {
				TestType.class.getModule().addExports(TestType.class.getPackageName(), item);
			}
		}
	}
	
	static {
		for(TestType item : TestType.values()) {
			tt.placeName((CharSequence)item.name(), item.ordinal(), item);
		}
	}
	
	@Test
	@Ignore
	public void lexemaTest() throws SyntaxException {
		final char[]	content = CharUtils.terminateAndConvert2CharArray("([{|}]))*)+:::=@Empty<Test1>'123''4'\r\n", '\uFFFF');
		final int[]		temp = new int[2];
		final Lexema	lex = new Lexema();
		
		int				position = 0;
		
		Assert.assertEquals(1, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Open, lex.type);
		Assert.assertEquals(2, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.OpenB, lex.type);
		Assert.assertEquals(3, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.OpenF, lex.type);
		Assert.assertEquals(4, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Alter, lex.type);
		Assert.assertEquals(5, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.CloseF, lex.type);
		Assert.assertEquals(6, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.CloseB, lex.type);
		Assert.assertEquals(7, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Close, lex.type);
		Assert.assertEquals(9, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Repeat, lex.type);
		Assert.assertEquals(11, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Repeat1, lex.type);
		Assert.assertEquals(12, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Colon, lex.type);
		Assert.assertEquals(15, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Ergo, lex.type);
		
		Assert.assertEquals(21, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Predefined, lex.type);
		Assert.assertEquals(Predefines.Empty, lex.predefine);
		Assert.assertEquals(28, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Name, lex.type);
		Assert.assertEquals(TestType.Test1, tt.getCargo(lex.keyword));
		Assert.assertEquals(33, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Sequence, lex.type);
		Assert.assertArrayEquals("123".toCharArray(), lex.sequence);
		Assert.assertEquals(36, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.Char, lex.type);
		Assert.assertEquals('4', lex.keyword);

		Assert.assertEquals(38, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.NL, lex.type);
		Assert.assertEquals(38, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.EOF, lex.type);
		Assert.assertEquals(38, position = InternalUtils.next(content, position, tt, temp, lex));
		Assert.assertEquals(LexType.EOF, lex.type);
		
		try{InternalUtils.next(CharUtils.terminateAndConvert2CharArray("@Unknown",'\uFFFF'), 0, tt, temp, lex);
			Assert.fail("Mandatory exception was not detected (unknown predefines)");
		} catch (SyntaxException exc) {
		}
		try{InternalUtils.next(CharUtils.terminateAndConvert2CharArray("<Unknown>",'\uFFFF'), 0, tt, temp, lex);
			Assert.fail("Mandatory exception was not detected (unknown node type)");
		} catch (SyntaxException exc) {
		}
		try{InternalUtils.next(CharUtils.terminateAndConvert2CharArray("<Test1",'\uFFFF'), 0, tt, temp, lex);
			Assert.fail("Mandatory exception was not detected (unclosed node type)");
		} catch (SyntaxException exc) {
		}
		try{InternalUtils.next(CharUtils.terminateAndConvert2CharArray("'123",'\uFFFF'), 0, tt, temp, lex);
			Assert.fail("Mandatory exception was not detected (unclosed sequence)");
		} catch (SyntaxException exc) {
		}
		try{InternalUtils.next(CharUtils.terminateAndConvert2CharArray("''",'\uFFFF'), 0, tt, temp, lex);
			Assert.fail("Mandatory exception was not detected (empty sequence)");
		} catch (SyntaxException exc) {
		}
		try{InternalUtils.next(CharUtils.terminateAndConvert2CharArray("?",'\uFFFF'), 0, tt, temp, lex);
			Assert.fail("Mandatory exception was not detected (unknown char)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	@Ignore
	public void parserTest() throws SyntaxException, IOException {
		Assert.assertEquals("<Test1> ::= '1' \r\n", parseAndPrint("<Test1>::='1'"));
		Assert.assertEquals("<Test1> ::= '1' :<Test2> \r\n", parseAndPrint("<Test1>::='1':<Test2>"));
		Assert.assertEquals("<Test1> ::= \"123\" \r\n", parseAndPrint("<Test1>::='123'"));
		Assert.assertEquals("<Test1> ::= \"123\" :<Test2> \r\n", parseAndPrint("<Test1>::='123':<Test2>"));
		Assert.assertEquals("<Test1> ::= <Test2> \r\n", parseAndPrint("<Test1>::=<Test2>"));
		Assert.assertEquals("<Test1> ::= @Empty \r\n", parseAndPrint("<Test1>::=@Empty"));
		Assert.assertEquals("<Test1> ::= <Test2> <Test3> \r\n", parseAndPrint("<Test1>::=<Test2><Test3>"));
		Assert.assertEquals("<Test1> ::= <Test2> [<Test3> ] \r\n", parseAndPrint("<Test1>::=<Test2>[<Test3>]"));
		Assert.assertEquals("<Test1> ::= <Test2> [<Test3> ] <Test4> \r\n", parseAndPrint("<Test1>::=<Test2>[<Test3>]<Test4>"));
		Assert.assertEquals("<Test1> ::= {<Test2> | <Test3> } \r\n", parseAndPrint("<Test1>::={<Test2>|<Test3>}"));
		Assert.assertEquals("<Test1> ::= <Test1> {<Test2> | <Test3> } <Test4> \r\n", parseAndPrint("<Test1>::=<Test1>{<Test2>|<Test3>}<Test4>"));
		Assert.assertEquals("<Test1> ::= <Test2> <Test3> \r\n", parseAndPrint("<Test1>::=(<Test2><Test3>)"));
		Assert.assertEquals("<Test1> ::= (<Test2> <Test3> )* \r\n", parseAndPrint("<Test1>::=(<Test2><Test3>)*"));
		
		try{parseAndPrint("'1'::='2'");
			Assert.fail("Mandatory exception was not detected (missing name at the left)");
		} catch (SyntaxException exc) {
		}
		try{parseAndPrint("<Test1>");
			Assert.fail("Mandatory exception was not detected (missing '::=' in the rule)");
		} catch (SyntaxException exc) {
		}
		try{parseAndPrint("<Test1>::=[<Test2>");
			Assert.fail("Mandatory exception was not detected (missing ']')");
		} catch (SyntaxException exc) { 
		}
		try{parseAndPrint("<Test1>::=(<Test2>");
			Assert.fail("Mandatory exception was not detected (missing ')', ')+' ')*')");
		} catch (SyntaxException exc) {
		}
		try{parseAndPrint("<Test1>::={<Test2>|");
			Assert.fail("Mandatory exception was not detected (missing '}')");
		} catch (SyntaxException exc) {
		}
		try{parseAndPrint("<Test1>::={<Test2>");
			Assert.fail("Mandatory exception was not detected (missing '}')");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void buildTest2Test() throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		test2Test("<Test1>::='1'", " 1 \n", 2, " 2\n", 1);
		test2Test("<Test1>::='11'", " 11 \n", 2, " 12\n", 1);
	}	
	
	
	@Test
	@Ignore
	public void buildTestTest() throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testTest("<Test1>::='1'", " 1 \n", 2, " 2\n", 1);
		testTest("<Test1>::='1':<Test2>", " 1 \n", 2, " 2\n", 1);
		testTest("<Test1>::='123'", " 123 \n", 4, "23\n", 0);
		testTest("<Test1>::='123':<Test2>", " 123 \n", 4, "23\n", 0);
		testTest("<Test1>::='123''4'", " 123 4\n", 6, "123 5\n", 0);
		testTest("<Test1>::='1''234'", " 1 234\n", 6, "123 5\n", 0);
		testTest("<Test1>::=('1''234')", " 1 234\n", 6, "123 5\n", 0);
		testTest("<Test1>::=@FixedNumber@Name", " 12assa\n", 7, " 12 3\n", 0);
		testTest("<Test1>::='1'['2']'3'", " 123\n", 4, " 143\n", 1);
		testTest("<Test1>::='1'['2']'3'", " 13\n", 3, " 143\n", 1);
		testTest("<Test1>::='1'{'2'|'3'|'4'}'5'", " 145\n", 4, " 143\n", 1);
		testTest("<Test1>::='1'{'2'|'3'|'4'}'5'", " 125\n", 4, " 143\n", 1);
		testTest("<Test1>::='1'{'2'|'3'|'4'|@Empty}'5'", " 15\n", 3, " 143\n", 1);
		testTest("<Test1>::=('1'',')*'2'", " 1,1,2\n", 6, " 1,3,2\n", 1);
		testTest("<Test1>::=('1'',')+'2'", " 1,1,2\n", 6, " 3,2\n", 1);
		
		totalTestTest("<Test1>::=<Test2>\n<Test2>::='1'", " 1\n", 2, " 2\n", 1);

		totalTestTest("<Test1>::=<Test2>{'+'|'-'}<Test2>\n<Test2>::='1'", " 1+1\n", 4, " 2\n", 1);
	}	
	
	@Test
	@Ignore
	public void buildSkipTest() throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		skipTest("<Test1>::='1'", " 1 \n", 2, " 2\n", 1);
		skipTest("<Test1>::='1':<Test2>", " 1 \n", 2, " 2\n", 1);
		skipTest("<Test1>::='123'", " 123 \n", 4, "23\n", 0);
		skipTest("<Test1>::='123':<Test2>", " 123 \n", 4, "23\n", 0);
		skipTest("<Test1>::='123''4'", " 123 4\n", 6, "123 5\n", 4);
		skipTest("<Test1>::='1''234'", " 1 234\n", 6, "123 5\n", 1);
		skipTest("<Test1>::=('1''234')", " 1 234\n", 6, "123 5\n", 1);
		skipTest("<Test1>::=@FixedNumber@Name", " 12assa\n", 7, " 12 3\n", 4);
		skipTest("<Test1>::='1'['2']'3'", " 123\n", 4, " 143\n", 2);
		skipTest("<Test1>::='1'['2']'3'", " 13\n", 3, " 143\n", 2);
		skipTest("<Test1>::='1'{'2'|'3'|'4'}'5'", " 145\n", 4, " 143\n", 3);
		skipTest("<Test1>::='1'{'2'|'3'|'4'}'5'", " 125\n", 4, " 143\n", 3);
		skipTest("<Test1>::='1'{'2'|'3'|'4'|@Empty}'5'", " 15\n", 3, " 143\n", 3);
		skipTest("<Test1>::=('1'',')*'2'", " 1,1,2\n", 6, " 1,3,2\n", 3);
		skipTest("<Test1>::=('1'',')+'2'", " 1,1,2\n", 6, " 1,3,2\n", 3);
		
		totalSkipTest("<Test1>::=<Test2>\n<Test2>::='1'", " 1\n", 2, " 2\n", 1);

		totalSkipTest("<Test1>::=<Test2>{'+'|'-'}<Test2>\n<Test2>::='1'", " 1+1\n", 4, " 2\n", 1);
	}	

	@Test
	@Ignore
	public void buildParseTest() throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		SyntaxNode<TestType,SyntaxNode> node;
		
		// Constants
		node = parseTest("<Test1>::='1'", " 1 \n", 2, " 2\n");
		Assert.assertNull(node.type);
		node = parseTest("<Test1>::='123'", " 123 \n", 4, "23\n");
		Assert.assertNull(node.type);
		
		node = parseTest("<Test1>::='1':<Test2>", " 1 \n", 2, " 2\n");
		Assert.assertEquals(TestType.Test2, node.type);
		node = parseTest("<Test1>::='123':<Test2>", " 123 \n", 4, "23\n");
		Assert.assertEquals(TestType.Test2, node.type);
		
		// Predefines
		node = parseTest("<Test1>::=@FixedNumber", " 12\n", 3, " 12 3\n");
		Assert.assertEquals(Predefines.FixedNumber, node.type);
		Assert.assertEquals(12, node.value);

		node = parseTest("<Test1>::=@FloatNumber", " 12\n", 3, " 12 3\n");
		Assert.assertEquals(Predefines.FloatNumber, node.type);
		Assert.assertEquals(12, Double.longBitsToDouble(node.value),0.0001);
		
		node = parseTest("<Test1>::=@Name", " assa\n", 5, " 12 3\n");
		Assert.assertEquals(Predefines.Name, node.type);
		Assert.assertEquals(1, node.value);

		node = parseTest("<Test1>::=@QuotedString", " \'assa\'\n", 7, " 12 3\n");
		Assert.assertEquals(Predefines.QuotedString, node.type);
		Assert.assertEquals("assa", new String((char[])node.cargo));

		node = parseTest("<Test1>::=@DoubleQuotedString", " \"assa\"\n", 7, " 12 3\n");
		Assert.assertEquals(Predefines.DoubleQuotedString, node.type);
		Assert.assertEquals("assa", new String((char[])node.cargo));

		node = parseTest("<Test1>::=@Empty", " \"assa\"\n", 1, " 12 3\n");
		Assert.assertEquals(Predefines.Empty, node.type);
		
		node = parseTest("<Test1>::=@FixedNumber@Name", " 12assa\n", 7, " 12 3\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(Predefines.FixedNumber, node.children[0].type);
		Assert.assertEquals(12, node.children[0].value);
		Assert.assertEquals(Predefines.Name, node.children[1].type);
		Assert.assertEquals(1, node.children[1].value);

		// Options
		node = parseTest("<Test1>::='1':<Test2>['2':<Test3>]'3':<Test4>", " 123\n", 4, " 143\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test3, node.children[1].type);
		Assert.assertEquals(TestType.Test4, node.children[2].type);

		node = parseTest("<Test1>::='1':<Test2>['2':<Test3>]'3':<Test4>", " 13\n", 3, " 143\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test4, node.children[1].type);

		node = parseTest("<Test1>::='1':<Test2>['2':<Test3>'3':<Test1>]'4':<Test4>", " 1234\n", 5, " 143\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test3, node.children[1].type);
		Assert.assertEquals(TestType.Test1, node.children[2].type);
		Assert.assertEquals(TestType.Test4, node.children[3].type);
		
		// Repeats
		node = parseTest("<Test1>::=('1':<Test2>',')+'2':<Test3>", " 1,2\n", 4, " 1,3,2\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test3, node.children[1].type);

		node = parseTest("<Test1>::=('1':<Test2>',')+'2':<Test3>", " 1,1,2\n", 6, " 1,3,2\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(3, node.children.length);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test2, node.children[1].type);
		Assert.assertEquals(TestType.Test3, node.children[2].type);

		try {parseTest("<Test1>::=('1':<Test2>',')+'2':<Test3>", " 2\n", 6, " 1,3,2\n");
			Assert.fail("Mandatory exception was not detected (at least one '1,' required)");
		} catch (SyntaxException exc) {
		}
		
		node = parseTest("<Test1>::=('1':<Test2>',')*'2':<Test3>", " 1,2\n", 4, " 1,3,2\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test3, node.children[1].type);

		node = parseTest("<Test1>::=('1':<Test2>',')*'2':<Test3>", " 1,1,2\n", 6, " 1,3,2\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(3, node.children.length);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test2, node.children[1].type);
		Assert.assertEquals(TestType.Test3, node.children[2].type);
		
		// Alternatives
		node = parseTest("<Test1>::='1':<Test2>{'2':<Test3>|'3':<Test4>|@Empty}'4':<Test1>", " 124\n", 4, " 132\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test3, node.children[1].type);
		Assert.assertEquals(TestType.Test1, node.children[2].type);
	
		node = parseTest("<Test1>::='1':<Test2>{'2':<Test3>|'3':<Test4>|@Empty}'4':<Test1>", " 134\n", 4, " 132\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(TestType.Test4, node.children[1].type);
		Assert.assertEquals(TestType.Test1, node.children[2].type);
		
		node = parseTest("<Test1>::='1':<Test2>{'2':<Test3>|'3':<Test4>|@Empty}'4':<Test1>", " 14\n", 3, " 132\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(TestType.Test2, node.children[0].type);
		Assert.assertEquals(Predefines.Empty, node.children[1].type);
		Assert.assertEquals(TestType.Test1, node.children[2].type);
		
		// Complex test
		node = totalParseTest("<Test1>::=<Test2> <Test3> \r\n<Test2>::='1':<Test4>\r\n<Test3>::='2':<Test1>\r\n", " 12\n", 3, " 13\n");
		Assert.assertEquals(TestType.Test1, node.type);
		Assert.assertEquals(TestType.Test4, node.children[0].type);
		Assert.assertEquals(TestType.Test1, node.children[1].type);
	}	

	
	@Test
	@Ignore
	public void complexTest() throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final SyntaxTreeInterface<Object>				tree = new AndOrTree<>();
		final SyntaxTreeInterface<Object>				names = new AndOrTree<>();
		final RuleBasedParser<TestExpression, Object>	rbp = CompilerUtils.buildRuleBasedParserClass("MyClass", TestExpression.class, Utils.fromResource(this.getClass().getResource("expression.txt")), PureLibSettings.INTERNAL_LOADER, true)
																.getConstructor(Class.class,SyntaxTreeInterface.class).newInstance(TestExpression.class, tree);
		final SyntaxNode<TestExpression,SyntaxNode> 	root = new SyntaxNode<>(0,0,TestExpression.Rule,0,null);
		final String									test = ".not.-2*3+4#-3.or.3.and.4 1";
		final char[]									content = CharUtils.terminateAndConvert2CharArray(test, '\n');
		
//		Assert.assertTrue(rbp.test(content, 0));
		Assert.assertEquals(test.length()-2, rbp.skip(content, 0));
		Assert.assertEquals(1, rbp.parse(content, 0, names, root));
	}	
	
	private String parseAndPrint(final String source) throws SyntaxException, IOException {
		final SyntaxNode<EntityType, SyntaxNode>	root = new SyntaxNode<>(0, 0, EntityType.Root, 0, null);
		final char[]	content = CharUtils.terminateAndConvert2CharArray(source, '\uFFFF'); 
		final int[]		temp = new int[2];
		final Lexema	lex = new Lexema();
		final Writer	wr = new StringWriter();
		
		Assert.assertEquals(content.length-1, InternalUtils.parse(content, InternalUtils.next(content, 0, tt, temp, lex), tt, temp, lex, root));
		InternalUtils.printTree(root, tt, wr);
		return wr.toString();
	}

	private <Cargo> Class<RuleBasedParser<TestType, Cargo>> parseAndBuild(final String source) throws SyntaxException, IOException {
		final SyntaxNode<EntityType, SyntaxNode>	root = new SyntaxNode<>(0, 0, EntityType.Root, 0, null);
		final char[]	content = CharUtils.terminateAndConvert2CharArray(source, '\uFFFF'); 
		final int[]		temp = new int[2];
		final Lexema	lex = new Lexema();
		final Writer	wr = new StringWriter();
		
		Assert.assertEquals(content.length-1, InternalUtils.parse(content, InternalUtils.next(content, 0, tt, temp, lex), tt, temp, lex, root));
		InternalUtils.printTree(root, tt, new PrintWriter(System.err));
		
		
		return InternalUtils.buildRuleBasedParserClass("MyClass"+System.currentTimeMillis(), TestType.class, root, tt, PureLibSettings.INTERNAL_LOADER, true);
	}

	private <Cargo> Class<RuleBasedParser<TestType, Cargo>> parseAndBuild2(final String source) throws SyntaxException, IOException {
		final SyntaxNode<EntityType, SyntaxNode>	root = new SyntaxNode<>(0, 0, EntityType.Root, 0, null);
		final char[]	content = CharUtils.terminateAndConvert2CharArray(source, '\uFFFF'); 
		final int[]		temp = new int[2];
		final Lexema	lex = new Lexema();
		final Writer	wr = new StringWriter();
		
		Assert.assertEquals(content.length-1, InternalUtils.parse(content, InternalUtils.next(content, 0, tt, temp, lex), tt, temp, lex, root));
		InternalUtils.printTree(root, tt, new PrintWriter(System.err));
		
		return InternalUtils.buildRuleBasedParser1("MyClass"+System.currentTimeMillis(), TestType.class, source, PureLibSettings.INTERNAL_LOADER, false, false);
	}
	
	private void testTest(final String rule, final String trueString, final int whereTrue, final String falseString, final int whereFalse) throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		SyntaxTreeInterface<Object>					sti = new AndOrTree<>();
		Class<RuleBasedParser<TestType, Object>>	cl = parseAndBuild(rule);
		RuleBasedParser<TestType, Object>			obj = cl.getConstructor(Class.class, SyntaxTreeInterface.class).newInstance(TestType.class, sti);
		
		Assert.assertTrue(obj.test(trueString.toCharArray(), 0));
		Assert.assertFalse(obj.test(falseString.toCharArray(), 0));
	}

	private void test2Test(final String rule, final String trueString, final int whereTrue, final String falseString, final int whereFalse) throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		SyntaxTreeInterface<Object>					sti = new AndOrTree<>();
		Class<RuleBasedParser<TestType, Object>>	cl = parseAndBuild2(rule);
		RuleBasedParser<TestType, Object>			obj = cl.getConstructor(Class.class, SyntaxTreeInterface.class).newInstance(TestType.class, sti);
		
		Assert.assertTrue(obj.test(trueString.toCharArray(), 0));
		Assert.assertFalse(obj.test(falseString.toCharArray(), 0));
	}
	
	private void totalTestTest(final String rule, final String trueString, final int whereTrue, final String falseString, final int whereFalse) throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		SyntaxTreeInterface<Object>					sti = new AndOrTree<>();
		Class<RuleBasedParser<TestType, Object>>	rbpc = InternalUtils.buildRuleBasedParser("MyClass"+System.currentTimeMillis(), TestType.class, rule, PureLibSettings.INTERNAL_LOADER, true); 
		RuleBasedParser<TestType, Object>			obj = rbpc.getConstructor(Class.class, SyntaxTreeInterface.class).newInstance(TestType.class, sti); 

		Assert.assertTrue(obj.test(trueString.toCharArray(), 0));
		Assert.assertFalse(obj.test(falseString.toCharArray(), 0));
	}
	
	private void skipTest(final String rule, final String trueString, final int whereTrue, final String falseString, final int whereFalse) throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		SyntaxTreeInterface<Object>					sti = new AndOrTree<>();
		Class<RuleBasedParser<TestType, Object>>	cl = parseAndBuild(rule);
		RuleBasedParser<TestType, Object>			obj = cl.getConstructor(Class.class, SyntaxTreeInterface.class).newInstance(TestType.class, sti);
		
		Assert.assertEquals(whereTrue, obj.skip(trueString.toCharArray(), 0));

		try{obj.skip(falseString.toCharArray(), 0);
		} catch (SyntaxException exc) {
			Assert.assertEquals(whereFalse, exc.getCol());
		}
	}

	private void totalSkipTest(final String rule, final String trueString, final int whereTrue, final String falseString, final int whereFalse) throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		SyntaxTreeInterface<Object>					sti = new AndOrTree<>();
		Class<RuleBasedParser<TestType, Object>>	rbpc = InternalUtils.buildRuleBasedParser("MyClass"+System.currentTimeMillis(), TestType.class, rule, PureLibSettings.INTERNAL_LOADER, true); 
		RuleBasedParser<TestType, Object>			obj = rbpc.getConstructor(Class.class, SyntaxTreeInterface.class).newInstance(TestType.class, sti); 

		Assert.assertEquals(whereTrue, obj.skip(trueString.toCharArray(), 0));
		
		try{obj.skip(falseString.toCharArray(), 0);
		} catch (SyntaxException exc) {
			Assert.assertEquals(whereFalse, exc.getCol());
		}
	}

	private SyntaxNode<TestType,SyntaxNode> parseTest(final String rule, final String trueString, final int whereTrue, final String falseString) throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<RuleBasedParser<TestType, Object>>	cl = parseAndBuild(rule);
		RuleBasedParser<TestType, Object>			obj = cl.getConstructor(Class.class, SyntaxTreeInterface.class).newInstance(TestType.class, new AndOrTree<>());
		SyntaxNode<TestType,SyntaxNode>				root = (SyntaxNode<TestType, SyntaxNode>) AbstractBNFParser.TEMPLATE.clone();

		TestType.Test1.allowUnnamedModuleAccess(PureLibSettings.INTERNAL_LOADER.getUnnamedModule());
		root.type = null; // Important!
		Assert.assertEquals(whereTrue, obj.parse(trueString.toCharArray(), 0, root));

//		try{obj.parse(falseString.toCharArray(), 0, root);
//		} catch (SyntaxException exc) {
//		}
		return root;
	}

	private SyntaxNode<TestType,SyntaxNode> totalParseTest(final String rule, final String trueString, final int whereTrue, final String falseString) throws SyntaxException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		SyntaxTreeInterface<Object>					sti = new AndOrTree<>();
		Class<RuleBasedParser<TestType, Object>>	rbpc = InternalUtils.buildRuleBasedParser("MyClass"+System.currentTimeMillis(), TestType.class, rule, PureLibSettings.INTERNAL_LOADER, true); 
		RuleBasedParser<TestType, Object>			obj = rbpc.getConstructor(Class.class, SyntaxTreeInterface.class).newInstance(TestType.class, sti); 
		SyntaxNode<TestType,SyntaxNode>				root = new SyntaxNode<>(0,0,TestType.Test1,0,null);
		
		root.type = null; // Important!
		Assert.assertEquals(whereTrue, obj.parse(trueString.toCharArray(), 0, root));

//		try{obj.parse(falseString.toCharArray(), 0, root);
//		} catch (SyntaxException exc) {
//		}
		return root;
	}
	
	private void printSyntaxNode(final SyntaxNode node) {
		printSyntaxNode("",node);
	}
	
	private void printSyntaxNode(final String prefix, final SyntaxNode node) {
		if (node != null) {
			System.err.print(prefix+">");
			if (node.type != null) {
				System.err.print(node.type);
			}
			if (node.value != 0) {
				System.err.print(" value="+node.value);
			}
			if (node.cargo != null) {
				System.err.print(" cargo="+node.cargo);
			}
			System.err.println();
			if (node.children != null) {
				for (SyntaxNode item : node.children) {
					printSyntaxNode(prefix+"  ", item);
				}
			}
		}
	}
}
