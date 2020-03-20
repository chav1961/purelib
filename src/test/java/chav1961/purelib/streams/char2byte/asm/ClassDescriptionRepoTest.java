package chav1961.purelib.streams.char2byte.asm;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.char2byte.CompilerUtils;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class ClassDescriptionRepoTest {
	public static final int		testField = 0;	// For testing purposes only!
	public void basicTest() {}	// For testing purposes only!
	
	public ClassDescriptionRepoTest(){}
	
	@Test
	public void ordinalClassTest() throws ContentException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		internalTest(this.getClass(),new Class[0]);
	}

	@Test
	public void innerClassTest() throws ContentException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		internalTest(InnerFakeClass.class,new Class[]{this.getClass()});
	}

	private void internalTest(final Class<?> clazz, final Class<?>[] constr) throws ContentException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final char[]				longClassName = clazz.getName().toCharArray();
		
		cdr.addDescription(clazz,true);
		
		Assert.assertEquals(cdr.getClassDescription(longClassName,0,longClassName.length),clazz);
		
		final Field					f = clazz.getField("testField");
		final char[]				longFieldName = (clazz.getName()+'.'+f.getName()).toCharArray();
		
		Assert.assertEquals(cdr.getFieldDescription(longFieldName,0,longFieldName.length),f);
		
		final Method				m = clazz.getMethod("basicTest");	// It's a name of THIS method!
		final char[]				longMethodName = (clazz.getName()+'.'+m.getName()+CompilerUtils.buildMethodSignature(m)).toCharArray();
		
		Assert.assertEquals(cdr.getMethodDescription(longMethodName,0,longMethodName.length),m);

		final Constructor<?>		c = clazz.getConstructor(constr);
		final char[]				longConstructorName = (clazz.getName()+'.'+clazz.getSimpleName()+CompilerUtils.buildConstructorSignature(c)).toCharArray();

		Assert.assertEquals(cdr.getConstructorDescription(longConstructorName,0,longConstructorName.length),c);
	}
	
	
	@Test
	public void illegallArgumentsTest() throws ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try{cdr.addDescription(null,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");			
		} catch (IllegalArgumentException exc) {
		}

		cdr.addDescription(this.getClass(),true);
		cdr.addDescription(FakeClass.class,true);
		
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
	
	class InnerFakeClass {
		public final int	testField = 10;
		public InnerFakeClass(){}
		public void basicTest(){}
	}
}

class FakeClass {
	@SuppressWarnings("unused")
	private final int	testField = 20;
	
	public void basicTest(){}
}