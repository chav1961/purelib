package chav1961.purelib.basic;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.ArgParser.IntegerArg;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.NodeEnterMode;

public class ArgParserTest {
	@Test
	public void lifeCycleTest() throws ConsoleCommandException, ContentException {
		ArgParser	parser = new ArgParser(new ArgParser.BooleanArg("key",false,"help",false));
		
		Assert.assertTrue(parser.getUsage("test").contains("test"));
		
		try{parser.getValue("key",boolean.class);
			Assert.fail("Mandatory exception was not detected (getValue() without parse())");
		} catch (IllegalStateException exc) {
		}
		try{parser.parse().parse();
			Assert.fail("Mandatory exception was not detected (duplicate parse())");
		} catch (IllegalStateException exc) {
		}
		
		try{parser.parse((String[])null);
			Assert.fail("Mandatory exception was not detected (null argument list)");
		} catch (NullPointerException exc) {
		}
		try{parser.parse((String)null);
			Assert.fail("Mandatory exception was not detected (null inside argument list)");
		} catch (NullPointerException exc) {
		}

		try{parser.parse().getValue(null,boolean.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.parse().getValue("",boolean.class);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.parse().getValue("key",null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		try{parser.getUsage(null);
			Assert.fail("Mandatory exception was not detected (null 1-at argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.getUsage("");
			Assert.fail("Mandatory exception was not detected (empty 1-at argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new ArgParser();
			Assert.fail("Mandatory exception was not detected (empty agrument list)");
		} catch (IllegalArgumentException exc) {
		}
	}	
	
	@Test
	public void booleanArgTest() throws ConsoleCommandException, ContentException {
		ArgParser	parser = new ArgParser(new ArgParser.BooleanArg("key",false,"help",false));
		
		Assert.assertTrue(parser.parse("-key").getValue("key",boolean.class));
		Assert.assertFalse(parser.parse().getValue("key",boolean.class));
		
		parser = new ArgParser(new ArgParser.BooleanArg("key",true,true,"help"));
		Assert.assertTrue(parser.parse("true").getValue("key",boolean.class));

		parser = new ArgParser(new ArgParser.BooleanArg("key",false,true,"help"));
		Assert.assertFalse(parser.parse().getValue("key",boolean.class));

		try{parser.parse().getValue("key",InputStream.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (ContentException exc) {
		}		
		
		try{new ArgParser(new ArgParser.BooleanArg("key",true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
		try{new ArgParser(new ArgParser.BooleanArg("key",true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (ConsoleCommandException exc) {
		}
		
		try{new ArgParser(new ArgParser.BooleanArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
	}

	@Test
	public void intArgTest() throws ConsoleCommandException, ContentException {
		ArgParser	parser = new ArgParser(new ArgParser.IntegerArg("key",false,"help",100));
		
		Assert.assertEquals(200,parser.parse("-key","200").getValue("key",int.class).intValue());
		Assert.assertEquals(100,parser.parse().getValue("key",int.class).intValue());
		
		parser = new ArgParser(new ArgParser.IntegerArg("key",true,true,"help"));
		Assert.assertEquals(200,parser.parse("200").getValue("key",int.class).intValue());

		parser = new ArgParser(new ArgParser.IntegerArg("key",false,true,"help"));
		Assert.assertEquals(0,parser.parse().getValue("key",int.class).intValue());

		try{parser.parse().getValue("key",InputStream.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (ContentException exc) {
		}		
		
		try{new ArgParser(new ArgParser.IntegerArg("key",true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
		try{new ArgParser(new ArgParser.IntegerArg("key",true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (ConsoleCommandException exc) {
		}
		
		try{new ArgParser(new ArgParser.IntegerArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
	}

	@Test
	public void realArgTest() throws ConsoleCommandException, ContentException {
		ArgParser	parser = new ArgParser(new ArgParser.RealArg("key",false,"help",100));
		
		Assert.assertEquals(200,parser.parse("-key","200").getValue("key",double.class).doubleValue(),0.001);
		Assert.assertEquals(100,parser.parse().getValue("key",double.class).doubleValue(),0.001);
		
		parser = new ArgParser(new ArgParser.RealArg("key",true,true,"help"));
		Assert.assertEquals(200,parser.parse("200").getValue("key",double.class).doubleValue(),0.001);

		parser = new ArgParser(new ArgParser.RealArg("key",false,true,"help"));
		Assert.assertEquals(0,parser.parse().getValue("key",double.class).doubleValue(),0.001);

		try{parser.parse().getValue("key",InputStream.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (ContentException exc) {
		}		
		
		try{new ArgParser(new ArgParser.RealArg("key",true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
		try{new ArgParser(new ArgParser.RealArg("key",true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (ConsoleCommandException exc) {
		}
		
		try{new ArgParser(new ArgParser.RealArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
	}

	@Test
	public void stringArgTest() throws ConsoleCommandException, ContentException {
		ArgParser	parser = new ArgParser(new ArgParser.StringArg("key",false,"help","100"));
		
		Assert.assertEquals("200",parser.parse("-key","200").getValue("key",String.class));
		Assert.assertEquals("100",parser.parse().getValue("key",String.class));
		
		parser = new ArgParser(new ArgParser.StringArg("key",true,true,"help"));
		Assert.assertEquals("200",parser.parse("200").getValue("key",String.class));

		parser = new ArgParser(new ArgParser.StringArg("key",false,true,"help"));
		Assert.assertEquals("",parser.parse().getValue("key",String.class));

		try{parser.parse().getValue("key",InputStream.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (ContentException exc) {
		}		
		
		try{new ArgParser(new ArgParser.StringArg("key",true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
		
		try{new ArgParser(new ArgParser.StringArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
	}
	
	@Test
	public void enumArgTest() throws ConsoleCommandException, ContentException {
		ArgParser	parser = new ArgParser(new ArgParser.EnumArg("key",NodeEnterMode.class,false,"help",NodeEnterMode.ENTER));
		
		Assert.assertEquals(NodeEnterMode.EXIT,parser.parse("-key","EXIT").getValue("key",NodeEnterMode.class));
		Assert.assertEquals(NodeEnterMode.ENTER,parser.parse().getValue("key",NodeEnterMode.class));
		
		parser = new ArgParser(new ArgParser.EnumArg("key",NodeEnterMode.class,true,true,"help"));
		Assert.assertEquals(NodeEnterMode.EXIT,parser.parse("EXIT").getValue("key",NodeEnterMode.class));

		parser = new ArgParser(new ArgParser.EnumArg("key",NodeEnterMode.class,false,true,"help"));
		Assert.assertEquals(NodeEnterMode.ENTER,parser.parse().getValue("key",NodeEnterMode.class));

		try{parser.parse().getValue("key",InputStream.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (ContentException exc) {
		}		
		
		try{new ArgParser(new ArgParser.EnumArg("key",NodeEnterMode.class,true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
		try{new ArgParser(new ArgParser.EnumArg("key",NodeEnterMode.class,true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (ConsoleCommandException exc) {
		}
		
		try{new ArgParser(new ArgParser.EnumArg("key",NodeEnterMode.class,true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (ConsoleCommandException exc) {
		}
	}
}
