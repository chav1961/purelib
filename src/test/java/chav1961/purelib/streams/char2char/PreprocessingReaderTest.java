package chav1961.purelib.streams.char2char;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class PreprocessingReaderTest {
	private static final String			BASIC_STRING = "line1\nline2\nline3"; 
	private static final String			DEFINE_AND_IF_STRING = "#define var1 10\n#if ?var1\ntrue\n#else\nfalse\n#endif"; 
	private static final String			DEFINE_AND_IF_STRING_WITH_COMMENT = "#define var1 10  // ?????\n#if ?var1   // ?????\ntrue\n#else\nfalse\n#endif"; 
	private static final String			DEFINE_UNDEFINE_AND_IF_STRING = "#define var1 10\n#undef var1\n#if ?var1\ntrue\n#else\nfalse\n#endif"; 
	private static final String			DEFINE_AND_IF_ELSEIF_STRING = "#define var1 10\n#if var1==\"20\"\n20\n#elseif var1==\"10\"\n10\n#else\n0\n#endif"; 
	private static final String			DEFINE_AND_CALC_STRING = "#define var1 10\n"
																+"#if var1==\"10\"\n==10\n#else\nerror\n#endif\n"
																+"#if var1!=\"11\"\n!=11\n#else\nerror\n#endif\n" 
																+"#if var1>=\"10\"\n>=10\n#else\nerror\n#endif\n"
																+"#if var1>\"09\"\n>09\n#else\nerror\n#endif\n"
																+"#if var1<=\"11\"\n<=11\n#else\nerror\n#endif\n"
																+"#if var1<\"11\"\n<11\n#else\nerror\n#endif\n"
																+"#if var1==10\n==10\n#else\nerror\n#endif\n"
																+"#if var1!=11\n!=11\n#else\nerror\n#endif\n"
																+"#if var1>=10\n>=10\n#else\nerror\n#endif\n"
																+"#if var1>9\n>9\n#else\nerror\n#endif\n"
																+"#if var1<=10\n<=10\n#else\nerror\n#endif\n"
																+"#if var1<11\n<11\n#else\nerror\n#endif\n"
																+"#if ?var1\nexists\n#else\nerror\n#endif\n"
																+"#if ! ? var1\nerror\n#else\nnotexists\n#endif\n"
																+"#if ! var1==20\n!=20\n#else\nerror\n#endif\n"
																+"#if var1>9 && var1 < 11\n9..11\n#else\nerror\n#endif\n"
																+"#if var1==-5 || var1 == 10\n-5..10\n#else\nerror\n#endif\n"
																+"#if (var1==10)\n==10\n#else\nerror\n#endif\n"
																+"#define var1 abcde\n#if var1==10\nerror\n#else\nabcde!=10\n#endif\n";
	private static final String			DEFINE_AND_CALC_RESULT = "==10\n"
																+ "!=11\n"
																+ ">=10\n"
																+ ">09\n"
																+ "<=11\n"
																+ "<11\n"
																+ "==10\n"
																+ "!=11\n"
																+ ">=10\n"
																+ ">9\n"
																+ "<=10\n"
																+ "<11\n"
																+ "exists\n"
																+ "notexists\n"
																+ "!=20\n"
																+ "9..11\n"
																+ "-5..10\n"
																+ "==10\n"
																+ "abcde!=10\n";
	private static final String			ERROR_AND_WARN_STRING = "#error error message\n#warning warning message\n#error\n#warning"; 
	private static final String			ERROR_AND_WARN_RESULT = "error message\nwarning message\n\n\n"; 
	private static final String			INCLUDE_STRING1 = "line1\n#include \""+PreprocessingReaderTest.class.getResource("include.txt")+"\"\nline3"; 
	private static final String			INCLUDE_STRING2 = "line1\n#include <"+PreprocessingReaderTest.class.getResource("include.txt")+">\nline3"; 
	private static final String			INCLUDE_STRING3 = "line1\n#include "+PreprocessingReaderTest.class.getResource("include.txt")+"\nline3"; 
	private static final String			INCLUDE_RESULT = "line1\ninclude line 1\nline3\n"; 
	private static final String			SUBSTITUTE_STRING = "line1\n#define x 10\nx inside x inside x\nline3"; 
	private static final String			SUBSTITUTE_RESULT = "line1\n10 inside 10 inside 10\nline3\n"; 
//	private static final String			SUBSTITUTE_NESTED_STRING = "line1\n#define x y\n#define y 10\nx inside x inside x\nline3"; 
//	private static final String			SUBSTITUTE_NESTED_RESULT = "line1\n10 inside 10 inside 10\nline3\n"; 

	@Test
	public void basicTest() throws IOException {
		final StringBuilder	sb = new StringBuilder();

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(BASIC_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr)) {
			int				content;
				
			while ((content = ppr.read()) >= 0) {
				sb.append((char)content);
			}
		}
		Assert.assertEquals(sb.toString(),BASIC_STRING+'\n');

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(BASIC_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,Utils.mkMap(PreprocessingReader.BUFFER_SIZE,10))) {
			int				content;
				
			while ((content = ppr.read()) >= 0) {
				sb.append((char)content);
			}
		}
		Assert.assertEquals(sb.toString(),BASIC_STRING+'\n');
		
		sb.setLength(0);
		try(final Reader	rdr = new StringReader(BASIC_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr);
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),BASIC_STRING+'\n');
		
		try(final Reader rdr = new PreprocessingReader(null)) {
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try(final Reader rdr = new PreprocessingReader(new StringReader(""),null)) {
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try(final Reader rdr = new PreprocessingReader(new StringReader(""),new HashMap<>(),null)) {
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		
		try(final Reader	rdr = new StringReader(BASIC_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr)) {
			final char[]	buffer = new char[2];

			try{ppr.read(null,0,1);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{ppr.read(buffer,2,1);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{ppr.read(buffer,0,5);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}

	@Test
	public void conditionalTest() throws IOException {
		final StringBuilder	sb = new StringBuilder();
		
		sb.setLength(0);
		try(final Reader	rdr = new StringReader(DEFINE_AND_IF_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr);
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),"true\n");
		
		sb.setLength(0);
		try(final Reader	rdr = new StringReader(DEFINE_UNDEFINE_AND_IF_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr);
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),"false\n");

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(DEFINE_AND_IF_ELSEIF_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr);
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),"10\n");

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(DEFINE_AND_CALC_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,Utils.mkMap(PreprocessingReader.IGNORE_CASE,true));
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),DEFINE_AND_CALC_RESULT);
		
		testSyntax("#mzinana","unknown command");
		testSyntax("#endif","unpaired #endif");
		testSyntax("#else\nerror\n#endif","unpaired #else");
		testSyntax("#elseif x == 0\nerror\n#endif","unpaired #elseif");

		testSyntax("#define var 10\n#if var\nerror\n#endif","missing comparison operator");
		testSyntax("#define var 10\n#if var =\nerror\n#endif","unknown comparison operator");
		testSyntax("#define var 10\n#if var !\nerror\n#endif","unknown comparison operator");
		testSyntax("#define var 10\n#if var == \nerror\n#endif","missing second operand");
		testSyntax("#define var 10\n#if var == var2\nerror\n#endif","unsupported second operand");
		testSyntax("#define var 10\n#if var == \"\nerror\n#endif","unpaired string quotas");
		testSyntax("#define var 10\n#if == 10\nerror\n#endif","missing first operand");
		testSyntax("#define var 10\n#if (var == 10\nerror\n#endif","missing close bracket");
		testSyntax("#define var 10\n#if var == 10)\nerror\n#endif","garbage in the tail");
	}

	@Test
	public void commentTest() throws IOException {
		final StringBuilder	sb = new StringBuilder();

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(DEFINE_AND_IF_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,
												Utils.mkMap(PreprocessingReader.HIDING_METHOD,PreprocessingReader.HidingMethod.SINGLE_LINE_COMMENTED
														   ,PreprocessingReader.COMMENT_SEQUENCE, "//\n/*\t*/"
												));
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),"true\n//false\n");
		
		sb.setLength(0);
		try(final Reader	rdr = new StringReader(DEFINE_AND_IF_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,
												Utils.mkMap(PreprocessingReader.HIDING_METHOD,PreprocessingReader.HidingMethod.MULTILINE_COMMENTED
														   ,PreprocessingReader.COMMENT_SEQUENCE, "//\n/*\t*/"
												));
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),"true\n/*false\n*/\n");

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(DEFINE_AND_IF_STRING_WITH_COMMENT);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,
												Utils.mkMap(PreprocessingReader.HIDING_METHOD,PreprocessingReader.HidingMethod.MULTILINE_COMMENTED
														   ,PreprocessingReader.COMMENT_SEQUENCE, "//\n/*\t*/"
												));
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),"true\n/*false\n*/\n");
		
		
		try(final Reader rdr = new PreprocessingReader(new StringReader(""),
												Utils.mkMap(PreprocessingReader.HIDING_METHOD,PreprocessingReader.HidingMethod.SINGLE_LINE_COMMENTED
												))) {
			Assert.fail("Mandatory exception was not detected (mandatory key PreprocessingReader.COMMENT_SEQUENCE is required for PreprocessingReader.HIDING_METHOD)");
		} catch (IllegalArgumentException exc) {
		}
		
		try(final Reader rdr = new PreprocessingReader(new StringReader(""),
												Utils.mkMap(PreprocessingReader.HIDING_METHOD,PreprocessingReader.HidingMethod.MULTILINE_COMMENTED
												))) {
			Assert.fail("Mandatory exception was not detected (mandatory key PreprocessingReader.COMMENT_SEQUENCE is required for PreprocessingReader.HIDING_METHOD)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void warnAndErrTest() throws IOException {
		final StringBuilder	sb = new StringBuilder();

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(ERROR_AND_WARN_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,
												Utils.mkMap(PreprocessingReader.ERROR_PROCESSING_CALLBACK,
														new PreprocessingReader.ErrorProcessingCallback() {
															@Override
															public void processWarning(final int line, final String sourceStream, final String message) throws SyntaxException {
																sb.append(message).append("\n");
															}
															
															@Override
															public void processError(final int line, final String sourceStream, final String message) throws SyntaxException {
																sb.append(message).append("\n");
															}
														}
												));
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(ERROR_AND_WARN_RESULT.trim(),sb.toString().trim());
	}

	@Test
	public void includeTest() throws IOException {
		final StringBuilder	sb = new StringBuilder();

		for (String item : new String[]{INCLUDE_STRING1, INCLUDE_STRING2, INCLUDE_STRING3}) {
			sb.setLength(0);
			try(final Reader	rdr = new StringReader(item);
				final PreprocessingReader	ppr = new PreprocessingReader(rdr);
				final BufferedReader		brdr = new BufferedReader(ppr)) {
				
				String	line;
				
				while ((line = brdr.readLine()) != null) {
					sb.append(line).append('\n');
				}
			}
			Assert.assertEquals(sb.toString(),INCLUDE_RESULT);
		}
	}
	
	@Test
	public void substitutionTest() throws IOException {
		final StringBuilder	sb = new StringBuilder();

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(SUBSTITUTE_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,
												Utils.mkMap(PreprocessingReader.INLINE_SUBSTITUTION,true
										));
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),SUBSTITUTE_RESULT);

		sb.setLength(0);
		try(final Reader	rdr = new StringReader(SUBSTITUTE_STRING);
			final PreprocessingReader	ppr = new PreprocessingReader(rdr,
												Utils.mkMap(PreprocessingReader.INLINE_SUBSTITUTION,true,
															PreprocessingReader.RECURSIVE_SUBSTITUTION,true
										));
			final BufferedReader		brdr = new BufferedReader(ppr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		Assert.assertEquals(sb.toString(),SUBSTITUTE_RESULT);
	}
	
	private void testSyntax(final String content, final String problem) {
		try{try(final Reader	rdr = new StringReader(content);
				final PreprocessingReader	ppr = new PreprocessingReader(rdr);) {
				
				Utils.copyStream(ppr,new StringWriter());
			}
			Assert.fail("Mandatory exception was not detected (syntax error in the content: "+problem+")");
		} catch (IOException exc) {			
		}
	}
}
