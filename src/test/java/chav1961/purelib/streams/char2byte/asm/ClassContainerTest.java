package chav1961.purelib.streams.char2byte.asm;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.streams.char2byte.asm.ClassContainer;
import chav1961.purelib.testing.TestingUtils;


@Tag("OrdinalTestCategory")
public class ClassContainerTest {
	private static final PrintStream	ps = TestingUtils.err();
	
	@Test
	public void emptyClassTest() throws IOException, ContentException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer(null)) {

			try{cc.getClassName();
				Assert.fail("Mandatory exception was not detected (call getClassName() before setClassName())");
			} catch (IllegalStateException exc) {
			}
			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName((CharSequence)"Test",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getSuperclass(),Object.class);
		}

		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer(null)) {
			
			cc.setClassName((short) 0x0001,cc.getNameTree().placeName((CharSequence)this.getClass().getPackage().getName(),null),cc.getNameTree().placeName("Test",null));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);

			Assert.assertEquals(cc.getClassName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(loaded.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(loaded.getSuperclass(),Object.class);
		}
	}

	@Test
	public void extendsAndImplementsClassTest() throws IOException, ContentException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer(null)) {

			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName((CharSequence)"Test",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.setExtendsClassName(cc.getNameTree().placeName((CharSequence)this.getClass().getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getSuperclass(),this.getClass());
		}

		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer(null)) {

			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName((CharSequence)"Test",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addInterfaceName(cc.getNameTree().placeName((CharSequence)Serializable.class.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addInterfaceName(cc.getNameTree().placeName((CharSequence)AutoCloseable.class.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
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
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer(null)) {

			cc.setClassName((short) 0x0001,-1,cc.getNameTree().placeName((CharSequence)"Test",null));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"byteField",null)
								,cc.getNameTree().placeName((CharSequence)"byte",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"charField",null)
								,cc.getNameTree().placeName((CharSequence)"char",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"doubleField",null)
								,cc.getNameTree().placeName((CharSequence)"double",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"floatField",null)
								,cc.getNameTree().placeName((CharSequence)"float",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"intField",null)
								,cc.getNameTree().placeName((CharSequence)"int",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"longField",null)
								,cc.getNameTree().placeName((CharSequence)"long",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"stringField",null)
								,cc.getNameTree().placeName((CharSequence)"java.lang.String",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"stringField2",null)
								,cc.getNameTree().placeName((CharSequence)"java.lang.String",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"shortField",null)
								,cc.getNameTree().placeName((CharSequence)"short",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"booleanField",null)
								,cc.getNameTree().placeName((CharSequence)"boolean",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"intArrayField",null)
								,cc.getNameTree().placeName((CharSequence)"int[]",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addFieldDescription((short) 0x0001, cc.getNameTree().placeName((CharSequence)"stringArrayField",null)
								,cc.getNameTree().placeName((CharSequence)"java.lang.String[]",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
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
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer(null)) {

			cc.getNameTree().placeName((CharSequence)"double",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			cc.getNameTree().placeName((CharSequence)"long",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			cc.getNameTree().placeName((CharSequence)"this",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			cc.setClassName((short) 0x0401,0,cc.getNameTree().placeName((CharSequence)"Test",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			cc.addMethodDescription((short) 0x0401, (short)0
						,cc.getNameTree().placeName((CharSequence)"voidAbstractMethod",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
						,cc.getNameTree().placeName((CharSequence)"void",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))).complete();
			cc.addMethodDescription((short) 0x0401, (short)0
						,cc.getNameTree().placeName((CharSequence)"voidAbstractMethodWithThrows",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
						,cc.getNameTree().placeName((CharSequence)"void",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
						,cc.getNameTree().placeName((CharSequence)"java.lang.Throwable",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))).complete();
			
			long	id;
			final MethodDescriptor	desc = cc.addMethodDescription((short) 0x0009, (short)0
											,id = cc.getNameTree().placeName((CharSequence)"voidMethodWithThrows"
											,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
											,cc.getNameTree().placeName((CharSequence)"void",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
											,cc.getNameTree().placeName((CharSequence)"java.lang.Throwable",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			
			ps.println(cc.getNameTree().getName(id));
			desc.getBody().putCommand(0,(byte)0xB1);	// Void method with return command only
			desc.complete();
			
			cc.dump(baos);
			
			final Class<?>	loaded = loadClass(cc.getClassName(),baos);
			
			Assert.assertEquals(cc.getClassName(),"Test");
			Assert.assertEquals(loaded.getName(),"Test");
			Assert.assertEquals(loaded.getMethod("voidAbstractMethod").getReturnType(),void.class);
			Assert.assertEquals(loaded.getMethod("voidAbstractMethodWithThrows").getReturnType(),void.class);
			ps.println("Methods: "+Arrays.toString(loaded.getMethods()));
			Assert.assertEquals(loaded.getMethod("voidMethodWithThrows").getReturnType(),void.class);
		}

		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ClassContainer		cc = new ClassContainer(null)) {

			cc.setClassName((short) 0x0001,0,cc.getNameTree().placeName((CharSequence)"Test",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			
			final MethodDescriptor	desc = cc.addMethodDescription((short) 0x0009, (short)0
												,cc.getNameTree().placeName((CharSequence)"add",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
												,cc.getNameTree().placeName((CharSequence)"int",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			desc.addParameterDeclaration((short)0x0000, cc.getNameTree().placeName((CharSequence)"x",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
												,cc.getNameTree().placeName((CharSequence)"int",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			desc.addParameterDeclaration((short)0x0000, cc.getNameTree().placeName((CharSequence)"y",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID))
												,cc.getNameTree().placeName((CharSequence)"int",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			
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

		ps.println("----------");
		for (int index = 0, maxIndex = buffer.length; index < maxIndex; index++) {
			ps.print(String.format("%1$02x ",buffer[index]));
			if (index % 16 == 15) {
				ps.println();
			}
		}
		ps.println("\n----------");
		for (int index = 0, maxIndex = buffer.length; index < maxIndex; index++) {
			ps.print(String.format("%1$c",(char)buffer[index]));
			if (index % 16 == 15) {
				ps.println();
			}
		}
		ps.println("\n----------");
		
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
