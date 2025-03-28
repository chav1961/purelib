package chav1961.purelib.streams.char2byte.asm;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.JavaByteCodeConstants;
import chav1961.purelib.streams.char2byte.asm.macro.Macros;


@Tag("OrdinalTestCategory")
public class ClassLineParserTest {
	@Test
	public void emptyClassTest() throws IOException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_FINAL | JavaByteCodeConstants.ACC_SYNTHETIC),(JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_FINAL | JavaByteCodeConstants.ACC_SYNTHETIC));
			Assert.assertFalse(clazz.isInterface());
		}

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public private\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public & private)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public protected\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public & protected)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class abstract final\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive abstract & final)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"Nested.class public\n"
							+"Nested.end"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (nested classes)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"		.end"
							);
			Assert.fail("Mandatory exception was not detected (non-labeled end directive)");
		} catch (IOException exc) {
		}
	}
	
	@Test
	public void emptyExtendedClassTest() throws IOException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public extends java.lang.Throwable final\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_FINAL ),(JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_FINAL));
			Assert.assertEquals(clazz.getSuperclass(),Throwable.class);
		}

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public extends java.lang.String final\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (attempt to extends final class)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void emptyImplementedAndImportClassTest() throws IOException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import java.lang.AutoCloseable\n"
							+"		.import java.lang.Cloneable\n"
							+"Test 	.class public abstract extends java.lang.Throwable implements java.lang.AutoCloseable, java.lang.Cloneable\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getModifiers() & (JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_ABSTRACT),(JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_ABSTRACT));
			Assert.assertEquals(clazz.getSuperclass(),Throwable.class);
			Assert.assertArrayEquals(clazz.getInterfaces(),new Class<?>[]{AutoCloseable.class,Cloneable.class});
		}

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.import unknown\n"
							+"Test 	.class public abstract extends java.lang.Throwable implements java.lang.AutoCloseable, java.lang.Cloneable\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (unknown class to import)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract extends java.lang.Throwable implements Unknown\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (unknown class to implements)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,null,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract extends java.lang.Throwable implements java.lang.String\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (implements references to class, not interface)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void classWithFieldsTest() throws IOException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"f1	.field byte public static volatile transient synthetic\n"
							+"f2	.field boolean public static volatile transient synthetic\n"
							+"f3	.field char public static volatile transient synthetic\n"
							+"f4	.field double public static volatile transient synthetic\n"
							+"f5	.field float public static volatile transient synthetic\n"
							+"f6	.field int public static volatile transient synthetic\n"
							+"f7	.field long public static volatile transient synthetic\n"
							+"f8	.field java.lang.String public static volatile transient synthetic\n"
							+"f9	.field short public static volatile transient synthetic\n"
							+"Test	.end"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getFields().length,9);
		}

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"f1	.field byte public private\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public/protected/private)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public final synthetic\n"
							+"f1	.field byte private protected\n"
							+"Test	.end"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public/protected/private)");
		} catch (IOException exc) {
		}
	}	
	
	@Test
	public void classWithMethodTest() throws IOException, NoSuchMethodException, SecurityException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public abstract\n"
							+"p1	.parameter java.lang.String final\n"
							+"p2	.parameter long\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("m1",String.class,long.class).getReturnType(),byte.class);
		}

		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"m1	.method byte public abstract\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (attempt to add abstract method to the non-abstract class)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public protected\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive public/protected/private)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public abstract final\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive abstract/final)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

			processString(lp," 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public abstract\n"
							+"m1	.method byte public abstract static\n"
							+"m1	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (mutually exclusive abstract/static)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void realClassMethodTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer(null);) {
			final LineParser		lp = new LineParser(this.getClass().getClassLoader(),cc,cdr,macros,null);

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
			final LineByLineProcessor	lblp = new LineByLineProcessor((displacement,lineNo,data,from,len)->lp.processLine(displacement, lineNo, data, from, len))) {
			
			lblp.write(rdr);
		} catch (SyntaxException e) {
			throw new IOException(e);
		}
	}

	static Class<?> checkClass(final ClassContainer cc, final String className) throws IOException, ContentException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			cc.dump(baos);
			return ClassContainerTest.loadClass(className, baos);
		}
	}
}
