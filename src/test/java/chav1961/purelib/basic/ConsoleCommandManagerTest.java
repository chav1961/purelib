package chav1961.purelib.basic;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.annotations.ConsoleCommand;
import chav1961.purelib.basic.annotations.ConsoleCommandParameter;
import chav1961.purelib.basic.annotations.ConsoleCommandPrefix;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.interfaces.ConsoleManagerInterface;

public class ConsoleCommandManagerTest {
	@Test
	public void lifeCycleTest() throws IOException, ConsoleCommandException {
		try(final ConsoleManagerInterface	cmi = new ConsoleCommandManager()) {
			final PseudoCommandProcessor	proc = new PseudoCommandProcessor();
			
			cmi.deploy(proc);
			
			Assert.assertEquals(cmi.processCmd("exec 2 + 3 - 4 + 5 ?"),"6");
			Assert.assertEquals(cmi.processCmd("help"),"exec\nhelp\nUse help cmd=<command_prefix> for details\n");
			Assert.assertEquals(cmi.processCmd("help cmd=exec")," - exec ${start} <{+${add}|-${sub}}>... ?	Calculate simple arithmetic expression. Use exec NNN {+|-} NNN... ? to calculate sum\n");
			
			try{cmi.processCmd("exec nothing");
				Assert.fail("Mandatory exception was not detected (unparsed command)");
			} catch (ConsoleCommandException cce) {
			}

			cmi.undeploy(proc);
		}
	}

	@Test
	public void consoleLifeCycleTest() throws IOException, ConsoleCommandException {
		try(final ConsoleManagerInterface	cmi = new ConsoleCommandManager();
			final Reader					rdr = new StringReader("exec 2 + 3 - 4 + 5 ?\n")) {
			final PseudoCommandProcessor	proc = new PseudoCommandProcessor();
			
			cmi.deploy(proc);
			cmi.processCmd(rdr,System.out);
			cmi.undeploy(proc);
		}
	}
	
	
	@Test
	public void illegalParametersTest() throws IOException, ConsoleCommandException {
		try(final ConsoleManagerInterface	cmi = new ConsoleCommandManager()) {
			try{cmi.deploy(new Object());
				Assert.fail("Mandatory exception was not detected (not anootated)");
			} catch (IllegalArgumentException exc) {
			}

			try{cmi.deploy(new IllegalCommandProcessor1());
				Assert.fail("Mandatory exception was not detected (different prefix and command description)");
			} catch (IllegalArgumentException exc) {
			}

			try{cmi.deploy(new IllegalCommandProcessor2());
				Assert.fail("Mandatory exception was not detected (method param is not labelled with @ConsoleCommandParameter)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{cmi.deploy(new IllegalCommandProcessor3());
				Assert.fail("Mandatory exception was not detected (@ConsoleCommandParameter name is missing in the @ConsoleCommand template)");
			} catch (IllegalArgumentException exc) {
			}

			final IllegalCommandProcessor4	proc = new IllegalCommandProcessor4(); 
			
			try{cmi.deploy(proc);
				cmi.processCmd("exec 1,2");
				Assert.fail("Mandatory exception was not detected (can't convert console parameters to method template)");
			} catch (ConsoleCommandException exc) {
			}
			
			try{cmi.deploy(proc);
				Assert.fail("Mandatory exception was not detected (duplicate command template)");
			} catch (IllegalArgumentException exc) {
			}

			try{cmi.processCmd("exec vassya");
				Assert.fail("Mandatory exception was not detected (illegal numeric value)");
			} catch (ConsoleCommandException exc) {
			}
			
			try{cmi.processCmd(null,System.out);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{cmi.processCmd(new StringReader(""),null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			cmi.undeploy(proc);
			try{cmi.undeploy(proc);
				Assert.fail("Mandatory exception was not detected (nothing to undeploy)");
			} catch (IllegalArgumentException exc) {
			}
		}		
	}
}


@ConsoleCommandPrefix("exec")
class PseudoCommandProcessor {
	public PseudoCommandProcessor(){}
	
	@ConsoleCommand(template="exec ${start} <{+${add}|-${sub}}>... ?",help="Calculate simple arithmetic expression. Use exec NNN {+|-} NNN... ? to calculate sum")
	public String addition(@ConsoleCommandParameter(name="start") int start
						 , @ConsoleCommandParameter(name="add") final  int[] additions
						 , @ConsoleCommandParameter(name="sub") int[] substractions) {
		for (int item : additions) {
			start += item;
		}
		for (int item : substractions) {
			start -= item;
		}
		return String.valueOf(start);
	}
}

@ConsoleCommandPrefix("exec")
class IllegalCommandProcessor1 {
	public IllegalCommandProcessor1(){}
	
	@ConsoleCommand(template="unknown ${start} <{+${add}|-${sub}}>... ?",help="Calculate simple arithmetic expression. Use exec NNN {+|-} NNN... ? to calculate sum")
	public String addition(@ConsoleCommandParameter(name="start") int start) {
		return null;
	}
}

@ConsoleCommandPrefix("exec")
class IllegalCommandProcessor2 {
	public IllegalCommandProcessor2(){}
	
	@ConsoleCommand(template="exec ${var}",help="help")
	public String addition(int start) {
		return null;
	}
}

@ConsoleCommandPrefix("exec")
class IllegalCommandProcessor3 {
	public IllegalCommandProcessor3(){}
	
	@ConsoleCommand(template="exec ${var}",help="help")
	public String addition(@ConsoleCommandParameter(name="unknown") int start) {
		return null;
	}
}

@ConsoleCommandPrefix("exec")
class IllegalCommandProcessor4 {
	public IllegalCommandProcessor4(){}
	
	@ConsoleCommand(template="exec <${var}>,...",help="help")
	public String addition(@ConsoleCommandParameter(name="var") int start) {
		return null;
	}
}