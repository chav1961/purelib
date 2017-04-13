package chav1961.purelib.basic;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class LineByLineProcessorTest {
	private static String[]	SOURCE = new String[]{"123\n456","\n789","\n","A","BC","\nDEF"};
	
	private int 			lineCount;
	private String			result;
	
	@Test
	public void basicTest() throws IOException, SyntaxException {
		lineCount = 0;		result = "";
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,len)->{lineCount++; result += new String(data,from,len);})) {
			for (String item : SOURCE) {
				final char[]	data = item.toCharArray();
				
				lblp.write(data,0,data.length);
			}
		}
		Assert.assertEquals(lineCount,5);
		Assert.assertEquals(result,"123\n456\n789\nABC\nDEF\n");

		lineCount = 0;		result = "";
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,len)->{lineCount++; result += new String(data,from,len);});
			final Reader				rdr = new StringReader("123\n456\n789\nABC\nDEF");) {
			lblp.write(rdr);
		}
		Assert.assertEquals(lineCount,5);
		Assert.assertEquals(result,"123\n456\n789\nABC\nDEF\n");
		
		try{new LineByLineProcessor(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc){
		}
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,len)->{lineCount++; result += new String(data,from,len);})) {
			try{lblp.write(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc){
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
}
