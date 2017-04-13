package chav1961.purelib.streams.char2char;


import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class AbstractChar2CharConvertorTest {
	@Test
	public void basicTest() throws IOException, SyntaxException {
		try{new AbstractChar2CharConvertor(null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("source"));
			Assert.fail("Mandatory exception was not detected (missing ::= )");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("source :::= target"));
			Assert.fail("Mandatory exception was not detected (missing ::= )");
		} catch (IOException exc) {
		}
	}
		
//	@Test
	public void ordinalTest() throws IOException, SyntaxException {
		// Sequences as-is
		new AbstractChar2CharConvertor(new StringReader("source ::= target"));
		new AbstractChar2CharConvertor(new StringReader("source 'with blank' ::= target 'with blank'"));
		new AbstractChar2CharConvertor(new StringReader("source \\% \\[ \\] \\{ \\| \\} \\< \\> \\: \\\' ::= target \\\\ \t"));
		try{new AbstractChar2CharConvertor(new StringReader("source 'unclosed := target"));
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("source \\? := target"));
			Assert.fail("Mandatory exception was not detected (unsupported escape char)");
		} catch (IOException exc) {
		}

		// Prefix functions
		new AbstractChar2CharConvertor(new StringReader("%anchor() source ::= target"));
		new AbstractChar2CharConvertor(new StringReader("%nested(var) source ::= target"));
		new AbstractChar2CharConvertor(new StringReader("%nested(var,5) source ::= target"));
		new AbstractChar2CharConvertor(new StringReader("%number(var) source ::= target"));
		try{new AbstractChar2CharConvertor(new StringReader("%parsed(1) source := target"));
			Assert.fail("Mandatory exception was not detected (illegal usage of this function as anchored)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("%parsed(1 source := target"));
			Assert.fail("Mandatory exception was not detected (close bracket is missing)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("%nested() source := target"));
			Assert.fail("Mandatory exception was not detected (variable name is missing)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("%nested(var source := target"));
			Assert.fail("Mandatory exception was not detected (close bracket is missing)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("%anchor( source := target"));
			Assert.fail("Mandatory exception was not detected (close bracket is missing)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("%number() source := target"));
			Assert.fail("Mandatory exception was not detected (variable is missing)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("%number( source := target"));
			Assert.fail("Mandatory exception was not detected (close bracket is missing)");
		} catch (IOException exc) {
		}		
	}

//	@Test
	public void optionsChoisesLoopsTest() throws IOException, SyntaxException {
		// Options
		new AbstractChar2CharConvertor(new StringReader("source [mark] ::= target"));
		try{new AbstractChar2CharConvertor(new StringReader("source [mark ::= target"));
			Assert.fail("Mandatory exception was not detected (close bracket is missing)");
		} catch (IOException exc) {
		}		
		try{new AbstractChar2CharConvertor(new StringReader("source [%name] ::= target"));
			Assert.fail("Mandatory exception was not detected (non-sequence beginning in the option)");
		} catch (IOException exc) {
		}		

		// Choises
		new AbstractChar2CharConvertor(new StringReader("source {key1|key2} ::= target"));
		new AbstractChar2CharConvertor(new StringReader("source {key1|key2|key3} ::= target"));
		try{new AbstractChar2CharConvertor(new StringReader("source {|key2} ::= target"));
			Assert.fail("Mandatory exception was not detected (non-sequence beginning in the option)");
		} catch (IOException exc) {
		}		
		try{new AbstractChar2CharConvertor(new StringReader("source {key1| ::= target"));
			Assert.fail("Mandatory exception was not detected (close bracket is missing)");
		} catch (IOException exc) {
		}
		
		// Loops
		new AbstractChar2CharConvertor(new StringReader("<source>... next ::= target"));
		new AbstractChar2CharConvertor(new StringReader("<source>,... ::= target"));
		new AbstractChar2CharConvertor(new StringReader("<source>'test'... ::= target"));
		try{new AbstractChar2CharConvertor(new StringReader("<source>.. ::= target"));
			Assert.fail("Mandatory exception was not detected (close loop is missing)");
		} catch (IOException exc) {
		}
		try{new AbstractChar2CharConvertor(new StringReader("<source>,.. ::= target"));
			Assert.fail("Mandatory exception was not detected (close loop is missing)");
		} catch (IOException exc) {
		}
	}

//	@Test
	public void namesAndFunctionsTest() throws IOException, SyntaxException {
		new AbstractChar2CharConvertor(new StringReader("source %name ::= target"));
		new AbstractChar2CharConvertor(new StringReader("source %+name ::= target"));
		new AbstractChar2CharConvertor(new StringReader("source %number(name) ::= target"));
		new AbstractChar2CharConvertor(new StringReader("source %parsed() ::= target"));
		new AbstractChar2CharConvertor(new StringReader("source %parsed(1) ::= target"));
	}
}
