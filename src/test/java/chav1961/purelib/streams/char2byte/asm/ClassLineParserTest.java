package chav1961.purelib.streams.char2byte.asm;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.exceptions.SyntaxException;


public class ClassLineParserTest {
	@Test
	public void emptyClassTest() throws IOException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (Constants.ACC_PUBLIC | Constants.ACC_FINAL | Constants.ACC_SYNTHETIC),(Constants.ACC_PUBLIC | Constants.ACC_FINAL | Constants.ACC_SYNTHETIC));
			Assert.assertFalse(clazz.isInterface());
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public private\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public & private)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public protected\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public & protected)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class abstract final\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive abstract & final)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"Nested.class public\n"
							+"Nested.end"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (nested classes)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"		.end"
							);
			Assert.fail("Mandatory exception was not detected (non-labeled end directive)");
		} catch (AsmSyntaxException exc) {
		}
	}
	
	@Test
	public void emptyExtendedClassTest() throws IOException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public extends java.lang.Throwable final\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (Constants.ACC_PUBLIC | Constants.ACC_FINAL ),(Constants.ACC_PUBLIC | Constants.ACC_FINAL));
			Assert.assertEquals(clazz.getSuperclass(),Throwable.class);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public extends java.lang.String final\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (attempt to extends final class)");
		} catch (AsmSyntaxException exc) {
		}
	}

	@Test
	public void emptyImplementedAndImportClassTest() throws IOException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import java.lang.AutoCloseable\n"
							+"		.import java.lang.Cloneable\n"
							+"Test 	.class public abstract extends Throwable implements AutoCloseable, Cloneable\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (Constants.ACC_PUBLIC | Constants.ACC_ABSTRACT),(Constants.ACC_PUBLIC | Constants.ACC_ABSTRACT));
			Assert.assertEquals(clazz.getSuperclass(),Throwable.class);
			Assert.assertArrayEquals(clazz.getInterfaces(),new Class<?>[]{AutoCloseable.class,Cloneable.class});
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import unknown\n"
							+"Test 	.class public abstract extends Throwable implements AutoCloseable, Cloneable\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (unknown class to import)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract extends Throwable implements Unknown\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (unknown class to implements)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract extends Throwable implements java.lang.String\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (implements references to class, not interface)");
		} catch (AsmSyntaxException exc) {
		}
	}

	@Test
	public void classWithFieldsTest() throws IOException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"f1	.field byte public static volatile transient synthetic\n"
							+"f2	.field boolean public static volatile transient synthetic\n"
							+"f3	.field char public static volatile transient synthetic\n"
							+"f4	.field double public static volatile transient synthetic\n"
							+"f5	.field float public static volatile transient synthetic\n"
							+"f6	.field int public static volatile transient synthetic\n"
							+"f7	.field long public static volatile transient synthetic\n"
							+"f8	.field String public static volatile transient synthetic\n"
							+"f9	.field short public static volatile transient synthetic\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getFields().length,9);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"f1	.field byte public private\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public/protected/private)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"f1	.field byte private protected\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public/protected/private)");
		} catch (AsmSyntaxException exc) {
		}
	}	
	
	@Test
	public void classWithMethodTest() throws IOException, NoSuchMethodException, SecurityException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public abstract\n"
							+"p1	.parameter String final\n"
							+"p2	.parameter long\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("m1",String.class,long.class).getReturnType(),byte.class);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"m1	.method byte public abstract\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (attempt to add abstract method to the non-abstract class)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public protected\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public/protected/private)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public abstract final\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive abstract/final)");
		} catch (AsmSyntaxException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public abstract static\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive abstract/static)");
		} catch (AsmSyntaxException exc) {
		}
	}

	@Test
	public void realClassMethodTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(null,cc);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"p2	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload_0\n"
							+"		iload_1\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("add",int.class,int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class,int.class).invoke(0,2,3),Integer.valueOf(5));
		}
	}	
	
	static void processString(final LineParser lp, final String source) throws IOException {
		try(final Reader				rdr = new StringReader(source);
			final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,len)->lp.processLine(lineNo, data, from, len))) {
			
			lblp.write(rdr);
		} catch (SyntaxException e) {
			throw new IOException(e);
		}
	}

	static Class<?> checkClass(final ClassContainer cc, final String className) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			cc.dump(baos);
			return ClassContainerTest.loadClass(className, baos);
		}
	}
}
