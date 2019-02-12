package chav1961.purelib.streams.char2byte.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.char2byte.asm.CompilerUtils;

public class CompilerUtilsTest {
	@Test
	public void findTest() throws ContentException {
		final Class<?>	clazz = CompilerUtilsRabbitChild.class;
		
		Assert.assertEquals("field1",CompilerUtils.findField(clazz,"field1",false).getName());
		Assert.assertEquals("field2",CompilerUtils.findField(clazz,"field2",false).getName());
		Assert.assertEquals("field3",CompilerUtils.findField(clazz,"field3",false).getName());
		Assert.assertEquals("field4",CompilerUtils.findField(clazz,"field4",false).getName());

		Assert.assertEquals("field2",CompilerUtils.findField(clazz,"field2",true).getName());
		Assert.assertEquals("field4",CompilerUtils.findField(clazz,"field4",true).getName());
		
		try{CompilerUtils.findField(null,"field2",false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.findField(clazz,null,false);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findField(clazz,"",false);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findField(clazz,"unknown",true);
			Assert.fail("Mandatory exception was not detected (non-existent name)");
		} catch (ContentException exc) {
		}
		try{CompilerUtils.findField(clazz,"unknown",false);
			Assert.fail("Mandatory exception was not detected (non-existent name)");
		} catch (ContentException exc) {
		}
		
		Assert.assertEquals("call1",CompilerUtils.findMethod(clazz,"call1",false).getName());
		Assert.assertEquals("call2",CompilerUtils.findMethod(clazz,"call2",false).getName());
		Assert.assertEquals("call3",CompilerUtils.findMethod(clazz,"call3",false).getName());
		Assert.assertEquals("call4",CompilerUtils.findMethod(clazz,"call4",false).getName());
		
		Assert.assertEquals("call2",CompilerUtils.findMethod(clazz,"call2",true).getName());
		Assert.assertEquals("call4",CompilerUtils.findMethod(clazz,"call4",true).getName());

		try{CompilerUtils.findMethod(null,"call1",false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.findMethod(clazz,null,false);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findMethod(clazz,"",false);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findMethod(clazz,"unknown",true);
			Assert.fail("Mandatory exception was not detected (non-existent name)");
		} catch (ContentException exc) {
		}
		try{CompilerUtils.findMethod(clazz,"unknown",false);
			Assert.fail("Mandatory exception was not detected (non-existent name)");
		} catch (ContentException exc) {
		}

		Assert.assertEquals(clazz,CompilerUtils.findConstructor(clazz,false).getDeclaringClass());
		
		try{CompilerUtils.findConstructor(null,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.findConstructor(clazz,true);
			Assert.fail("Mandatory exception was not detected (non-existent name)");
		} catch (ContentException exc) {
		}
	}

	@Test
	public void buildTest() throws ContentException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		final Class<?>	clazz = CompilerUtilsRabbitChild.class, array = CompilerUtilsRabbitChild[].class; 
	
		Assert.assertEquals(clazz.getPackage().getName()+"."+clazz.getSimpleName(),CompilerUtils.buildClassPath(clazz));
		Assert.assertEquals(clazz.getPackage().getName()+"."+clazz.getSimpleName()+"[]",CompilerUtils.buildClassPath(array));
		
		try{CompilerUtils.buildClassPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals("L"+(clazz.getPackage().getName()+"."+clazz.getSimpleName()).replace(".","/")+";",CompilerUtils.buildClassSignature(clazz));
		Assert.assertEquals("[L"+(clazz.getPackage().getName()+"."+clazz.getSimpleName()).replace(".","/")+";",CompilerUtils.buildClassSignature(array));
		
		try{CompilerUtils.buildClassSignature(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final Field		field2 = clazz.getField("field2");
		
		Assert.assertEquals(clazz.getPackage().getName()+"."+clazz.getSuperclass().getSimpleName()+"."+field2.getName(),CompilerUtils.buildFieldPath(field2));
		
		try{CompilerUtils.buildFieldPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals("I",CompilerUtils.buildFieldSignature(field2));

		try{CompilerUtils.buildFieldSignature(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final Method	callMethod = clazz.getMethod("callMethod",String.class,int[].class); 

		Assert.assertEquals(clazz.getPackage().getName()+"."+clazz.getSimpleName()+"."+callMethod.getName(),CompilerUtils.buildMethodPath(callMethod));

		try{CompilerUtils.buildMethodPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals("(Ljava/lang/String;[I)I",CompilerUtils.buildMethodSignature(callMethod));

		try{CompilerUtils.buildMethodSignature(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final Constructor<?>	constr = clazz.getDeclaredConstructor();

		Assert.assertEquals(clazz.getPackage().getName()+"."+clazz.getSimpleName()+"."+clazz.getSimpleName(),CompilerUtils.buildConstructorPath(constr));

		try{CompilerUtils.buildConstructorPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals("()V",CompilerUtils.buildConstructorSignature(constr));

		try{CompilerUtils.buildConstructorSignature(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void callsAndHeadersTest() throws ContentException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		final Class<?>			clazz = CompilerUtilsRabbitChild.class;
		final Method			m = clazz.getMethod("callMethod",String.class,int[].class);
		final Method			i = clazz.getMethod("callInterface",String.class);
		final Method			s = clazz.getMethod("staticCall");
		final Constructor<?>	c = clazz.getDeclaredConstructor();
		final Field				f = clazz.getField("field2"), fs = clazz.getField("str");
		
		Assert.assertEquals("callMethod .method int public\narg0 .parameter java.lang.String final\narg1 .parameter int[] final\n",CompilerUtils.buildMethodHeader(m));
		Assert.assertEquals("callMethod .method int public\np1 .parameter java.lang.String final\np2 .parameter int[] final\n",CompilerUtils.buildMethodHeader(m,"p1","p2"));
		
		try{CompilerUtils.buildMethodHeader(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.buildMethodHeader(m,(String[])null);
			Assert.fail("Mandatory exception was not detected (null tail arguments)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.buildMethodHeader(m,"p1");
			Assert.fail("Mandatory exception was not detected (different amount of arguments for the method)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.buildMethodHeader(m,null,"p2");
			Assert.fail("Mandatory exception was not detected (null inside arguments)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.buildMethodHeader(m,"","p2");
			Assert.fail("Mandatory exception was not detected (empty inside arguments)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(" invokevirtual chav1961.purelib.basic.CompilerUtilsRabbitChild.callMethod(Ljava/lang/String;[I)I\n",CompilerUtils.buildMethodCall(m));
		Assert.assertEquals(" invokeinterface chav1961.purelib.basic.RabbitInterface.callInterface(Ljava/lang/String;)V\n",CompilerUtils.buildMethodCall(i,RabbitInterface.class));
		Assert.assertEquals(" invokestatic chav1961.purelib.basic.CompilerUtilsRabbitChild.staticCall()V\n",CompilerUtils.buildMethodCall(s));

		try{CompilerUtils.buildMethodCall(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.buildMethodCall(null,RabbitInterface.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.buildMethodCall(m,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(" invokespecial chav1961.purelib.basic.CompilerUtilsRabbitChild.CompilerUtilsRabbitChild()V\n",CompilerUtils.buildConstructorCall(c));

		try{CompilerUtils.buildConstructorCall(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(" getfield chav1961.purelib.basic.CompilerUtilsRabbit.field2\n",CompilerUtils.buildGetter(f));
		Assert.assertEquals(" getstatic chav1961.purelib.basic.CompilerUtilsRabbitChild.str\n",CompilerUtils.buildGetter(fs));

		try{CompilerUtils.buildGetter(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(" putfield chav1961.purelib.basic.CompilerUtilsRabbit.field2\n",CompilerUtils.buildSetter(f));
		Assert.assertEquals(" putstatic chav1961.purelib.basic.CompilerUtilsRabbitChild.str\n",CompilerUtils.buildSetter(fs));

		try{CompilerUtils.buildSetter(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}

class CompilerUtilsRabbit {
	CompilerUtilsRabbit(){}
	int	field1;
	public int field2;
	int call1(){return 0;}
	public int call2(){return 0;}
	protected int callMethod(String s, int[] t){return 0;}
}

interface RabbitInterface {
	void callInterface(String value);
}

class CompilerUtilsRabbitChild extends CompilerUtilsRabbit implements RabbitInterface {
	public static String str;
	CompilerUtilsRabbitChild(){}
	int	field3;
	public int field4;
	int call3(){return 0;}
	public int call4(){return 0;}
	@Override
	public int callMethod(String s, int[] t){return 0;}
	@Override
	public void callInterface(String value){}
	public static void staticCall(){}
}
