package chav1961.purelib.streams.char2byte.asm;


import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.JavaByteCodeConstants;

@Tag("OrdinalTestCategory")
public class InterfaceLineParserTest {
	@Test
	public void emptyInterfaceTest() throws IOException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			ClassLineParserTest.processString(lp,
					 		 " 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.interface public synthetic\n"
							+"Test	.end"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_INTERFACE | JavaByteCodeConstants.ACC_ABSTRACT | JavaByteCodeConstants.ACC_SYNTHETIC),(JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_INTERFACE | JavaByteCodeConstants.ACC_ABSTRACT | JavaByteCodeConstants.ACC_SYNTHETIC));
			Assert.assertTrue(clazz.isInterface());
		}

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			ClassLineParserTest.processString(lp,
							 " 			.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 		.interface public\n"
							+"Nested	.interface public\n"
							+"Nested	.end"
							+"Test 		.end"
							);
			Assert.fail("Mandatory exception was not detected (nested classes)");
		} catch (IOException exc) {
		}
	}	
	
	@Test
	public void emptyEntendedAndImportInterfaceTest() throws IOException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			ClassLineParserTest.processString(lp,
							 " 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import java.lang.AutoCloseable\n"
							+"		.import java.lang.Cloneable\n"
							+"Test 	.interface public extends java.lang.AutoCloseable, java.lang.Cloneable\n"
							+"Test	.end"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_INTERFACE | JavaByteCodeConstants.ACC_ABSTRACT),(JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_INTERFACE | JavaByteCodeConstants.ACC_ABSTRACT));
			Assert.assertEquals(clazz.getSuperclass(),null);
			Assert.assertArrayEquals(clazz.getInterfaces(),new Class<?>[]{AutoCloseable.class,Cloneable.class});
		}

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			ClassLineParserTest.processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import java.lang.AutoCloseable\n"
							+"		.import java.lang.Cloneable\n"
							+"Test 	.interface public extends java.lang.AutoCloseable, java.lang.Throwable\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (implements references to class, not interface)");
		} catch (IOException exc) {
		}
	}

//	@Test
	public void interfaceWithFieldsTest() throws IOException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			ClassLineParserTest.processString(lp,
							 " 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.interface\n"
							+"f1	.field byte\n"
							+"Test	.end"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getFields().length,1);
		}
	}	

	@Test
	public void classWithMethodTest() throws IOException, NoSuchMethodException, SecurityException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			ClassLineParserTest.processString(lp,
							 " 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.interface \n"
							+"m1	.method byte\n"
							+"p1	.parameter java.lang.String final\n"
							+"p2	.parameter long\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("m1",String.class,long.class).getReturnType(),byte.class);
		}
	}
}
