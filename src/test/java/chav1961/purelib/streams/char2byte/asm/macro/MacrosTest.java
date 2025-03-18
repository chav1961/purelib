package chav1961.purelib.streams.char2byte.asm.macro;


import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.SyntaxException;

@Tag("OrdinalTestCategory")
public class MacrosTest {
	@Test
	public void definitionTest() throws IOException, SyntaxException {
		try(final TestMacros	m = new TestMacros()) {
			errorAwaiting(m,"someshit","unknown string",SyntaxException.class,IOException.class);
			errorAwaiting(m,"someshit:","unknown string",SyntaxException.class,IOException.class);
			errorAwaiting(m," someshit","unknown string",SyntaxException.class,IOException.class);
			errorAwaiting(m," .mend","outside the macro",SyntaxException.class,IOException.class);
			errorAwaiting(m," .macro","name is missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"name .macro var1,var1","duplicated pasitional names",SyntaxException.class,IOException.class);
			errorAwaiting(m,"name .macro key1=,var1","mix with the pasitional and key names",SyntaxException.class,IOException.class);
			errorAwaiting(m,"name .macro var1,key1=,key2=true","positional parameter without type",SyntaxException.class,IOException.class);
			errorAwaiting(m,"name .macro var1:unknown,key1=,key2=true","positional parameter with unknown type",SyntaxException.class,IOException.class);
			errorAwaiting(m,"name .macro var1:int,key1=,key2=true","key parameter without type",SyntaxException.class,IOException.class);
			errorAwaiting(m,"name .macro var1:int,key1:unknown=,key2=true","key parameter with unknown type",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"name .macro var1:str,key1:int=,key2:bool=true");
			
			errorAwaiting(m,"var1	.local","duplicate name",SyntaxException.class,IOException.class);
			errorAwaiting(m,"local1:.local","label instead of name",SyntaxException.class,IOException.class);
			errorAwaiting(m,"local1	.local ","var without type",SyntaxException.class,IOException.class);
			errorAwaiting(m,"local1	.local int = ","var without initial value",SyntaxException.class,IOException.class);
			successAwaiting(m,"local1	.local int = 100");
			successAwaiting(m,"local1	.set local1*2");
			errorAwaiting(m,"local1	.set","expression is missing",SyntaxException.class,IOException.class);
			
			errorAwaiting(m,"local2	.local 200","outside the content",SyntaxException.class,IOException.class);
			
			errorAwaiting(m,"name	.if","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.if","label need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.if","expression is missing",SyntaxException.class,IOException.class);
			successAwaiting(m,"		.if true");
			
			errorAwaiting(m,"name	.elseif","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.elseif","label need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.elseif","expression missing",SyntaxException.class,IOException.class);
			successAwaiting(m,"		.elseif true");
			
			errorAwaiting(m,"name	.else","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.else","label need missing",SyntaxException.class,IOException.class);
			successAwaiting(m,"		.else");
			errorAwaiting(m,"		.else","else without context",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.endif");
			errorAwaiting(m,"		.endif","end without context",SyntaxException.class,IOException.class);

			successAwaiting(m,"		.if true");
			successAwaiting(m,"		.else");
			successAwaiting(m,"		.endif");

			errorAwaiting(m,"		.else","else without context",SyntaxException.class,IOException.class);
			
			errorAwaiting(m,"name	.while","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.while","expression is missing",SyntaxException.class,IOException.class);
			successAwaiting(m,"		.while true");
			
			successAwaiting(m,"		.break");
			errorAwaiting(m,"		.break","dead code",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.endwhile");

			errorAwaiting(m,"		.break","break outside the context",SyntaxException.class,IOException.class);
			
			errorAwaiting(m,"name	.while","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.while","expression is missing",SyntaxException.class,IOException.class);
			successAwaiting(m,"lab:	.while true");
			
			errorAwaiting(m,"		.continue unknown","unknown label",SyntaxException.class,IOException.class);
			successAwaiting(m,"		.continue lab");
			errorAwaiting(m,"		.continue","dead code",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.endwhile");

			errorAwaiting(m,"		.continue","continue outside the context",SyntaxException.class,IOException.class);
			
			errorAwaiting(m,"name	.for","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for","expression is missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for unknown","undeclared variable",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for var1","missing (=)",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for var1 = 10","missing to)",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for var1 = true","illegal data type)",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for var1 = 10 to","missing expr)",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for var1 = 10 to true","illegal data type)",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for var1 = 10 to 20 step","missing expr)",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.for var1 = 10 to 20 step true","illegal data type)",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.for var1 = 10 to 20 step 2");
			
			errorAwaiting(m,"name	.error","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.error","label need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.error","expression is missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.error true","illegal data type",SyntaxException.class,IOException.class);

			successAwaiting(m,"		.error \"123\"");
			errorAwaiting(m,"		.error \"123\"","dead code",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.endfor");

			successAwaiting(m,"		.for var1 = 10 to 20 step 2");
			
			errorAwaiting(m,"name	.exit","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.exit","label need missing",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.exit");
			errorAwaiting(m,"		.exit","dead code",SyntaxException.class,IOException.class);

			errorAwaiting(m,"		.mend","unclosed .end",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.endfor");
			
			errorAwaiting(m,"name	.choise","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.choise","label need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.choise","missing expression",SyntaxException.class,IOException.class);
			successAwaiting(m,"		.choise 100");
			
			errorAwaiting(m,"name	.of","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.of","label need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"		.of","missing expression",SyntaxException.class,IOException.class);
			
			successAwaiting(m,"		.of 100");
			
			errorAwaiting(m,"name	.otherwise","name need missing",SyntaxException.class,IOException.class);
			errorAwaiting(m,"label:	.otherwise","label need missing",SyntaxException.class,IOException.class);

			successAwaiting(m,"		.otherwise");
			
			successAwaiting(m,"		.endchoise");
			
			successAwaiting(m,"1234567&var1&local1.1234567 ");
			successAwaiting(m,"		.mend");
		}
	}

	@Test
	public void parseCallTest() throws IOException, SyntaxException {
		try(final TestMacros	m = new TestMacros()) {
			successAwaiting(m,"name .macro var1:int,key1:int=,key2:boolean=true");
			successAwaiting(m,"		.exit");
			successAwaiting(m,"		.mend");
			
			
			successCall(m,"100,key2=false,key1=1");
			successCall(m,"-10,key1=1");
			errorCall(m,"100,key2=unknown,key1=1","Invalid constant value",SyntaxException.class,IOException.class);
			errorCall(m,"100,200","Too many positionals",SyntaxException.class,IOException.class);
			errorCall(m,"100,key1=10,200","mix of the parameters",SyntaxException.class,IOException.class);
		}

		try(final TestMacros	m = new TestMacros()) {
			successAwaiting(m,"name .macro var1:int[],key1:int=,key2:boolean=true,key3:str[]={\"init1\"}");
			successAwaiting(m,"		.exit");
			successAwaiting(m,"		.mend");
			
			
			successCall(m,"100,key2=false,key1=1");
			errorCall(m,"-10,key1=1","Invalid constant value",SyntaxException.class,IOException.class);
			successCall(m,"{10,20},key1=1");
			successCall(m,"10,key3=\"val1\"");
			successCall(m,"10,key3={\"val1\",\"val2\"}");
			errorCall(m,"100,key2=unknown,key1=1","Invalid constant value",SyntaxException.class,IOException.class);
			errorCall(m,"100,200","Too many positionals",SyntaxException.class,IOException.class);
			errorCall(m,"100,key1=10,200","mix of the parameters",SyntaxException.class,IOException.class);
		}
	}
	
	private void successAwaiting(final TestMacros macros, final String content) {
		try{macros.processLine(content);			
		} catch (Throwable t) {
			t.printStackTrace();
			Assert.fail("Unwaited error was detected: "+t.getLocalizedMessage());
		}
	}

	@SafeVarargs
	private final void errorAwaiting(final TestMacros macros, final String content, final String problemDescription, final Class<? extends Throwable>... problems) {
		try{macros.processLine(content);
			Assert.fail("Mandatory exception was not detected ("+problemDescription+")");
		} catch (Throwable t) {
			for (Class<?> item : problems) {
				if (t.getClass() == item) {
					return;
				}
			}
			Assert.fail("Unwaited exception was detected: "+t.getLocalizedMessage());
		}
	}

	private void successCall(final TestMacros macros, final String content) {
		try{macros.parseCall(content);			
		} catch (Throwable t) {
			t.printStackTrace();
			Assert.fail("Unwaited error was detected: "+t.getLocalizedMessage());
		}
	}

	@SafeVarargs
	private final void errorCall(final TestMacros macros, final String content, final String problemDescription, final Class<? extends Throwable>... problems) {
		try{macros.parseCall(content);
			Assert.fail("Mandatory exception was not detected ("+problemDescription+")");
		} catch (Throwable t) {
			for (Class<?> item : problems) {
				if (t.getClass() == item) {
					return;
				}
			}
			Assert.fail("Unwaited exception was detected: "+t.getLocalizedMessage());
		}
	}
}

class TestMacros extends Macros {
	public void processLine(final String line) throws IOException, SyntaxException {
		final char[]	content = (line+"\n").toCharArray();
		
		processLine(0,0,content,0,content.length);
	}

	MacroCommand parseCall(final String line) throws IOException, SyntaxException {
		final char[]	content = (line+"\n").toCharArray();
		
		return parseCall(0,content,0,content.length);
	}
}
