package chav1961.purelib.streams.char2byte.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class InternalUtilsTest {
	public InternalUtilsTest(){}

	@Test
	public void strongTypeSignatures() throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		final Field 			f = InternalUtilsTest.class.getField("testField");
		final Method 			m = InternalUtilsTest.class.getMethod("testMethod",boolean.class,byte.class,char.class,double.class,float.class,int.class,long.class,String.class,short[][].class);
		final Constructor<?>	c = InternalUtilsTest.class.getConstructor();
		
		Assert.assertEquals(InternalUtils.buildSignature(f),"Ljava/lang/String;");
		
		try{InternalUtils.buildSignature((Field)null);
			Assert.fail("Mandatory exception was not detected(null argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(InternalUtils.buildSignature(m),"(ZBCDFIJLjava/lang/String;[[S)V");
		
		try{InternalUtils.buildSignature((Method)null);
			Assert.fail("Mandatory exception was not detected(null argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(InternalUtils.buildSignature(c),"()V");
		
		try{InternalUtils.buildSignature((Constructor<?>)null);
			Assert.fail("Mandatory exception was not detected(null argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void softSignatures() throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		final SyntaxTreeInterface<String>	tree = new AndOrTree<String>(1,16);
		final long					field1 = tree.placeName("java.lang.String",null), field2 = tree.placeName("java.lang.String[]",null), field3 = tree.placeName("int",null);
		final long					returned =  tree.placeName("void",null);
		
		Assert.assertEquals(InternalUtils.buildFieldSignature(tree,field1),"Ljava/lang/String;");
		Assert.assertEquals(InternalUtils.buildFieldSignature(tree,field2),"[Ljava/lang/String;");
		Assert.assertEquals(InternalUtils.buildFieldSignature(tree,field3),"I");
		
		try{InternalUtils.buildFieldSignature(null,field1);
			Assert.fail("Mandatory exception was not detected(null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.buildFieldSignature(tree,-1);
			Assert.fail("Mandatory exception was not detected(non-existent 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(InternalUtils.buildMethodSignature(tree,true,returned,field1,field2,field3),"(Ljava/lang/String;[Ljava/lang/String;I)V");
		
		try{InternalUtils.buildMethodSignature(null,true,returned);
			Assert.fail("Mandatory exception was not detected(null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.buildMethodSignature(tree,true,-1);
			Assert.fail("Mandatory exception was not detected(non-existent 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.buildMethodSignature(tree,true,returned,-1);
			Assert.fail("Mandatory exception was not detected(non-existent var argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	
	
	
	public static void testMethod(boolean value1,byte value2,char value3,double val4,float val5,int val6,long val7,String val8,short[][] val9) {}
	
	public static String testField;
}
