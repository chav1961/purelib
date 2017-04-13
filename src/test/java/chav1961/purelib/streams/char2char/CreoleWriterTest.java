package chav1961.purelib.streams.char2char;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.junit.Test;
import org.junit.Assert;

import chav1961.purelib.basic.Utils;

public class CreoleWriterTest {
	@Test
	public void complexText() throws IOException {
		// Stream markers
		testOneString("plain text","plain text\n"); 
		testOneString("plain text 1\nplain text 2","plain text 1\nplain text 2\n");
		testOneString("plain text\n\nparagraph","plain text\n<p>paragraph\n</p>");
		testOneString("\nparagraph 1\n\nparagraph 2","<p>paragraph 1\n</p><p>paragraph 2\n</p>");
		testOneString(" *~* ~~ escape"," ** ~ escape\n");
		testOneString(" *~*~~escape"," **~escape\n");
		testOneString("simple **bold** //italic// simple","simple <strong>bold</strong> <em>italic</em> simple\n");
		testOneString("simple **bold //italic unclosed","simple <strong>bold <em>italic unclosed\n</strong></em>");
		testOneString("simple * / simple","simple * / simple\n");
		testOneString("simple *** /// simple","simple *** /// simple\n");
		testOneString("line1 ---- line2","line1 <hr/> line2\n");
		testOneString("line1 --- line2","line1 --- line2\n");
		testOneString("line1 ----- line2","line1 ----- line2\n");
		testOneString("line1 \\\\ line2","line1 <br/> line2\n");
		testOneString("line1 \\ line2","line1 \\ line2\n");
		testOneString("line1 \\\\\\ line2","line1 \\\\\\ line2\n");
		
		// Anchored markers
		testOneString("*list1\n*list2\nplain","<ul><li>list1\n</li><li>list2\n</li></ul>plain\n");
		testOneString("#list1\n#list2\nplain","<ol><li>list1\n</li><li>list2\n</li></ol>plain\n");
		testOneString("\n\n*list1\n*list2\nplain","<p></p><p></p><ul><li>list1\n</li><li>list2\n</li></ul>plain\n");
		testOneString("\n\n#list1\n#list2\nplain","<p></p><p></p><ol><li>list1\n</li><li>list2\n</li></ol>plain\n");
		testOneString("*list1\n**list1-1\n**list1-2\n*list2\nplain","<ul><li>list1\n</li><ul><li>list1-1\n</li><li>list1-2\n</li></ul><li>list2\n</li></ul>plain\n");
		testOneString("#list1\n##list1-1\n##list1-2\n#list2\nplain","<ol><li>list1\n</li><ol><li>list1-1\n</li><li>list1-2\n</li></ol><li>list2\n</li></ol>plain\n");
		testOneString("=header1=\nplain","<h1>header1</h1>\nplain\n");
		testOneString("=header1\nunclosed","<h1>header1\n</h1>unclosed\n");
		testOneString("==========header10=====\nplain","<h6>header10</h6>\nplain\n");

		// Links and parser skip
		testOneString("before http://ref after","before <a href=\"http://ref\">http://ref</a> after\n");
		testOneString("before [[http://ref]] after","before <a href=\"http://ref\">http://ref</a> after\n");
		testOneString("before [[http://ref|comment]] after","before <a href=\"http://ref\">comment</a> after\n");
		testOneString("before [[http://ref| **comment** ]] after","before <a href=\"http://ref\"> <strong>comment</strong> </a> after\n");
		testOneString("before [ ] after","before [ ] after\n");
		testOneString("before [[[ ]]] after","before [[[ ]]] after\n");
		testOneString("before [http://ref] after","before [<a href=\"http://ref\">http://ref</a>] after\n");
		testOneString("before {{myName.png}} after","before <img src=\"/img/myName.png\"/> after\n");
		testOneString("before {{myName.png|comment}} after","before <img src=\"/img/myName.png\" alt=\"comment\"/> after\n");
		testOneString("before { } after","before { } after\n");
		testOneString("before {{{{ }}}} after","before {{{{ }}}} after\n");
		testOneString("before {{{**notbold**}}} after","before **notbold** after\n");
		testOneString("before {{{**notbold**\n*notlist}}} after","before **notbold**\n*notlist after\n");

		// Tables
		testOneString("before |=col1|=col2|\n|val1|val2|\n after","before <table><tr><th>col1</th><th>col2</th></tr>\n<tr><td>val1</td><td>val2</td></tr>\n</table> after\n");
		testOneString("before |=col1|=col2|\n|val11|val12|\n|val21|val22|\n after","before <table><tr><th>col1</th><th>col2</th></tr>\n<tr><td>val11</td><td>val12</td></tr>\n<tr><td>val21</td><td>val22</td></tr>\n</table> after\n");
		testOneString("before |val11|val12|\n|val21|val22|\n after","before <table><tr><td>val11</td><td>val12</td></tr>\n<tr><td>val21</td><td>val22</td></tr>\n</table> after\n");
		testOneString("before |=col1|=col2|\n| **val1**|val2|\n after","before <table><tr><th>col1</th><th>col2</th></tr>\n<tr><td> <strong>val1</strong></td><td>val2</td></tr>\n</table> after\n");
	
		// Cutted stream
		testManyStrings("line1\nline2\nline3\n","line1\n","line2\n","line3\n");
		testManyStrings("simple <strong>bold <em>italic unclosed\n</strong></em>","simple **bol","d //italic unclosed");
		testManyStrings("before **notbold**\n*notlist after\n","before {{{**notbold*","*\n*notlist}}} after");
	}
	
	private static void testOneString(final String source, final String target) throws IOException {
		try(final Reader					rdr = new StringReader(source);
			final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			
			try(final Writer				wr = new CreoleWriter(baos)) {
				Utils.copyStream(rdr, wr);	wr.flush();
			}
			Assert.assertEquals(baos.toString(),target);
		}
	}

	private static void testManyStrings(final String target, final String... source) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final Writer				wr = new CreoleWriter(baos)) {
				
				for (String item : source) {
					try(final Reader		rdr = new StringReader(item)) {				
						Utils.copyStream(rdr, wr);	wr.flush();
					}
				}
			}
			Assert.assertEquals(baos.toString(),target);
		}
	}
}
