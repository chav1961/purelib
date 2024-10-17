package chav1961.purelib.streams.char2char.intern;



import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.XMLUtils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.interfaces.internal.CreoleFontActions;
import chav1961.purelib.streams.interfaces.internal.CreoleFontState;
import chav1961.purelib.streams.interfaces.internal.CreoleSectionActions;
import chav1961.purelib.streams.interfaces.internal.CreoleSectionState;
import chav1961.purelib.streams.interfaces.internal.CreoleTerminals;
import chav1961.purelib.testing.TestingUtils;

@Tag("OrdinalTestCategory")
public class CreoleWriterTest {
	private final PrintStream	ps = TestingUtils.err();
	
	@Test
	public void basicAndFontTest() throws IOException {
		final List<CreoleTestRecord>	ctr = new ArrayList<>();
		
		callWriter("te**s**t",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"te",
				Action.PROCESS_FONT,CreoleTerminals.TERM_BOLD,
				Action.WRITE_ESCAPED,"s",
				Action.PROCESS_FONT,CreoleTerminals.TERM_BOLD,
				Action.WRITE_ESCAPED,"t",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("te//s//t",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"te",
				Action.PROCESS_FONT,CreoleTerminals.TERM_ITALIC,
				Action.WRITE_ESCAPED,"s",
				Action.PROCESS_FONT,CreoleTerminals.TERM_ITALIC,
				Action.WRITE_ESCAPED,"t",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("te//**s**//t",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"te",
				Action.PROCESS_FONT,CreoleTerminals.TERM_ITALIC,
				Action.WRITE_ESCAPED,"",
				Action.PROCESS_FONT,CreoleTerminals.TERM_BOLD,
				Action.WRITE_ESCAPED,"s",
				Action.PROCESS_FONT,CreoleTerminals.TERM_BOLD,
				Action.WRITE_ESCAPED,"",
				Action.PROCESS_FONT,CreoleTerminals.TERM_ITALIC,
				Action.WRITE_ESCAPED,"t",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("te//~**s~**//t",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"te",
				Action.PROCESS_FONT,CreoleTerminals.TERM_ITALIC,
				Action.WRITE_ESCAPED,"",
				Action.WRITE_ESCAPED,"**s",
				Action.WRITE_ESCAPED,"**",
				Action.PROCESS_FONT,CreoleTerminals.TERM_ITALIC,
				Action.WRITE_ESCAPED,"t",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
	}	

	@Test
	public void linkAndImageTest() throws IOException {
		final List<CreoleTestRecord>	ctr = new ArrayList<>();

		// Link
		callWriter("before [[ref|caption]] after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"ref","caption",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before [[|caption]] after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"","caption",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before [[ref|]] after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before [[ref]] after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// ftp/ftps Link
		callWriter("before ftp://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"ftp://ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before ftps://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"ftps://ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		// http/https Link
		callWriter("before http://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"http://ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before https://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_LINK,"https://ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Escaping
		callWriter("before ~[[ref]] after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"[[ref]] after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before ~ftp://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"ftp://ref after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before ~ftps://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"ftps://ref after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("before ~http://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"http://ref after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("before ~https://ref after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"https://ref after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Image
		callWriter("before {{ref|caption}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_IMAGE,"ref","caption",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before {{|caption}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_IMAGE,"","caption",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before {{ref|}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_IMAGE,"ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before {{ref}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.INSERT_IMAGE,"ref","",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Escaping
		callWriter("before ~{{ref}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"{{ref}} after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
	}	
	
	@Test
	public void paragraphAndCaptionTest() throws IOException {
		final List<CreoleTestRecord>	ctr = new ArrayList<>();

		// Paragraph
		callWriter("test",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("test\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("test\r\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\r\n",
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("test\n\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("test\r\n\r\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\r\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		// Caption
		callWriter("=caption\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("test\n=caption",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("=caption\r\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\r\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("=caption=\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("=====caption\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Too long and escaping
		callWriter("~=====caption\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"=====caption",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("==========caption\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"==========caption",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("=====caption~=====\ntest",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.WRITE_ESCAPED,"=====",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"test",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// List and caption
		callWriter("*item\n=caption",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED,"item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("#item\n=caption",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED,"item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"caption",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
	}
	
	@Test
	public void orderedAndUnorderedListTest() throws IOException {
		final List<CreoleTestRecord>	ctr = new ArrayList<>();

		// Unordered list
		callWriter("before\n* item\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("==before\n* item\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("before\n* item\n\n==after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("before\n* item1\n* item2\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n* item1\n** item11\n* item2\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item11",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Ordered list
		callWriter("before\n# item\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("==before\n# item\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("before\n# item\n\n==after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_START, null, null, CreoleSectionActions.H_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_H_END, null, null, CreoleSectionActions.H_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("before\n# item1\n# item2\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n# item1\n## item11\n# item2\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item11",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		// Unordered-ordered list mix
		callWriter("before\n* item1\n## item11\n* item2\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item11",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		callWriter("before\n# item1\n** item11\n# item2\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item11",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED," item2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Escaping
		callWriter("before\n~* item\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"* item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n~# item\n\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"# item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
	}	

	@Test
	public void tablesTest() throws IOException {
		final List<CreoleTestRecord>	ctr = new ArrayList<>();

		// Table header
		callWriter("before\n|=col1|=col2|\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n|=col1|=col2\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Table header and data
		callWriter("before\n|=col1|=col2|\n|val1|val2|\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TBODY_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TD_OPEN,
				Action.WRITE_ESCAPED,"val1",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TD_OPEN,
				Action.WRITE_ESCAPED,"val2",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOL, null, null, CreoleSectionActions.TD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOL, null, null, CreoleSectionActions.TR_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TBODY_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// Escaping
		callWriter("before\n~|=col1\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"",
				Action.WRITE_ESCAPED,"col1",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n|=col1~|=col2|\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.WRITE_ESCAPED,"|=col2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n|=col1|=col2~|\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col2",
				Action.WRITE_ESCAPED,"|",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n|=col1|=col2|\n|val1~|val11|val2|\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col2",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TBODY_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TD_OPEN,
				Action.WRITE_ESCAPED,"val1",
				Action.WRITE_ESCAPED,"|val11",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TD, null, null, CreoleSectionActions.TD_OPEN,
				Action.WRITE_ESCAPED,"val2",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOL, null, null, CreoleSectionActions.TD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOL, null, null, CreoleSectionActions.TR_CLOSE,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TBODY_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		// Tables and lists
		callWriter("*item\n|=col1\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED,"item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("#item\n|=col1\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED,"item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TABLE_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.THEAD_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TR_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_TH, null, null, CreoleSectionActions.TH_OPEN,
				Action.WRITE_ESCAPED,"col1",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TH_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TR_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.THEAD_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.TABLE_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
	}	

	@Test
	public void miscellaneousTest() throws IOException {
		final List<CreoleTestRecord>	ctr = new ArrayList<>();

		// line break
		callWriter("te\\\\st",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"te",
				Action.PROCESS_FONT,CreoleTerminals.TERM_BR,
				Action.WRITE_ESCAPED,"st",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("te~\\\\st",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"te",
				Action.WRITE_ESCAPED,"\\\\st",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// horizontal line
		callWriter("before\n----\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_END, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_HL,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n---\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"---",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before\n~-----\nafter",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"-----",
				Action.WRITE_ESCAPED,"\n",
				Action.WRITE_ESCAPED,"after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// escaping
		callWriter("before ~~ after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"~",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		// horizontal line and list
		callWriter("*item\n----",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_START, null, null, CreoleSectionActions.UL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED,"item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_UL_END, null, null, CreoleSectionActions.UL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_HL, null, null, CreoleSectionActions.HR,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("#item\n----",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_START, null, null, CreoleSectionActions.OL_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_LI, null, null, CreoleSectionActions.LI_OPEN,
				Action.WRITE_ESCAPED,"item",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.LI_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_OL_END, null, null, CreoleSectionActions.OL_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_HL, null, null, CreoleSectionActions.HR,
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
		
		// Non-Creone insertion
		callWriter("before {{{content}}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE,"content",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before {{{content before\n\ncontent after}}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE,"content before\n\n\n\ncontent after",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before ~{{{content}}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE_ESCAPED,"{{{content}}} after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);

		callWriter("before {{{content~}}} after",ctr);
		assertContent(ctr,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_SOD, null, null, CreoleSectionActions.DIV_OPEN,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_P_START, null, null, CreoleSectionActions.P_OPEN,
				Action.WRITE_ESCAPED,"before ",
				Action.WRITE,"content~",
				Action.WRITE_ESCAPED," after",
				Action.WRITE_ESCAPED,"\n",
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.P_CLOSE,
				Action.PROCESS_SECTION,CreoleTerminals.TERM_EOD, null, null, CreoleSectionActions.DIV_CLOSE,
				Action.CLOSE
		);
	}	
	
//	@Test
	public void txtTest() throws IOException {
		testLoop(MarkupOutputFormat.XML2TEXT,"txt");
	}

//	@Test
	public void htmlTest() throws IOException {
		testLoop(MarkupOutputFormat.XML2HTML,"html");
	}

//	@Test
	public void xmlTest() throws IOException {
		testLoop(MarkupOutputFormat.XML,"xml");
		testXSD(MarkupOutputFormat.XML,XSDCollection.CreoleXML);
	}

//	@Test
	public void fopTest() throws IOException {
		testLoop(MarkupOutputFormat.XML2PDF,"fop");
		testXSD(MarkupOutputFormat.XML2PDF,XSDCollection.CreoleXMLFO);
	}

//	@Test
	public void lowLevelTest() throws IOException {
		try(final CharArrayWriter			wr = new CharArrayWriter()) { // It's an official WikiCreole test case
			try(final InputStream			is = new FileInputStream("./src/test/resources/chav1961/purelib/streams/char2char/cretest.cre"); // It's an official WikiCreole test case
				final Reader				rdr = new InputStreamReader(is,"UTF-8");
				final CreoleOutputWriter	cow = new CreoleOutputWriter(){
					@Override
					public void close() throws IOException {
					}

					@Override
					public void write(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
						ps.print(new String(content,from,to-from));
					}

					@Override
					public void writeEscaped(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
						ps.print(new String(content,from,to-from));
					}
					
					@Override
					public void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
						ps.print("<IMAGE: "+new String(data,startLink,endLink-startLink)+">");
					}

					@Override
					public void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
						ps.print("<LINK: "+new String(data,startLink,endLink-startLink)+">");
					}

					@Override
					public void processSection(final FSM<CreoleTerminals, CreoleSectionState, CreoleSectionActions, Long> fsm, final CreoleTerminals terminal, final CreoleSectionState fromState, final CreoleSectionState toState, final CreoleSectionActions[] action, final Long parameter) throws FlowException {
						ps.print("<Section: "+fromState+"->"+toState+">");
					}

					@Override
					public void processFont(final FSM<CreoleTerminals, CreoleFontState, CreoleFontActions, Long> fsm, final CreoleTerminals terminal, final CreoleFontState fromState, final CreoleFontState toState, final CreoleFontActions[] action, final Long parameter) throws FlowException {
						ps.print("<Font: "+fromState+"->"+toState+">");
					}
				}; 
				final CreoleWriter			cwr = new CreoleWriter(cow)) {
	
				Utils.copyStream(rdr,cwr);
			}
		}
	}
	
	private void testXSD(final MarkupOutputFormat type, final XSDCollection coll) throws IOException {
		final String	processed;
		
		try(final CharArrayWriter	wr = new CharArrayWriter()) { // It's an official WikiCreole test case
			try(final Reader		rdr = new FileReader("./src/test/resources/chav1961/purelib/streams/char2char/cretest.cre"); // It's an official WikiCreole test case
				final CreoleWriter	cwr = new CreoleWriter(wr,type)) {
	
				Utils.copyStream(rdr,cwr);
			}
			processed = wr.toString().replace("\r","");
		}
		ps.println(processed);
		
		try(final InputStream	xsd = XMLUtils.getPurelibXSD(coll);
			final InputStream	xml = new ByteArrayInputStream(processed.getBytes("UTF-8"));) {
			
			Assert.assertTrue(XMLUtils.validateXMLByXSD(xml,xsd,PureLibSettings.SYSTEM_ERR_LOGGER));
		}
	} 
	
	private void testLoop(final MarkupOutputFormat type, final String extension) throws IOException {
//		final String	processed;
		
		try(final CharArrayWriter	wr = new CharArrayWriter()) { // It's an official WikiCreole test case
			try(final InputStream	is = new FileInputStream("./src/test/resources/chav1961/purelib/streams/char2char/cretest.cre"); // It's an official WikiCreole test case
				final Reader		rdr = new InputStreamReader(is,"UTF-8");
				final CreoleWriter	cwr = new CreoleWriter(wr,type)) {
	
				Utils.copyStream(rdr,cwr);
			}
//			processed = 
			wr.toString().replace("\r","").replace("\n","").replace("\t","");
		}

		try(final InputStream	is = new FileInputStream("./src/test/resources/chav1961/purelib/streams/char2char/cretest."+extension);
			final Reader		rdr = new InputStreamReader(is,"UTF-8");
			final StringWriter	wr = new StringWriter()) {
			
			Utils.copyStream(rdr,wr);
			wr.flush();
	//		Assert.assertEquals(processed,wr.toString().replace("\r","").replace("\n","").replace("\t",""));
		}
	}
	
	private void callWriter(final String content, final List<CreoleTestRecord> list) throws IOException {
		list.clear();
		
		try(final CreoleTestWriter	ctw = new CreoleTestWriter((action,parameters)->list.add(new CreoleTestRecord(action,parameters)));
			final CreoleWriter		cw = new CreoleWriter(ctw)) {
			
			cw.append(content);
			cw.flush();
		}
	}
	
	private void assertContent(final List<CreoleTestRecord> content, Object... awaited) {
		int		awaitedIndex = 0, pos = 0;
		
		for (CreoleTestRecord item : content) {
			System.err.println(item);
			if (awaitedIndex < awaited.length && (awaited[awaitedIndex] instanceof Action)) {
				Assert.assertEquals("awaitedIndex="+awaitedIndex+", pos="+pos,awaited[awaitedIndex],item.action);
				awaitedIndex++;
			}
			int	awaitedParameter = 0;
			
			while (awaitedIndex < awaited.length && !(awaited[awaitedIndex] instanceof Action)) {
				if (awaited[awaitedIndex] != null) {
					Assert.assertEquals("awaitedIndex="+awaitedIndex+", pos="+pos+", parmIndex="+awaitedParameter,awaited[awaitedIndex],item.parameters[awaitedParameter]);
				}
				awaitedIndex++;
				awaitedParameter++;
			}
			pos++;
		}
	}
}

enum Action {
	WRITE,
	WRITE_ESCAPED,
	INSERT_IMAGE,
	INSERT_LINK,
	PROCESS_SECTION,
	PROCESS_FONT,
	CLOSE
}

class CreoleTestRecord {
	final Action	action;
	final Object[]					parameters;
	
	public CreoleTestRecord(final Action action, final Object... parameters) {
		this.action = action;
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "CreoleTestRecord [action=" + action + ", parameters=" + Arrays.toString(parameters) + "]";
	}
}

class CreoleTestWriter extends CreoleOutputWriter {
	@FunctionalInterface
	interface CreoleCallback {
		void process(Action action, Object... parameters);
	}
	
	private final CreoleCallback	callback;
	
	CreoleTestWriter(final CreoleCallback callback) {
		this.callback = callback;
	}
	
	@Override
	public void write(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
//		String s = new String(content,from,to-from);
//		System.err.println("{}--> "+s);
		callback.process(Action.WRITE,new String(content,from,to-from),keepNewLines);
	}

	@Override
	public void writeEscaped(long displacement, char[] content, int from, int to, boolean keepNewLines) throws IOException, SyntaxException {
//		System.err.println("--> "+new String(content,from,to-from));
		callback.process(Action.WRITE_ESCAPED,new String(content,from,to-from),keepNewLines);
	}

	@Override
	public void insertImage(long displacement, char[] data, int startLink, int endLink, int startCaption, int endCaption) throws IOException, SyntaxException {
		callback.process(Action.INSERT_IMAGE,new String(data,startLink,endLink-startLink),new String(data,startCaption,endCaption-startCaption));
	}

	@Override
	public void insertLink(boolean localRef, long displacement, char[] data, int startLink, int endLink, int startCaption, int endCaption) throws IOException, SyntaxException {
		callback.process(Action.INSERT_LINK,new String(data,startLink,endLink-startLink),new String(data,startCaption,endCaption-startCaption));
	}

	@Override
	public void processSection(FSM<CreoleTerminals, CreoleSectionState, CreoleSectionActions, Long> fsm, CreoleTerminals terminal, CreoleSectionState fromState, CreoleSectionState toState, CreoleSectionActions[] action, Long parameter) throws FlowException {
		for (CreoleSectionActions item : action) {
			callback.process(Action.PROCESS_SECTION,terminal,fromState,toState,item,parameter);
		}
	}

	@Override
	public void processFont(FSM<CreoleTerminals, CreoleFontState, CreoleFontActions, Long> fsm, CreoleTerminals terminal, CreoleFontState fromState, CreoleFontState toState, CreoleFontActions[] action, Long parameter) throws FlowException {
		for (CreoleFontActions item : action) {
			callback.process(Action.PROCESS_FONT,terminal,fromState,toState,item,parameter);
		}
	}

	@Override
	public void close() throws IOException {
		callback.process(Action.CLOSE);
	}
}
