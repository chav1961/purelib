package chav1961.purelib.streams.char2byte.asm;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;

public class ClassDescriptionRepoTest {
	public static final int		testField = 0;
	
	public ClassDescriptionRepoTest(){}

//	@Test
	public void basicTest() throws ContentException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final char[]				shortClassName = this.getClass().getSimpleName().toCharArray(), longClassName = this.getClass().getName().toCharArray();
		
		cdr.addDescription(this.getClass());
		
		Assert.assertEquals(cdr.getClassDescription(shortClassName,0,shortClassName.length),this.getClass());
		Assert.assertEquals(cdr.getClassDescription(longClassName,0,longClassName.length),this.getClass());
		
		final Field					f = this.getClass().getField("testField");
		final char[]				shortFieldName = f.getName().toCharArray(), longFieldName = (this.getClass().getName()+'.'+f.getName()).toCharArray();
		
		Assert.assertEquals(cdr.getFieldDescription(shortFieldName,0,shortFieldName.length),f);
		Assert.assertEquals(cdr.getFieldDescription(longFieldName,0,longFieldName.length),f);
		
		final Method				m = this.getClass().getMethod("basicTest");	// It's a name of THIS method!
		final char[]				shortMethodName = InternalUtils.buildSignature(m).toCharArray(), longMethodName = (this.getClass().getName()+'.'+InternalUtils.buildSignature(m)).toCharArray();
		
		Assert.assertEquals(cdr.getMethodDescription(shortMethodName,0,shortMethodName.length),m);
		Assert.assertEquals(cdr.getMethodDescription(longMethodName,0,longMethodName.length),m);

		final Constructor			c = this.getClass().getConstructor();
		final char[]				shortConstructorName = InternalUtils.buildSignature(c).toCharArray(), longConstructorName = (this.getClass().getName()+'.'+InternalUtils.buildSignature(c)).toCharArray();

		Assert.assertEquals(cdr.getConstructorDescription(shortConstructorName,0,shortConstructorName.length),c);
		Assert.assertEquals(cdr.getConstructorDescription(longConstructorName,0,longConstructorName.length),c);
	}
	
	@Test
	public void illegallArgumentsTest() throws ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try{cdr.addDescription(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");			
		} catch (IllegalArgumentException exc) {
		}

		cdr.addDescription(this.getClass());
		cdr.addDescription(FakeClass.class);
		
		try{cdr.getClassDescription(null,0,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");			
		} catch (IllegalArgumentException exc) {
		}
		
		try{cdr.getClassDescription("test".toCharArray(),-1,1);
			Assert.fail("Mandatory exception was not detected (outside the bound 2-nd argument)");			
		} catch (IllegalArgumentException exc) {
		}
		try{cdr.getClassDescription("test".toCharArray(),10,1);
			Assert.fail("Mandatory exception was not detected (outside the bound 2-nd argument)");			
		} catch (IllegalArgumentException exc) {
		}

		try{cdr.getClassDescription("test".toCharArray(),0,11);
			Assert.fail("Mandatory exception was not detected (outside the bound 3-rd argument)");			
		} catch (IllegalArgumentException exc) {
		}
		try{cdr.getClassDescription("test".toCharArray(),0,10);
			Assert.fail("Mandatory exception was not detected (outside the bound 3-rd argument)");			
		} catch (IllegalArgumentException exc) {
		}
	
		try{cdr.getClassDescription("unknown".toCharArray(),0,7);
			Assert.fail("Mandatory exception was not detected (simple name not found)");			
		} catch (ContentException exc) {
		}
		try{cdr.getClassDescription("test.unknown".toCharArray(),0,12);
			Assert.fail("Mandatory exception was not detected (qualified name not found)");			
		} catch (ContentException exc) {
		}
		try{cdr.getClassDescription("basicTest()V".toCharArray(),0,12);
			Assert.fail("Mandatory exception was not detected (method instead of class)");			
		} catch (ContentException exc) {
		}		
		try{cdr.getMethodDescription("basicTest()V".toCharArray(),0,12);
			Assert.fail("Mandatory exception was not detected (ambigious name)");			
		} catch (ContentException exc) {
		}		
	}
}

class FakeClass {
	public void basicTest(){}
}