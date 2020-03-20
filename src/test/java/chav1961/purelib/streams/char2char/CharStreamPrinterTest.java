package chav1961.purelib.streams.char2char;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.CharStreamPrinter;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class CharStreamPrinterTest {
	private interface Call {
		void process(CharStreamPrinter<?> printer) throws PrintingException;
	}

	@Test
	public void writerTest() throws NullPointerException, PrintingException, IOException {
		final String	tail = new String(PrintWriterWrapper.CRNL); 
		
		Assert.assertEquals(tail,processWriterTest((wr)->{wr.println();}));
		
		Assert.assertEquals("a",processWriterTest((wr)->{wr.print('a');}));
		Assert.assertEquals("a"+tail,processWriterTest((wr)->{wr.println('a');}));
		
		Assert.assertEquals("10",processWriterTest((wr)->{wr.print((byte)10);}));
		Assert.assertEquals("10"+tail,processWriterTest((wr)->{wr.println((byte)10);}));

		Assert.assertEquals("10",processWriterTest((wr)->{wr.print((short)10);}));
		Assert.assertEquals("10"+tail,processWriterTest((wr)->{wr.println((short)10);}));

		Assert.assertEquals("10",processWriterTest((wr)->{wr.print((int)10);}));
		Assert.assertEquals("10"+tail,processWriterTest((wr)->{wr.println((int)10);}));

		Assert.assertEquals("10",processWriterTest((wr)->{wr.print((long)10);}));
		Assert.assertEquals("10"+tail,processWriterTest((wr)->{wr.println((long)10);}));

		Assert.assertEquals("12.5",processWriterTest((wr)->{wr.print((float)12.5);}));
		Assert.assertEquals("12.5"+tail,processWriterTest((wr)->{wr.println((float)12.5);}));

		Assert.assertEquals("12.5",processWriterTest((wr)->{wr.print((double)12.5);}));
		Assert.assertEquals("12.5"+tail,processWriterTest((wr)->{wr.println((double)12.5);}));

		Assert.assertEquals("true",processWriterTest((wr)->{wr.print(true);}));
		Assert.assertEquals("false"+tail,processWriterTest((wr)->{wr.println(false);}));

		Assert.assertEquals("test",processWriterTest((wr)->{wr.print("test");}));
		Assert.assertEquals("test"+tail,processWriterTest((wr)->{wr.println("test");}));
		Assert.assertEquals("null",processWriterTest((wr)->{wr.print((String)null);}));
		Assert.assertEquals("null"+tail,processWriterTest((wr)->{wr.println((String)null);}));

		Assert.assertEquals("te",processWriterTest((wr)->{wr.print("test",0,2);}));
		Assert.assertEquals("te"+tail,processWriterTest((wr)->{wr.println("test",0,2);}));
		Assert.assertEquals("null",processWriterTest((wr)->{wr.print((String)null,0,2);}));
		Assert.assertEquals("null"+tail,processWriterTest((wr)->{wr.println((String)null,0,2);}));

		Assert.assertEquals("test",processWriterTest((wr)->{wr.print("test".toCharArray());}));
		Assert.assertEquals("test"+tail,processWriterTest((wr)->{wr.println("test".toCharArray());}));
		Assert.assertEquals("null",processWriterTest((wr)->{wr.print((char[])null);}));
		Assert.assertEquals("null"+tail,processWriterTest((wr)->{wr.println((char[])null);}));

		Assert.assertEquals("te",processWriterTest((wr)->{wr.print("test".toCharArray(),0,2);}));
		Assert.assertEquals("te"+tail,processWriterTest((wr)->{wr.println("test".toCharArray(),0,2);}));
		Assert.assertEquals("null",processWriterTest((wr)->{wr.print((char[])null,0,2);}));
		Assert.assertEquals("null"+tail,processWriterTest((wr)->{wr.println((char[])null,0,2);}));

		Assert.assertEquals("10",processWriterTest((wr)->{wr.print(Integer.valueOf(10));}));
		Assert.assertEquals("10"+tail,processWriterTest((wr)->{wr.println(Integer.valueOf(10));}));
		Assert.assertEquals("null",processWriterTest((wr)->{wr.print((Object)null);}));
		Assert.assertEquals("null"+tail,processWriterTest((wr)->{wr.println((Object)null);}));
	}

	@Test
	public void StreamTest() throws NullPointerException, PrintingException, IOException {
		final String	tail = new String(PrintWriterWrapper.CRNL); 
		
		Assert.assertEquals(tail,processStreamTest((wr)->{wr.println();}));
		
		Assert.assertEquals("a",processStreamTest((wr)->{wr.print('a');}));
		Assert.assertEquals("a"+tail,processStreamTest((wr)->{wr.println('a');}));
		
		Assert.assertEquals("10",processStreamTest((wr)->{wr.print((byte)10);}));
		Assert.assertEquals("10"+tail,processStreamTest((wr)->{wr.println((byte)10);}));

		Assert.assertEquals("10",processStreamTest((wr)->{wr.print((short)10);}));
		Assert.assertEquals("10"+tail,processStreamTest((wr)->{wr.println((short)10);}));

		Assert.assertEquals("10",processStreamTest((wr)->{wr.print((int)10);}));
		Assert.assertEquals("10"+tail,processStreamTest((wr)->{wr.println((int)10);}));

		Assert.assertEquals("10",processStreamTest((wr)->{wr.print((long)10);}));
		Assert.assertEquals("10"+tail,processStreamTest((wr)->{wr.println((long)10);}));

		Assert.assertEquals("12.5",processStreamTest((wr)->{wr.print((float)12.5);}));
		Assert.assertEquals("12.5"+tail,processStreamTest((wr)->{wr.println((float)12.5);}));

		Assert.assertEquals("12.5",processStreamTest((wr)->{wr.print((double)12.5);}));
		Assert.assertEquals("12.5"+tail,processStreamTest((wr)->{wr.println((double)12.5);}));

		Assert.assertEquals("true",processStreamTest((wr)->{wr.print(true);}));
		Assert.assertEquals("false"+tail,processStreamTest((wr)->{wr.println(false);}));

		Assert.assertEquals("test",processStreamTest((wr)->{wr.print("test");}));
		Assert.assertEquals("test"+tail,processStreamTest((wr)->{wr.println("test");}));
		Assert.assertEquals("null",processStreamTest((wr)->{wr.print((String)null);}));
		Assert.assertEquals("null"+tail,processStreamTest((wr)->{wr.println((String)null);}));

		Assert.assertEquals("te",processStreamTest((wr)->{wr.print("test",0,2);}));
		Assert.assertEquals("te"+tail,processStreamTest((wr)->{wr.println("test",0,2);}));
		Assert.assertEquals("null",processStreamTest((wr)->{wr.print((String)null,0,2);}));
		Assert.assertEquals("null"+tail,processStreamTest((wr)->{wr.println((String)null,0,2);}));

		Assert.assertEquals("test",processStreamTest((wr)->{wr.print("test".toCharArray());}));
		Assert.assertEquals("test"+tail,processStreamTest((wr)->{wr.println("test".toCharArray());}));
		Assert.assertEquals("null",processStreamTest((wr)->{wr.print((char[])null);}));
		Assert.assertEquals("null"+tail,processStreamTest((wr)->{wr.println((char[])null);}));

		Assert.assertEquals("te",processStreamTest((wr)->{wr.print("test".toCharArray(),0,2);}));
		Assert.assertEquals("te"+tail,processStreamTest((wr)->{wr.println("test".toCharArray(),0,2);}));
		Assert.assertEquals("null",processStreamTest((wr)->{wr.print((char[])null,0,2);}));
		Assert.assertEquals("null"+tail,processStreamTest((wr)->{wr.println((char[])null,0,2);}));

		Assert.assertEquals("10",processStreamTest((wr)->{wr.print(Integer.valueOf(10));}));
		Assert.assertEquals("10"+tail,processStreamTest((wr)->{wr.println(Integer.valueOf(10));}));
		Assert.assertEquals("null",processStreamTest((wr)->{wr.print((Object)null);}));
		Assert.assertEquals("null"+tail,processStreamTest((wr)->{wr.println((Object)null);}));
	}
	
	private String processWriterTest(Call call) throws NullPointerException, IOException, PrintingException {
		try(final Writer	wr = new StringWriter();
			final CharStreamPrinter<PrintWriterWrapper>	wrapper = new PrintWriterWrapper(wr)) {
			
			call.process(wrapper);
			wrapper.flush();
			wrapper.close();
			return wr.toString();
		}
	}

	private String processStreamTest(Call call) throws NullPointerException, IOException, PrintingException {
		try(final ByteArrayOutputStream	wr = new ByteArrayOutputStream();
			final PrintStream			ps = new PrintStream(wr);
			final CharStreamPrinter<PrintStreamWrapper>	wrapper = new PrintStreamWrapper(ps)) {
			
			call.process(wrapper);
			wrapper.flush();
			wrapper.close();
			return wr.toString();
		}
	}
}
