package chav1961.purelib.basic;

import java.io.IOException;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.SyntaxException;

@Tag("OrdinalTestCategory")
public class LineByLineProcessorTest {
	private static String[]	SOURCE = new String[]{"123\n456","\n789","\n","A","BC","\nDEF"};
	
	private int 			lineCount;
	private String			result;
	
	@Test
	public void basicTest() throws IOException, SyntaxException {
		lineCount = 0;		result = "";
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement,lineNo,data,from,len)->{lineCount++; result += new String(data,from,len);})) {
			for (String item : SOURCE) {
				final char[]	data = item.toCharArray();
				
				lblp.write(data,0,data.length);
			}
		}
		Assert.assertEquals(lineCount,5);
		Assert.assertEquals(result,"123\n456\n789\nABC\nDEF\n");

		lineCount = 0;		result = "";
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement,lineNo,data,from,len)->{lineCount++; result += new String(data,from,len);});
			final Reader				rdr = new StringReader("123\n456\n789\nABC\nDEF");) {
			lblp.write(rdr);
		}
		Assert.assertEquals(lineCount,5);
		Assert.assertEquals(result,"123\n456\n789\nABC\nDEF\n");
		
		try(final LineByLineProcessor	var = new LineByLineProcessor(null)) {
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc){
		}
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement,lineNo,data,from,len)->{lineCount++; result += new String(data,from,len);})) {
			try{lblp.write(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc){
			}		
			try{lblp.write(null,0,1);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc){
			}		
			try{lblp.write(new char[0],0,1);
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc){
			}		
			try{lblp.write("test".toCharArray(),100,1);
				Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
			} catch (IllegalArgumentException exc){
			}		
			try{lblp.write("test".toCharArray(),1,100);
				Assert.fail("Mandatory exception was not detected (3-st argument out of range)");
			} catch (IllegalArgumentException exc){
			}		
		}
	}

	LineByLineProcessor		lblpExt;
	
	@Test
	public void stackTest() throws IOException, SyntaxException {
		final char[]		line1 = "line1\nline3".toCharArray(), line2 = "line2".toCharArray(), line1a = "line".toCharArray(), line1b = "1\nline3".toCharArray(); 

		lineCount = 0;		result = "";
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement,lineNo,data,from,len)->{
															final String	put = new String(data,from,len);
															
															lineCount++; 
															result += put; 
															if ("line1\n".equals(put)) {
																lblpExt.pushProcessing();
															}
														})) {
			lblpExt = lblp;
			lblp.write(line1,0,line1.length);
			lblp.write(line2,0,line2.length);
			lblp.popProcessing();
			
			try{lblp.popProcessing();
				Assert.fail("Mandatory exception was not detected (stack exhausted)");
			} catch (IllegalStateException exc){
			}
		}
		Assert.assertEquals(lineCount,3);
		Assert.assertEquals(result,"line1\nline2\nline3\n");
		
		lineCount = 0;		result = "";
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement,lineNo,data,from,len)->{
															final String	put = new String(data,from,len);
															
															lineCount++; 
															result += put; 
															if ("line1\n".equals(put)) {
																lblpExt.pushProcessing();
															}
														})) {
			lblpExt = lblp;
			lblp.write(line1a,0,line1a.length);
			lblp.write(line1b,0,line1b.length);
			lblp.write(line2,0,line2.length);
			lblp.popProcessing();
			
			try{lblp.popProcessing();
				Assert.fail("Mandatory exception was not detected (stack exhausted)");
			} catch (IllegalStateException exc){
			}
		}
		Assert.assertEquals(lineCount,3);
		Assert.assertEquals(result,"line1\nline2\nline3\n");		
	}
}
