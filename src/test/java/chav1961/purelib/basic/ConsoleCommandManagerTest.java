package chav1961.purelib.basic;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.annotations.ConsoleCommand;
import chav1961.purelib.basic.annotations.ConsoleCommandParameter;
import chav1961.purelib.basic.annotations.ConsoleCommandPrefix;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.interfaces.ConsoleManagerInterface;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class ConsoleCommandManagerTest {
	@Test
	public void lifeCycleTest() throws IOException, ConsoleCommandException {
		try(final ConsoleManagerInterface	cmi = new ConsoleCommandManager()) {
			final PseudoCommandProcessor	proc = new PseudoCommandProcessor();
			
			cmi.deploy(proc);
			
			Assert.assertEquals(cmi.processCmd("exec 2 + 3 - 4 + 5 ?"),"6");
			Assert.assertEquals(cmi.processCmd(""),"exec\nhelp\nUse help cmd=<command_prefix> for details\n");
			Assert.assertEquals(cmi.processCmd("help"),"exec\nhelp\nUse help cmd=<command_prefix> for details\n");
			Assert.assertEquals(cmi.processCmd("help cmd=exec")," - exec ${start} <{+${add}|-${sub}}>... ?\tCalculate simple arithmetic expression. Use exec NNN {+|-} NNN... ? to calculate sum\n - exec crush [all ${name}]\tcrush method\n");
			Assert.assertEquals(cmi.processCmd("help cmd=unknown"),"command prefix [unknown] in not known, use help to get list of available commands\n");
			
			try{cmi.processCmd("exec nothing");
				Assert.fail("Mandatory exception was not detected (unparsed command)");
			} catch (ConsoleCommandException cce) {
			}
			try{cmi.processCmd("exec crush");
				Assert.fail("Mandatory exception was not detected (runtime exception in the procesing method)");
			} catch (ConsoleCommandException cce) {
			}
			try{cmi.processCmd("unknown");
				Assert.fail("Mandatory exception was not detected (unknown command prefix)");
			} catch (ConsoleCommandException cce) {
			}

			cmi.undeploy(proc);
		}
	}

	@Test
	public void consoleLifeCycleTest() throws IOException, ConsoleCommandException {
		try(final ConsoleManagerInterface	cmi = new ConsoleCommandManager();
			final Reader					rdr = new StringReader("exec 2 + 3 - 4 + 5 ?\nexec crush\nunknown\n")) {
			final PseudoCommandProcessor	proc = new PseudoCommandProcessor();
			
			cmi.deploy(proc);
			cmi.processCmd(rdr,System.out);
		}	// undeployAll() need be processed!
	}
	
	
	@Test
	public void illegalParametersTest() throws IOException, ConsoleCommandException {
		try(final ConsoleManagerInterface	cmi = new ConsoleCommandManager()) {
			try{cmi.deploy((Object[])null);
				Assert.fail("Mandatory exception was not detected (null object to deploy)");
			} catch (IllegalArgumentException exc) {
			}
			try{cmi.deploy((Object)null);
				Assert.fail("Mandatory exception was not detected (null object to deploy)");
			} catch (IllegalArgumentException exc) {
			}
			try{cmi.deploy(null,new Object());
				Assert.fail("Mandatory exception was not detected (null object in list to deploy)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{cmi.deploy(new Object());
				Assert.fail("Mandatory exception was not detected (not annotated)");
			} catch (IllegalArgumentException exc) {
			}

			try{cmi.deploy(new IllegalCommandProcessor0());
				Assert.fail("Mandatory exception was not detected (no any annotated methods in the processor)");
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

			try{cmi.deploy(new IllegalCommandProcessor4());
				Assert.fail("Mandatory exception was not detected (unsupported parameter type in the method marked with the @ConsoleCommand template)");
			} catch (IllegalArgumentException exc) {
			}
			
			final IllegalCommandProcessor5	proc = new IllegalCommandProcessor5(); 
			
			try{cmi.deploy(proc);
				cmi.processCmd("exec 1,2");
				Assert.fail("Mandatory exception was not detected (can't convert console parameters to method template)");
			} catch (ConsoleCommandException exc) {
			}
			
			try{cmi.deploy(proc);
				Assert.fail("Mandatory exception was not detected (duplicate command template)");
			} catch (IllegalArgumentException exc) {
			}

			try{cmi.processCmd(null);
				Assert.fail("Mandatory exception was not detected (null command string)");
			} catch (ConsoleCommandException exc) {
			}
			
			try{cmi.processCmd("exec vassya");
				Assert.fail("Mandatory exception was not detected (illegal numeric value)");
			} catch (ConsoleCommandException exc) {
			}
			
			try{cmi.processCmd(null,System.out);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try{cmi.processCmd(new StringReader(""),null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			cmi.undeploy(proc);
			try{cmi.undeploy(proc);
				Assert.fail("Mandatory exception was not detected (nothing to undeploy)");
			} catch (IllegalArgumentException exc) {
			}
			try{cmi.undeploy((Object[])null);
				Assert.fail("Mandatory exception was not detected (null list to undeploy)");
			} catch (IllegalArgumentException exc) {
			}
			try{cmi.undeploy(null,new Object());
				Assert.fail("Mandatory exception was not detected (null list element to undeploy)");
			} catch (IllegalArgumentException exc) {
			}
		}
		
		final ConsoleManagerInterface	cmi = new ConsoleCommandManager();
		cmi.close();
		
		try{cmi.deploy(new Object());
			Assert.fail("Mandatory exception was not detected (closed manager)");
		} catch (IllegalStateException exc) {
		}
		try{cmi.undeploy(new Object());
			Assert.fail("Mandatory exception was not detected (closed manager)");
		} catch (IllegalStateException exc) {
		}
		try{cmi.undeployAll();
			Assert.fail("Mandatory exception was not detected (closed manager)");
		} catch (IllegalStateException exc) {
		}
		try{cmi.processCmd("cmd");
			Assert.fail("Mandatory exception was not detected (closed manager)");
		} catch (IllegalStateException exc) {
		}
		try{cmi.processCmd(new StringReader(""),System.err);
			Assert.fail("Mandatory exception was not detected (closed manager)");
		} catch (IllegalStateException exc) {
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

	@ConsoleCommand(template="exec crush [all ${name}]",help="crush method")
	public String crush(@ConsoleCommandParameter(name="name",defaultValue="!!!") final String data) {
		throw new RuntimeException("Crush simulation");
	}
}

@ConsoleCommandPrefix("exec")
class IllegalCommandProcessor0 {
	public IllegalCommandProcessor0(){}
	
	public String addition(int start) {
		return null;
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
	public String addition(@ConsoleCommandParameter(name="var") Throwable value) {
		return null;
	}
}

@ConsoleCommandPrefix("exec")
class IllegalCommandProcessor5 {
	public IllegalCommandProcessor5(){}
	
	@ConsoleCommand(template="exec <${var}>,...",help="help")
	public String addition(@ConsoleCommandParameter(name="var") int start) {
		return null;
	}
}