package chav1961.purelib.ui.swing.terminal;


import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.PrintingException;

@Tag("OrdinalTestCategory")
public class TermTest {
	private static final char	ESC = 0x1F;
	
	@FunctionalInterface
	private interface LambdaPrint {
		void process(Term term) throws PrintingException;
	}
	
	@Test
	public void printingTest() throws PrintingException, IOException {
		try(final Term	term = new Term(20, 10)) {
		
			printAndTest(term,(t)->{t.println();}," ");
			printAndTest(term,(t)->{t.println('a');},"a");
			printAndTest(term,(t)->{t.println((byte)10);},"10");
			printAndTest(term,(t)->{t.println((short)29);},"29");
			printAndTest(term,(t)->{t.println(38);},"38");
			printAndTest(term,(t)->{t.println(47L);},"47");
			printAndTest(term,(t)->{t.println(56.0f);},"56");
			printAndTest(term,(t)->{t.println(65.0);},"65");
			printAndTest(term,(t)->{t.println(true);},"true");
			printAndTest(term,(t)->{t.println(false);},"false");
			printAndTest(term,(t)->{t.println(Integer.valueOf(123));},"123");
			printAndTest(term,(t)->{t.println("test".toCharArray());},"test");
			printAndTest(term,(t)->{t.println("test".toCharArray(),0,2);},"te");
			printAndTest(term,(t)->{t.println("test");},"test");
			printAndTest(term,(t)->{t.println("test",0,2);},"te");
		}
	}

	@Test
	public void cursorTest() throws PrintingException, IOException {
		try(final Term	term = new Term(20, 10)) {
			
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(1,term.getCursorY());
			
			term.print('a');
			Assert.assertEquals(2,term.getCursorX());
			Assert.assertEquals(1,term.getCursorY());
			
			term.print('\t');
			Assert.assertEquals(8,term.getCursorX());
			Assert.assertEquals(1,term.getCursorY());
		
			term.print('\r');
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(1,term.getCursorY());
	
			term.print('\n');
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(2,term.getCursorY());
			
			term.setCursor(0, 0);
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(1,term.getCursorY());
	
			term.setCursor(100, 100);
			Assert.assertEquals(term.getConsoleWidth(),term.getCursorX());
			Assert.assertEquals(term.getConsoleHeight(),term.getCursorY());
		}
	}
	
	@Test
	public void escapingTest() throws PrintingException, IOException {
		try(final Term	term = new Term(20, 10)) {
			
			Assert.assertTrue(term.isCursorOn());	// on/off curs
			term.print(ESC+"[l");
			Assert.assertFalse(term.isCursorOn());
			term.print(ESC+"[h");
			Assert.assertTrue(term.isCursorOn());
			
			term.print(ESC+"[s");	// push/pop curs
			term.setCursor(2, 2);
			Assert.assertEquals(2,term.getCursorX());
			Assert.assertEquals(2,term.getCursorY());
			term.print(ESC+"[u");
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(1,term.getCursorY());
			term.print(ESC+"[u");
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(1,term.getCursorY());

			term.setCursor(5, 5);	// movement
			Assert.assertEquals(5,term.getCursorX());
			Assert.assertEquals(5,term.getCursorY());
			term.print(ESC+"[2A");
			Assert.assertEquals(5,term.getCursorX());
			Assert.assertEquals(3,term.getCursorY());
			term.print(ESC+"[5B");
			Assert.assertEquals(5,term.getCursorX());
			Assert.assertEquals(8,term.getCursorY());
			term.print(ESC+"[2C");
			Assert.assertEquals(3,term.getCursorX());
			Assert.assertEquals(8,term.getCursorY());
			term.print(ESC+"[4D");
			Assert.assertEquals(7,term.getCursorX());
			Assert.assertEquals(8,term.getCursorY());
			term.print(ESC+"[1E");
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(9,term.getCursorY());
			term.print(ESC+"[3F");
			Assert.assertEquals(1,term.getCursorX());
			Assert.assertEquals(6,term.getCursorY());
			term.print(ESC+"[3G");
			Assert.assertEquals(3,term.getCursorX());
			Assert.assertEquals(6,term.getCursorY());
			term.print(ESC+"[5H");
			Assert.assertEquals(5,term.getCursorX());
			Assert.assertEquals(6,term.getCursorY());
			term.print(ESC+"[5;4H");
			Assert.assertEquals(5,term.getCursorX());
			Assert.assertEquals(4,term.getCursorY());
			
			term.clear().print("12345678901234567890").setCursor(5,1).print(ESC+"[0K");	// Line clear
			Assert.assertArrayEquals("1234                ".toCharArray(),term.readContent(new Rectangle(1,1,20,1)));
			term.clear().print("12345678901234567890").setCursor(5,1).print(ESC+"[1K");
			Assert.assertArrayEquals("     678901234567890".toCharArray(),term.readContent(new Rectangle(1,1,20,1)));
			term.clear().print("12345678901234567890").setCursor(5,1).print(ESC+"[2K");
			Assert.assertArrayEquals("                    ".toCharArray(),term.readContent(new Rectangle(1,1,20,1)));

			term.clear().print("1234567890123456789012345678901234567890").setCursor(5,1).print(ESC+"[0J");	// Screen clear
			Assert.assertArrayEquals("1234                                    ".toCharArray(),term.readContent(new Rectangle(1,1,20,2)));
			term.clear().print("1234567890123456789012345678901234567890").setCursor(5,1).print(ESC+"[1J");
			Assert.assertArrayEquals("     67890123456789012345678901234567890".toCharArray(),term.readContent(new Rectangle(1,1,20,2)));
			term.clear().print("1234567890123456789012345678901234567890").setCursor(5,1).print(ESC+"[2J");
			Assert.assertArrayEquals("                                        ".toCharArray(),term.readContent(new Rectangle(1,1,20,2)));

			term.clear().print("1234567890123456789012345678901234567890").setCursor(5,1).print(ESC+"[1S");	// Screen scroll
			Assert.assertArrayEquals("                    12345678901234567890".toCharArray(),term.readContent(new Rectangle(1,1,20,2)));
			term.clear().print("1234567890123456789012345678901234567890").setCursor(5,1).print(ESC+"[1T");
			Assert.assertArrayEquals("12345678901234567890                    ".toCharArray(),term.readContent(new Rectangle(1,1,20,2)));
			
			Assert.assertEquals(Color.GREEN,term.getForeground());	// Color choosing
			Assert.assertEquals(Color.BLACK,term.getBackground());
			term.print(ESC+"[30m");
			Assert.assertEquals(Color.BLACK,term.getForeground());
			Assert.assertEquals(Color.BLACK,term.getBackground());
			term.print(ESC+"[42m");
			Assert.assertEquals(Color.BLACK,term.getForeground());
			Assert.assertEquals(Color.GREEN,term.getBackground());
			term.print(ESC+"[37;47m");
			Assert.assertEquals(Color.WHITE,term.getForeground());
			Assert.assertEquals(Color.WHITE,term.getBackground());
		}
	}
	
	
	private void printAndTest(final Term term, final LambdaPrint action, final String result) throws PrintingException {
		final StringBuilder	sb = new StringBuilder(result);
		
		while (sb.length() < term.getConsoleWidth()) {
			sb.append(' ');
		}
		term.clear();
		action.process(term);
		final char[]	content = term.readContent(new Rectangle(1,1,term.getConsoleWidth(),1));
		
		Assert.assertArrayEquals(sb.toString().toCharArray(),content);
	}
}
