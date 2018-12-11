package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.char2byte.asm.ClassContainer;


public class ClassContainerTest {
	@Test
	public void emptyClassTest() throws IOException, ContentException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer()) {

			try{cc.getClassName();
				Assert.fail("Mandatory exception was not detected (call getClassName() before setClassName())");
			} catch (IllegalStateException exc) {
			}
			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName("Test",new NameDescriptor()));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getSuperclass(),Object.class);
		}

		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer()) {
			
			cc.setClassName((short) 0x0001,cc.getNameTree().placeName(this.getClass().getPackage().getName(),null),cc.getNameTree().placeName("Test",null));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);

			Assert.assertEquals(cc.getClassName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(loaded.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(loaded.getSuperclass(),Object.class);
		}
	}

	@Test
	public void extendsAndImplementsClassTest() throws IOException, ContentException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			final ClassContainer		cc = new ClassContainer();

			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName("Test",new NameDescriptor()));
			cc.setExtendsClassName(cc.getNameTree().placeName(this.getClass().getName(),new NameDescriptor()));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getSuperclass(),this.getClass());
		}

		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer()) {

			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName("Test",new NameDescriptor()));
			cc.addInterfaceName(cc.getNameTree().placeName(Serializable.class.getName(),new NameDescriptor()));
			cc.addInterfaceName(cc.getNameTree().placeName(AutoCloseable.class.getName(),new NameDescriptor()));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getSuperclass(),Object.class);
			for (Class<?> item : loaded.getInterfaces()) {
				Assert.assertTrue(item == Serializable.class || item == AutoCloseable.class);
			}
		}
	}

//	@Test
	public void fieldClassTest() throws IOException, NoSuchFieldException, SecurityException, ContentException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			final ClassContainer		cc = new ClassContainer();

			cc.setClassName((short) 0x0001,-1,cc.getNameTree().placeName("Test",null));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("byteField",null),cc.getNameTree().placeName("byte",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("charField",null),cc.getNameTree().placeName("char",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("doubleField",null),cc.getNameTree().placeName("double",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("floatField",null),cc.getNameTree().placeName("float",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("intField",null),cc.getNameTree().placeName("int",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("longField",null),cc.getNameTree().placeName("long",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("stringField",null),cc.getNameTree().placeName("java.lang.String",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("stringField2",null),cc.getNameTree().placeName("java.lang.String",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("shortField",null),cc.getNameTree().placeName("short",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("booleanField",null),cc.getNameTree().placeName("boolean",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("intArrayField",null),cc.getNameTree().placeName("int[]",new NameDescriptor()));
			cc.addFieldDescription((short) 0x0001,cc.getNameTree().placeName("stringArrayField",null),cc.getNameTree().placeName("java.lang.String[]",new NameDescriptor()));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getFields().length,12);
			Assert.assertEquals(loaded.getField("stringArrayField").getDeclaringClass(),loaded);
			Assert.assertEquals(loaded.getField("stringArrayField").getType(),String[].class);
		}
	}	
	
	@Test
	public void methodClassTest() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			final ClassContainer		cc = new ClassContainer();

			cc.setClassName((short) 0x0401,0,cc.getNameTree().placeName("Test",new NameDescriptor()));
			cc.addMethodDescription((short) 0x0401,cc.getNameTree().placeName("voidAbstractMethod",new NameDescriptor()),cc.getNameTree().placeName("void",new NameDescriptor())).complete();
			cc.addMethodDescription((short) 0x0401,cc.getNameTree().placeName("voidAbstractMethodWithThrows",new NameDescriptor()),cc.getNameTree().placeName("void",new NameDescriptor()),cc.getNameTree().placeName("java.lang.Throwable",new NameDescriptor())).complete();
			
			long	id;
			final MethodDescriptor	desc = cc.addMethodDescription((short) 0x0009,id = cc.getNameTree().placeName("voidMethodWithThrows",new NameDescriptor()),cc.getNameTree().placeName("void",new NameDescriptor()),cc.getNameTree().placeName("java.lang.Throwable",new NameDescriptor()));
			
			System.err.println(cc.getNameTree().getName(id));
			desc.getBody().putCommand(0,(byte)0xB1);	// Void method with return command only
			desc.complete();
			
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getMethod("voidAbstractMethod").getReturnType(),void.class);
			Assert.assertEquals(loaded.getMethod("voidAbstractMethodWithThrows").getReturnType(),void.class);
			System.err.println("Methods: "+Arrays.toString(loaded.getMethods()));
			Assert.assertEquals(loaded.getMethod("voidMethodWithThrows").getReturnType(),void.class);
		}

		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			final ClassContainer		cc = new ClassContainer();

			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName("Test",new NameDescriptor()));
			
			final MethodDescriptor	desc = cc.addMethodDescription((short) 0x0009,cc.getNameTree().placeName("add",new NameDescriptor()),cc.getNameTree().placeName("int",new NameDescriptor()));
			desc.addParameterDeclaration((short)0x0000,cc.getNameTree().placeName("x",new NameDescriptor()),cc.getNameTree().placeName("int",new NameDescriptor()));
			desc.addParameterDeclaration((short)0x0000,cc.getNameTree().placeName("y",new NameDescriptor()),cc.getNameTree().placeName("int",new NameDescriptor()));
			
			desc.getBody().putCommand(1,(byte)0x1A);	// load x into stack
			desc.getBody().putCommand(1,(byte)0x1b);	// load y into stack
			desc.getBody().putCommand(-1,(byte)0x60);	// add x and y
			desc.getBody().putCommand(-1,(byte)0xAC);	// return int result
			desc.complete();
			
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getMethod("add",int.class,int.class).getReturnType(),int.class);
			Assert.assertEquals(loaded.getMethod("add",int.class,int.class).invoke(null,2,3),Integer.valueOf(5));
		}
	}	

	
	public static Class<?> loadClass(final String className, final ByteArrayOutputStream baos) {
		final TestClassLoader	tcl = new TestClassLoader();
		final byte[]			buffer = baos.toByteArray();

		System.err.println("----------");
		for (int index = 0, maxIndex = buffer.length; index < maxIndex; index++) {
			System.err.print(String.format("%1$02x ",buffer[index]));
			if (index % 16 == 15) {
				System.err.println();
			}
		}
		System.err.println("\n----------");
		for (int index = 0, maxIndex = buffer.length; index < maxIndex; index++) {
			System.err.print(String.format("%1$c",(char)buffer[index]));
			if (index % 16 == 15) {
				System.err.println();
			}
		}
		System.err.println("\n----------");
		
		final Class<?>	result = tcl.defineClass(className,baos.toByteArray()); 
		
		Assert.assertNotNull(result);
		return result;
	}
}


class TestClassLoader extends ClassLoader {
	public TestClassLoader(ClassLoader parent) {
		super(parent);
	}

	public TestClassLoader() {
	}
	
	public Class<?> defineClass(final String name, final byte[] data){
		return defineClass(name, data, 0, data.length);
	}
}