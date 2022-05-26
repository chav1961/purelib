package chav1961.purelib.ui;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class ColorPairTest {
	@Test
	public void basicTest() {
		final ColorPair	cp1 = new ColorPair(Color.WHITE,Color.BLACK), cp2 = new ColorPair(Color.WHITE,Color.BLACK), cp3 = new ColorPair(Color.BLACK,Color.WHITE);
		
		Assert.assertEquals(Color.WHITE,cp1.getForeground());
		Assert.assertEquals(Color.BLACK,cp1.getBackground());
		Assert.assertEquals(cp1,cp2);
		Assert.assertEquals(cp1.toString(),cp2.toString());
		Assert.assertFalse(cp1.equals(cp3));

		cp1.setForeground(Color.MAGENTA);
		Assert.assertEquals(Color.MAGENTA,cp1.getForeground());
		
		cp1.setBackground(Color.CYAN);
		Assert.assertEquals(Color.CYAN,cp1.getBackground());
		
		try{new ColorPair(null,Color.white);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new ColorPair(Color.white,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{cp1.setForeground(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{cp1.setBackground(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void serializationTest() throws IOException, PrintingException, SyntaxException {
		final ColorPair	from = new ColorPair(Color.GREEN, Color.BLUE), to = new ColorPair(Color.BLACK, Color.WHITE);
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final Writer			wr = new OutputStreamWriter(baos);
				final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
				
				from.toJson(prn);
				prn.flush();
				
				try{from.toJson(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
			}
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray());
				final Reader				rdr = new InputStreamReader(bais);
				final JsonStaxParser		parser = new JsonStaxParser(rdr)) {
				
				parser.next();
				to.fromJson(parser);
				
				try{to.fromJson(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
			}
			
			Assert.assertEquals(from, to);
		}
	}
}
