package chav1961.purelib.streams.char2byte.asm;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.AsmSyntaxException;

public class InterfaceLineParserTest {
	@Test
	public void emptyInterfaceTest() throws IOException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			ClassLineParserTest.processString(lp,
					 		 " 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.interface public synthetic\n"
							+"Test	.end"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (Constants.ACC_PUBLIC | Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT | Constants.ACC_SYNTHETIC),(Constants.ACC_PUBLIC | Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT | Constants.ACC_SYNTHETIC));
			Assert.assertTrue(clazz.isInterface());
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			ClassLineParserTest.processString(lp,
							 " 			.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 		.interface public\n"
							+"Nested	.interface public\n"
							+"Nested	.end"
							+"Test 		.end"
							);
			Assert.fail("Mandatory exception was not detected (nested classes)");
		} catch (AsmSyntaxException exc) {
		}
	}	
	
	@Test
	public void emptyEntendedAndImportInterfaceTest() throws IOException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			ClassLineParserTest.processString(lp,
							 " 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import java.lang.AutoCloseable\n"
							+"		.import java.lang.Cloneable\n"
							+"Test 	.interface public extends AutoCloseable, Cloneable\n"
							+"Test	.end"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (Constants.ACC_PUBLIC | Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT),(Constants.ACC_PUBLIC | Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT));
			Assert.assertEquals(clazz.getSuperclass(),null);
			Assert.assertArrayEquals(clazz.getInterfaces(),new Class<?>[]{AutoCloseable.class,Cloneable.class});
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			ClassLineParserTest.processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import java.lang.AutoCloseable\n"
							+"		.import java.lang.Cloneable\n"
							+"Test 	.interface public extends AutoCloseable, Throwable\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (implements references to class, not interface)");
		} catch (AsmSyntaxException exc) {
		}
	}

//	@Test
	public void interfaceWithFieldsTest() throws IOException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

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
	public void classWithMethodTest() throws IOException, NoSuchMethodException, SecurityException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			ClassLineParserTest.processString(lp,
							 " 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.interface \n"
							+"m1	.method byte\n"
							+"p1	.parameter String final\n"
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
