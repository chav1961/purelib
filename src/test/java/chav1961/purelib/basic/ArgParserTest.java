package chav1961.purelib.basic;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.ArgParser.ArgDescription;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.NodeEnterMode;

public class ArgParserTest {
	@Test
	public void basicTest() throws ConsoleCommandException, ContentException {
		try{new ArgParser((ArgDescription[])null) {};
			Assert.fail("Mandatory exception was not detected (null argument list)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ArgParser() {}; 
			Assert.fail("Mandatory exception was not detected (empty argument list)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ArgParser((ArgDescription)null) {};
			Assert.fail("Mandatory exception was not detected (nulls inside argument list)");
		} catch (IllegalArgumentException exc) {
		}

		try{new ArgParser(new ArgParser.BooleanArg(null,false,"help",false)) {};
			Assert.fail("Mandatory exception was not detected (null name of argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ArgParser(new ArgParser.BooleanArg("",false,"help",false)) {};
			Assert.fail("Mandatory exception was not detected (null name of argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ArgParser(new ArgParser.BooleanArg("key",false,null,false)) {};
			Assert.fail("Mandatory exception was not detected (null help string of argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ArgParser(new ArgParser.BooleanArg("key",false,"",false)) {};
			Assert.fail("Mandatory exception was not detected (empty help string of argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new ArgParser(new ArgParser.BooleanArg("key",false,"help",false),new ArgParser.BooleanArg("key",false,"help",false)) {};
			Assert.fail("Mandatory exception was not detected (duplicate names inside argument list)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ArgParser(new ArgParser.StringListArg("key1",false,true,"help"),new ArgParser.StringListArg("key2",false,true,"help")) {};
			Assert.fail("Mandatory exception was not detected (positional list after positional list)");
		} catch (IllegalArgumentException exc) {
		}
	}	

	@Test
	public void parseTest() throws ConsoleCommandException, ContentException {
		final Map<String,String[]>	result = new HashMap<>();
		
		ArgParser.parseParameters(false,false,'-',true,new ArgDescription[]{new ArgParser.BooleanArg("key",false,"help",false)},false,CharUtils.split("-key",' '),result);
		Assert.assertTrue(result.containsKey("key"));
		Assert.assertEquals("true",result.get("key")[0]);

		try{ArgParser.parseParameters(false,false,'-',true,new ArgDescription[]{new ArgParser.BooleanArg("key",false,"help",false)},false,CharUtils.split("",' '),result);
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}	
	
	@Test
	public void lifeCycleTest() throws ConsoleCommandException, ContentException {
		ArgParser	parser = new ArgParser(new ArgParser.BooleanArg("key",false,"help",false));
		
		Assert.assertTrue(parser.getUsage("test").contains("test"));
		
		try{parser.isTyped("key");
			Assert.fail("Mandatory exception was not detected (isTyped() without parse())");
		} catch (IllegalStateException exc) {
		}
		try{parser.getValue("key",boolean.class);
			Assert.fail("Mandatory exception was not detected (getValue() without parse())");
		} catch (IllegalStateException exc) {
		}
		
		try{parser.parse().parse();
			Assert.fail("Mandatory exception was not detected (duplicate parse())");
		} catch (IllegalStateException exc) {
		}

		Assert.assertTrue(parser.parse("-key").getValue("key",boolean.class));
		Assert.assertTrue(parser.parse("-key").isTyped("key"));
		Assert.assertFalse(parser.parse("-key").isTyped("unknown"));
		
		try{parser.parse((String[])null);
			Assert.fail("Mandatory exception was not detected (null argument list)");
		} catch (NullPointerException exc) {
		}
		try{parser.parse((String)null);
			Assert.fail("Mandatory exception was not detected (null inside argument list)");
		} catch (NullPointerException exc) {
		}

		try{parser.parse().isTyped(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.parse().isTyped("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
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
		final ArgParser.BooleanArg	arg = new ArgParser.BooleanArg("key",false,"help",false);

		Assert.assertNotNull(arg.toString());
		arg.validate("true");
		arg.validate("false");
		try{arg.validate(null);
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("unknown");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		
		ArgParser	parser = new ArgParser(arg);
		
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
		} catch (CommandLineParametersException exc) {
		}
		try{new ArgParser(new ArgParser.BooleanArg("key",true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{new ArgParser(new ArgParser.BooleanArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}

	@Test
	public void intArgTest() throws ConsoleCommandException, ContentException {
		final ArgParser.IntegerArg	arg = new ArgParser.IntegerArg("key",false,"help",100,new long[][]{{-100,100},{1000,1000}});
		
		Assert.assertNotNull(arg.toString());
		arg.validate("-100");
		try{arg.validate(null);
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("unknown");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("200");
			Assert.fail("Mandatory exception was not detected (value out of ranges available)");
		} catch (CommandLineParametersException exc) {
		}
		
		ArgParser	parser = new ArgParser(arg);
		
		Assert.assertEquals(1000,parser.parse("-key","1000").getValue("key",int.class).intValue());
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
		} catch (CommandLineParametersException exc) {
		}
		try{new ArgParser(new ArgParser.IntegerArg("key",true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{new ArgParser(new ArgParser.IntegerArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}

	@Test
	public void realArgTest() throws ConsoleCommandException, ContentException {
		final ArgParser.RealArg	arg = new ArgParser.RealArg("key",false,"help",100);

		Assert.assertNotNull(arg.toString());
		arg.validate("-100");
		try{arg.validate(null);
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("unknown");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		
		ArgParser	parser = new ArgParser(arg);
		
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
		} catch (CommandLineParametersException exc) {
		}
		try{new ArgParser(new ArgParser.RealArg("key",true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{new ArgParser(new ArgParser.RealArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}

	@Test
	public void stringArgTest() throws ConsoleCommandException, ContentException {
		final ArgParser.StringArg	arg = new ArgParser.StringArg("key",false,"help","100");

		Assert.assertNotNull(arg.toString());
		arg.validate("test");
		try{arg.validate(null);
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		
		ArgParser	parser = new ArgParser(arg);
		
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
		} catch (CommandLineParametersException exc) {
		}
		
		try{new ArgParser(new ArgParser.StringArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}

	@Test
	public void URIArgTest() throws ConsoleCommandException, ContentException {
		final ArgParser.URIArg	arg = new ArgParser.URIArg("key",false,"help","100");

		Assert.assertNotNull(arg.toString());
		arg.validate("file://localhost/c:");
		try{arg.validate(null);
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("c:#/");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		
		ArgParser	parser = new ArgParser(arg);
		
		Assert.assertEquals("200",parser.parse("-key","200").getValue("key",String.class));
		Assert.assertEquals(URI.create("c:/200"),parser.parse("-key","c:/200").getValue("key",URI.class));
		Assert.assertEquals("100",parser.parse().getValue("key",String.class));
		
		parser = new ArgParser(new ArgParser.URIArg("key",true,true,"help"));
		Assert.assertEquals(URI.create("file:/x"),parser.parse("file:/x").getValue("key",URI.class));

		parser = new ArgParser(new ArgParser.URIArg("key",false,true,"help"));
		Assert.assertEquals("",parser.parse().getValue("key",String.class));

		try{parser.parse().getValue("key",InputStream.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (ContentException exc) {
		}		
		
		try{new ArgParser(new ArgParser.URIArg("key",true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{new ArgParser(new ArgParser.StringArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}
	
	@Test
	public void enumArgTest() throws ConsoleCommandException, ContentException {
		final ArgParser.EnumArg<NodeEnterMode>	arg = new ArgParser.EnumArg<NodeEnterMode>("key",NodeEnterMode.class,false,"help",NodeEnterMode.ENTER);

		Assert.assertNotNull(arg.toString());
		arg.validate("ENTER");
		try{arg.validate(null);
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("unknown");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		
		ArgParser	parser = new ArgParser(arg);
		
		Assert.assertEquals(NodeEnterMode.EXIT,parser.parse("-key","EXIT").getValue("key",NodeEnterMode.class));
		Assert.assertEquals(NodeEnterMode.ENTER,parser.parse().getValue("key",NodeEnterMode.class));
		
		parser = new ArgParser(new ArgParser.EnumArg<NodeEnterMode>("key",NodeEnterMode.class,true,true,"help"));
		Assert.assertEquals(NodeEnterMode.EXIT,parser.parse("EXIT").getValue("key",NodeEnterMode.class));

		parser = new ArgParser(new ArgParser.EnumArg<NodeEnterMode>("key",NodeEnterMode.class,false,true,"help"));
		Assert.assertEquals(NodeEnterMode.ENTER,parser.parse().getValue("key",NodeEnterMode.class));

		try{parser.parse().getValue("key",InputStream.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (ContentException exc) {
		}		
		
		try{new ArgParser(new ArgParser.EnumArg<NodeEnterMode>("key",NodeEnterMode.class,true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
		try{new ArgParser(new ArgParser.EnumArg<NodeEnterMode>("key",NodeEnterMode.class,true,true,"help")).parse("illegal");
			Assert.fail("Mandatory exception was not detected (illegal argument value)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{new ArgParser(new ArgParser.EnumArg<NodeEnterMode>("key",NodeEnterMode.class,true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}
	
	@Test
	public void stringListArgTest() throws ConsoleCommandException, ContentException {
		final ArgParser.StringListArg	arg = new ArgParser.StringListArg("key",false,"help","100"); 
		ArgParser						parser = new ArgParser(arg);
		
		Assert.assertNotNull(arg.toString());
		Assert.assertArrayEquals(new String[]{"200","400"},parser.parse("-key","200","400").getValue("key",String[].class));
		Assert.assertArrayEquals(new String[]{"100"},parser.parse().getValue("key",String[].class));
		
		parser = new ArgParser(new ArgParser.StringListArg("key",true,true,"help"));
		Assert.assertArrayEquals(new String[]{"200"},parser.parse("200").getValue("key",String[].class));

		parser = new ArgParser(new ArgParser.StringListArg("key",false,true,"help"));
		Assert.assertArrayEquals(new String[0],parser.parse().getValue("key",String[].class));

		Assert.assertArrayEquals(new InputStream[0],parser.parse().getValue("key",InputStream[].class));
		Assert.assertNull(parser.parse().getValue("key",InputStream.class));
		
		try{new ArgParser(new ArgParser.StringListArg("key",true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{new ArgParser(new ArgParser.StringListArg("key",true,false,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}

	@Test
	public void ConfigArgTest() throws ConsoleCommandException, ContentException {
		final ArgParser.ConfigArg	arg = new ArgParser.ConfigArg("key",false,"help","./test/properties");

		Assert.assertNotNull(arg.toString());
		arg.validate("file://localhost/c:");
		try{arg.validate(null);
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		try{arg.validate("c:#/");
			Assert.fail("Mandatory exception was not detected (illegal value for the given type)");
		} catch (CommandLineParametersException exc) {
		}
		
		ArgParser	parser = new ArgParser(arg);
		
		Assert.assertEquals("200",parser.parse("-key","200").getValue("key",String.class));
		Assert.assertEquals(URI.create("c:/200"),parser.parse("-key","c:/200").getValue("key",URI.class));
		Assert.assertEquals("./test/properties",parser.parse().getValue("key",String.class));
		
		parser = new ArgParser(new ArgParser.ConfigArg("key",true,true,"help"));
		Assert.assertEquals(URI.create("file:./src/test/resources/chav1961/purelib/basic/test.properties"),parser.parse(true,true,"file:./src/test/resources/chav1961/purelib/basic/test.properties").getValue("key",URI.class));
		Assert.assertEquals("value1",parser.parse(true,true,"file:./src/test/resources/chav1961/purelib/basic/test.properties").getValue("key1",String.class));

		try{parser.parse(true,false,"file:./src/test/resources/chav1961/purelib/basic/test.properties").getValue("key",URI.class);
			Assert.fail("Mandatory exception was not detected (unknown keys ion the configuration file)");
		} catch (CommandLineParametersException exc) {
		}
		
		parser = new ArgParser(new ArgParser.ConfigArg("key",false,true,"help"));
		Assert.assertNull(parser.parse().getValue("key",String.class));

		try{new ArgParser(new ArgParser.ConfigArg("key",true,true,"help")).parse();
			Assert.fail("Mandatory exception was not detected (missing mandatory argument)");
		} catch (CommandLineParametersException exc) {
		}
	}
}
