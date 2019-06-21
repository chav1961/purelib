package chav1961.purelib.javadoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.javadoc.DocletUtils;
import chav1961.purelib.javadoc.DocletUtils.MultilangContent;

public class DocletUtilsTest {
	@Test
	public void actionsTest() {
		final SyntaxTreeInterface<char[]>	imports = new AndOrTree<>();
		final StringBuilder					sb = new StringBuilder();
		char[]		string;

		imports.placeName("test", "class/test/Test".toCharArray());
		
		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("lt")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("<",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("gt")).process(sb,string,0,string.length,imports);
		Assert.assertEquals(">",sb.toString());
		
		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("nbsp")).process(sb,string,0,string.length,imports);
		Assert.assertEquals(" ",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("amp")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("&",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("p")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("/p")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("b")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("**",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("b")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("**",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("i")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("//",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("/i")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("//",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("li")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n* ",sb.toString());

		string = "".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("/li")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n",sb.toString());

		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("about")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());
		
		string = " test\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("author")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Author:// test\n",sb.toString());

		string = " description\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("beta")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//**Beta:**// description\n",sb.toString());

		string = " description".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("code")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("{{{ description}}}",sb.toString());

		string = " test".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("codeSample")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Code samples(s)://  [[class/test/Test|test]]\n",sb.toString());
		
		string = " test\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("deprecated")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Deprecated:// test\n",sb.toString());

		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("docRoot")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());

		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("inheritDoc")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());
		
		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("keyWords")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());
		
		string = " 1.2.3\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("lastUpdated")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Last updated:// 1.2.3\n",sb.toString());

		string = " test test".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("link")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("[[class/test/Test| test]]",sb.toString());
		
		string = " name description\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("param")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n* **name** description\n",sb.toString());

		string = " description\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("return")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Returns:// description\n",sb.toString());

		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("serial")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());

		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("serialData")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());

		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("serialField")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("",sb.toString());
		
		string = " 123 test 123\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("see")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//See also://  [[123|123]] [[class/test/Test|test]] [[123|123]]\n",sb.toString());

		string = " 1.2.3\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("since")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Since:// 1.2.3\n",sb.toString());

		string = " description\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("threadSafed")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Thread safed:// description\n",sb.toString());

		string = " test description\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("throws")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n* [[class/test/Test|test]] :  description\n",sb.toString());
		
		string = " 1.2.3\n".toCharArray();
		sb.setLength(0);	DocletUtils.ACTIONS.getCargo(DocletUtils.ACTIONS.seekName("version")).process(sb,string,0,string.length,imports);
		Assert.assertEquals("\n\n//Version:// 1.2.3\n",sb.toString());
	}

	@Test
	public void lineParserTest() {
		final StringBuilder					sb = new StringBuilder();
		final List<MultilangContent>		content = new ArrayList<>();
		final SyntaxTreeInterface<char[]>	imports = new AndOrTree<>();
		char[]		string;

		imports.placeName("test", "class/test/Test".toCharArray());

		string = "\n".toCharArray();
		sb.setLength(0);	DocletUtils.parseInternal(0,string,0,string.length,sb,imports,DocletUtils.ACTIONS,content);
		Assert.assertEquals("\n",sb.toString());

		string = "<p>123</p>\n".toCharArray();
		sb.setLength(0);	DocletUtils.parseInternal(0,string,0,string.length,sb,imports,DocletUtils.ACTIONS,content);
		Assert.assertEquals("123\n\n\n",sb.toString());

		string = "&lt;&gt;&nbsp;&amp;\n".toCharArray();
		sb.setLength(0);	DocletUtils.parseInternal(0,string,0,string.length,sb,imports,DocletUtils.ACTIONS,content);
		Assert.assertEquals("<> &\n",sb.toString());

		string = "@see 123 test 123\n".toCharArray();
		sb.setLength(0);	DocletUtils.parseInternal(0,string,0,string.length,sb,imports,DocletUtils.ACTIONS,content);
		Assert.assertEquals("\n\n//See also://  [[123|123]] [[class/test/Test|test]] [[123|123]]\n",sb.toString());

		string = "{@link test}".toCharArray();
		sb.setLength(0);	DocletUtils.parseInternal(0,string,0,string.length,sb,imports,DocletUtils.ACTIONS,content);
		Assert.assertEquals("[[class/test/Test|test]]",sb.toString());
	}

	@Test
	public void javadoc2CreoleTest() throws SyntaxException, IOException {
		final SyntaxTreeInterface<char[]>	imports = new AndOrTree<>();

		imports.placeName("test", "class/test/Test".toCharArray());
		
		Assert.assertEquals("See [[class/test/Test|test]]\n\n\n",new String(DocletUtils.javadoc2Creole("<p>See {@link test}</p>",imports)[0].content));
		Assert.assertEquals("",new String(DocletUtils.javadoc2Creole("",imports)[0].content));
		
		try{DocletUtils.javadoc2Creole(null,imports);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{DocletUtils.javadoc2Creole("<p>See {@link test}</p>",null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}
	}
}
