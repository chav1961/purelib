package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.asm.ClassContainer;
import chav1961.purelib.streams.char2byte.asm.macro.Macros;

interface ByteCodeTestInterface {
	void callInterfaceVoid(long parameter);
	int callInterfaceInt(long parameter);
}

public class ByteCodeLineParserTest implements ByteCodeTestInterface {
	public static int callTest() {return 666;}	// Do not remove - need for test purposes! 

	public static void callStaticVoid(final long parameter) {}
	public static int callStaticInt(final long parameter) {return (int)parameter;}

	public void callVirtualVoid(final long parameter) {}
	public int callVirtualInt(final long parameter) {return (int)parameter;}

	@Override public void callInterfaceVoid(final long parameter) {}
	@Override public int callInterfaceInt(final long parameter) {return (int)parameter;}
	
	@Test
	public void localAddressTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.source \"file:./x\"\n"
							+"Test 	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"p2	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		iload p2\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("add",int.class,int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class,int.class).invoke(null,2,3),Integer.valueOf(5));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test2	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"p2	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload 0\n"
							+"		iload p2\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test2	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test2");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test2");
			Assert.assertEquals(clazz.getMethod("add",int.class,int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class,int.class).invoke(null,2,3),Integer.valueOf(5));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"p2	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload 100\n"
							+"		iload p2\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (too big index for frame size)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"p2	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		iload p3\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (no-existent variable)");
		} catch (IOException exc) {
		}
	}	

	@Test
	public void localValueTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		bipush 10\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("add",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class).invoke(null,2),Integer.valueOf(12));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test2	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		bipush 0b1010\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test2	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test2");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test2");
			Assert.assertEquals(clazz.getMethod("add",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class).invoke(null,2),Integer.valueOf(12));
		}
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test3	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		bipush 0x0A\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test3	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test3");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test3");
			Assert.assertEquals(clazz.getMethod("add",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class).invoke(null,2),Integer.valueOf(12));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test4	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		bipush 012\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test4	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test4");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test4");
			Assert.assertEquals(clazz.getMethod("add",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class).invoke(null,2),Integer.valueOf(12));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test5	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		bipush (2+1)*4-2\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test5	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test5");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test5");
			Assert.assertEquals(clazz.getMethod("add",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class).invoke(null,2),Integer.valueOf(12));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test6	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		sipush (2+1)*4-2\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test6	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test6");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test6");
			Assert.assertEquals(clazz.getMethod("add",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class).invoke(null,2),Integer.valueOf(12));
		}
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test4	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		bipush 999\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test4	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (too big value for frame size)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test4	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int final\n"
							+"		.stack 5\n"
							+"		iload p1\n"
							+"		bipush 0z\n"
							+"		iadd\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test4	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (unparsed tail in the string)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void arrayOfPrimitiveTypeTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"getInt.method int[] public static\n"
							+"		.stack 5\n"
							+"		bipush 4\n"
							+"		newarray int\n"
							+"		areturn\n"
							+"getInt.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("getInt").getReturnType(),int[].class);
			Assert.assertEquals(((int[])clazz.getMethod("getInt").invoke(null)).length,4);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"getInt.method int[] public static\n"
							+"		.stack 5\n"
							+"		bipush 4\n"
							+"		newarray string\n"
							+"		areturn\n"
							+"getInt.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (illegal type for the command)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void arrayOfreferencedTypeTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"get	.method java.lang.String[] public static\n"
							+"		.stack 5\n"
							+"		bipush 4\n"
							+"		anewarray java.lang.String\n"
							+"		areturn\n"
							+"get	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),String[].class);
			Assert.assertEquals(((String[])clazz.getMethod("get").invoke(null)).length,4);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"getInt.method int[] public static\n"
							+"		.stack 5\n"
							+"		bipush 4\n"
							+"		newarray java.lang.String\n"
							+"		areturn\n"
							+"getInt.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (illegal type for the command)");
		} catch (IOException exc) {
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"get	.method java.lang.String[][] public static\n"
							+"		.stack 5\n"
							+"		iconst_3\n"
							+"		iconst_4\n"
							+"		multianewarray java.lang.String[][],2\n"
							+"		areturn\n"
							+"get	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),String[][].class);
			Assert.assertEquals(((String[][])clazz.getMethod("get").invoke(null)).length,3);
		}
	}
	
	@Test
	public void incrementTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"add	.method int public static\n"
							+"p1	.parameter int\n"
							+"		.stack 5\n"
							+"		iinc p1,10\n"
							+"		iload_0\n"
							+"		ireturn\n"
							+"add	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("add",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("add",int.class).invoke(null,2),Integer.valueOf(12));
		}
	}

	@Test
	public void constantTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"get	.method int public static\n"
							+"		.stack 5\n"
							+"		ldc 10\n"
							+"		ireturn\n"
							+"get	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("get").invoke(null),Integer.valueOf(10));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test2	.class public\n"
							+"get	.method float public static\n"
							+"		.stack 5\n"
							+"		ldc_w 10.0f\n"
							+"		freturn\n"
							+"get	.end\n"
							+"Test2	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test2");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test2");
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),float.class);
			Assert.assertEquals(clazz.getMethod("get").invoke(null),Float.valueOf(10));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test3	.class public\n"
							+"get	.method long public static\n"
							+"		.stack 8\n"
							+"		ldc2_w 10L\n"
							+"		lreturn\n"
							+"get	.end\n"
							+"Test3	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test3");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test3");
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),long.class);
			Assert.assertEquals(clazz.getMethod("get").invoke(null),Long.valueOf(10));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test4	.class public\n"
							+"get	.method double public static\n"
							+"		.stack 5\n"
							+"		ldc2_w 10.0\n"
							+"		dreturn\n"
							+"get	.end\n"
							+"Test4	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test4");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test4");
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),double.class);
			Assert.assertEquals(clazz.getMethod("get").invoke(null),Double.valueOf(10));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test5	.class public\n"
							+"get	.method java.lang.String public static\n"
							+"		.stack 5\n"
							+"		ldc_w \"mzinana\"\n"
							+"		areturn\n"
							+"get	.end\n"
							+"Test5	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test5");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test5");
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),String.class);
			Assert.assertEquals(clazz.getMethod("get").invoke(null),"mzinana");
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"get	.method long public static\n"
							+"		.stack 5\n"
							+"		ldc 10L\n"
							+"		lreturn\n"
							+"get	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (illegal constant type)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"get	.method long public static\n"
							+"		.stack 5\n"
							+"		ldc2_w 10\n"
							+"		lreturn\n"
							+"get	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (illegal constant type)");
		} catch (IOException exc) {
		}
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"get	.method String public static\n"
							+"		.stack 5\n"
							+"		ldc2_w \"unknown\"\n"
							+"		areturn\n"
							+"get	.end\n"
							+"Test	.end\n"
							);
			Assert.fail("Mandatory exception was not detected (illegal constant type)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void brunchTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"abs	.method int public static\n"
							+"p1	.parameter int\n"
							+"		.stack 5\n"
							+"		iload_0\n"
							+"		dup\n"
							+"		ifgt skip\n"
							+"		ineg\n"
							+"skip:	ireturn\n"
							+"abs	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("abs",int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("abs",int.class).invoke(null,10),Integer.valueOf(10));
			Assert.assertEquals(clazz.getMethod("abs",int.class).invoke(null,-10),Integer.valueOf(10));
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"val	.method int public static\n"
							+"p1	.parameter int\n"
							+"p2	.parameter int\n"
							+"		.stack 5\n"
							+"		iload_0\n"
							+"		iload_1\n"
							+"		isub\n"
							+"		ifgt skip\n"
							+"		iconst_1\n"
							+"		goto exit\n"
							+"skip:	iconst_m1\n"
							+"exit:	ireturn\n"
							+"val	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("val",int.class,int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("val",int.class,int.class).invoke(null,2,3),Integer.valueOf(1));
			Assert.assertEquals(clazz.getMethod("val",int.class,int.class).invoke(null,3,2),Integer.valueOf(-1));
		}
	}

	@Test
	public void java_1_8_Test() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"		.version 1.8\n"
							+"Test 	.class public\n"
							+"val	.method int public static\n"
							+"p1	.parameter int\n"
							+"p2	.parameter int\n"
							+"		.stack 5\n"
							+"		iload_0\n"
							+"		iload_1\n"
							+"		isub\n"
							+"		ifgt skip\n"
							+"		iconst_1\n"
							+"		goto_w exit\n"
							+"skip:	iconst_m1\n"
							+"exit:	ireturn\n"
							+"val	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("val",int.class,int.class).getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("val",int.class,int.class).invoke(null,2,3),Integer.valueOf(1));
			Assert.assertEquals(clazz.getMethod("val",int.class,int.class).invoke(null,3,2),Integer.valueOf(-1));
		}		
	}	
	
	@Test
	public void constructorTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, InstantiationException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 		.class public\n"
							+"Test		.method void public\n"
							+"			.stack 2\n"
							+"			aload_0\n"
							+"			invokespecial java.lang.Object.Object()V\n"
							+"			return\n"
							+"Test		.end\n"
							+"Test		.method void public\n"
							+"p1		.parameter int\n"
							+"			.stack 2\n"
							+"			aload_0\n"
							+"			invokespecial java.lang.Object.Object()V\n"
							+"			return\n"
							+"Test		.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getConstructors().length,2);
			Assert.assertNotNull(clazz.newInstance());
		}
	}
	
	@Test
	public void fieldAccessTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, InstantiationException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 		.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 	.class public\n"
							+"val	.field int public static\n"
							+"set	.method void public static\n"
							+"p1	.parameter int\n"
							+"		.stack 5\n"
							+"		iload_0\n"
							+"		putstatic val\n"
							+"		return\n"
							+"set	.end\n"
							+"get	.method int public static\n"
							+"		.stack 5\n"
							+"		getstatic val\n"
							+"		ireturn\n"
							+"get	.end\n"
							+"Test	.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			System.err.println("Con=="+Arrays.toString(clazz.getFields()));
			Assert.assertEquals(clazz.getField("val").getType(),int.class);
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("set",int.class).getReturnType(),void.class);
			clazz.getMethod("set",int.class).invoke(null,666);
			Assert.assertEquals(((Integer)clazz.getMethod("get").invoke(null)).intValue(),666);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
							+"Test 		.class public\n"
							+"val		.field int public\n"
							+"Test		.method void public\n"
							+"			.stack 2\n"
							+"			aload_0\n"
							+"			invokespecial java.lang.Object.Object()V\n"
							+"			return\n"
							+"Test		.end\n"
							+"set		.method void public\n"
							+"p1		.parameter int\n"
							+"			.stack 5\n"
							+"			aload_0\n"
							+"			iload_1\n"
							+"			putfield val\n"
							+"			return\n"
							+"set		.end\n"
							+"get		.method int public\n"
							+"			.stack 5\n"
							+"			aload_0\n"
							+"			getfield val\n"
							+"			ireturn\n"
							+"get		.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getField("val").getType(),int.class);
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("set",int.class).getReturnType(),void.class);
			
			final Object	test = clazz.newInstance();
			clazz.getMethod("set",int.class).invoke(test,666);
			Assert.assertEquals(((Integer)clazz.getMethod("get").invoke(test)).intValue(),666);
		}
	}

	@Test
	public void invocationTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, InstantiationException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
						    +" 			.import "+this.getClass().getPackage().getName()+".PseudoTestCheckInterface\n"
							+"Test 		.class public implements "+this.getClass().getPackage().getName()+".PseudoTestCheckInterface\n"
							+"Test		.method void public\n"
							+"			.stack 2\n"
							+"			aload this\n"
							+"			invokespecial java.lang.Object.Object()V\n"
							+"			return\n"
							+"Test		.end\n"
							+"call1		.method int static public\n"
							+"			.stack 2\n"
							+"			invokestatic "+this.getClass().getName()+".callTest()I\n"
							+"			ireturn\n"
							+"call1		.end\n"
							+"call2		.method int static public\n"
							+"			.stack 2\n"
							+"			invokestatic call1()I\n"
							+"			ireturn\n"
							+"call2		.end\n"
							+"call3		.method int public\n"
							+"			.stack 2\n"
							+"			invokestatic call2()I\n"
							+"			ireturn\n"
							+"call3		.end\n"
							+"call4		.method int public\n"
							+"			.stack 2\n"
							+"			aload_0\n"
							+"			invokevirtual call3()I\n"
							+"			ireturn\n"
							+"call4		.end\n"
							+"get		.method int public\n"
							+"			.stack 2\n"
							+"			aload this\n"
							+"			invokevirtual call4()I\n"
							+"			ireturn\n"
							+"get		.end\n"
							+"set		.method void public\n"
							+"value		.parameter int\n"
							+"			.stack 1\n"
							+"			return\n"
							+"set		.end\n"
							+"call5		.method int public\n"
							+"			.stack 3\n"
							+"			aload this\n"
							+"			invokeinterface "+this.getClass().getPackage().getName()+".PseudoTestCheckInterface.get()I\n"
							+"			aload this\n"
							+"			iconst_1\n"
							+"			invokeinterface "+this.getClass().getPackage().getName()+".PseudoTestCheckInterface.set(I)V\n"
							+"			ireturn\n"
							+"call5		.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("call1").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("call2").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("call3").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("call4").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("get").getReturnType(),int.class);
			Assert.assertEquals(clazz.getMethod("call5").getReturnType(),int.class);
			
			Assert.assertEquals(((Integer)clazz.getMethod("call1").invoke(null)).intValue(),666);
			Assert.assertEquals(((Integer)clazz.getMethod("call2").invoke(null)).intValue(),666);
			
			final Object	test = clazz.newInstance();
			Assert.assertEquals(((Integer)clazz.getMethod("call3").invoke(test)).intValue(),666);
			Assert.assertEquals(((Integer)clazz.getMethod("call4").invoke(test)).intValue(),666);
			Assert.assertEquals(((Integer)clazz.getMethod("get").invoke(test)).intValue(),666);
			Assert.assertEquals(((Integer)clazz.getMethod("call5").invoke(test)).intValue(),666);
		}
	}

	@Test
	public void switchTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, InstantiationException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
							+"Test 		.class public\n"
							+"call		.method int static public\n"
							+"p1		.parameter int\n"
							+"			.stack 5\n"
							+"			iload_0\n"
							+"			tableswitch\n"
							+"				10,L1\n"
							+"				11,L2\n"
							+"				.default L3\n"
							+"			.end\n"
							+"L1:		iconst_0\n"
							+"			ireturn\n"
							+"L2:		iconst_1\n"
							+"			ireturn\n"
							+"L3:		iconst_2\n"
							+"			ireturn\n"
							+"call		.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("call",int.class).getReturnType(),int.class);
			Assert.assertEquals(((Integer)clazz.getMethod("call",int.class).invoke(null,10)).intValue(),0);
			Assert.assertEquals(((Integer)clazz.getMethod("call",int.class).invoke(null,11)).intValue(),1);
			Assert.assertEquals(((Integer)clazz.getMethod("call",int.class).invoke(null,12)).intValue(),2);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
							+"Test 		.class public\n"
							+"call		.method int static public\n"
							+"p1		.parameter int\n"
							+"			.stack 5\n"
							+"			iload_0\n"
							+"			lookupswitch\n"
							+"				10,L1\n"
							+"				11,L2\n"
							+"				.default L3\n"
							+"			.end\n"
							+"L1:		iconst_0\n"
							+"			ireturn\n"
							+"L2:		iconst_1\n"
							+"			ireturn\n"
							+"L3:		iconst_2\n"
							+"			ireturn\n"
							+"call		.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("call",int.class).getReturnType(),int.class);
			Assert.assertEquals(((Integer)clazz.getMethod("call",int.class).invoke(null,10)).intValue(),0);
			Assert.assertEquals(((Integer)clazz.getMethod("call",int.class).invoke(null,11)).intValue(),1);
			Assert.assertEquals(((Integer)clazz.getMethod("call",int.class).invoke(null,12)).intValue(),2);
		}
	}

	@Test
	public void tryCatchTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, InstantiationException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
						    +" 			.import "+Exception.class.getName()+"\n"
							+"Test 		.class public\n"
							+"call		.method int static public\n"
							+"p1		.parameter java.lang.Throwable\n"
							+"			.stack 5\n"
							+"			.try\n"
							+"			aload_0\n"
							+"			athrow\n"	
							+"			.catch java.lang.Exception\n"
							+"			pop\n"
							+"			iconst_1\n"
							+"			ireturn\n"
							+"			.catch java.lang.Throwable\n"
							+"			pop\n"
							+"			iconst_2\n"
							+"			ireturn\n"
							+"			.endtry\n"		
							+"call		.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("call",Throwable.class).getReturnType(),int.class);
			Assert.assertEquals(((Integer)clazz.getMethod("call",Throwable.class).invoke(null,new RuntimeException())).intValue(),1);
			Assert.assertEquals(((Integer)clazz.getMethod("call",Throwable.class).invoke(null,new Throwable())).intValue(),2);
		}
	}

	@Test
	public void nestedDescriptionTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, InstantiationException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
						    +" 			.import "+Exception.class.getName()+"\n"
							+"Test 		.class public\n"
							+"call		.method int static public\n"
							+"p1		.parameter java.lang.Throwable\n"
							+"			.stack 5\n"
							+"			.begin\n"
							+"var1		.var int\n"
							+"			.begin\n"
							+"var2		.var int\n"
							+"			iconst_0\n"
							+"			.end\n"							
							+"			.end\n"							
							+"			ireturn\n"
							+"call		.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("call",Throwable.class).getReturnType(),int.class);
		}
	}

	@Test
	public void invokeTest() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, InstantiationException, ContentException {
		final ClassDescriptionRepo	cdr = new ClassDescriptionRepo();
		final SyntaxTreeInterface<Macros>	macros = new AndOrTree<>();
		
		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
						    +" 			.import "+Exception.class.getName()+"\n"
							+"Test 		.class public\n"
							+"callVoid	.method void static public\n"
							+"p1		.parameter java.lang.Throwable\n"
							+"			.stack 5\n"
							+"			.begin\n"
							+"			lconst_1\n"
							+"			invokestatic "+this.getClass().getName()+".callStaticVoid(J)V\n"
							+"			return\n"
							+"			.end\n"
							+"callVoid	.end\n"
							+"callInt	.method int static public\n"
							+"p1		.parameter java.lang.Throwable\n"
							+"			.stack 5\n"
							+"			.begin\n"
							+"			lconst_1\n"
							+"			invokestatic "+this.getClass().getName()+".callStaticInt(J)I\n"
							+"			ireturn\n"
							+"			.end\n"
							+"callInt	.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("callVoid",Throwable.class).getReturnType(),void.class);
			Assert.assertEquals(clazz.getMethod("callInt",Throwable.class).getReturnType(),int.class);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
						    +" 			.import "+Exception.class.getName()+"\n"
							+"Test 		.class public\n"
							+"callVoid	.method void static public\n"
							+"p1		.parameter "+this.getClass().getName()+"\n"
							+"			.stack 5\n"
							+"			.begin\n"
							+"			aload p1\n"
							+"			lconst_1\n"
							+"			invokevirtual "+this.getClass().getName()+".callVirtualVoid(J)V\n"
							+"			return\n"
							+"			.end\n"
							+"callVoid	.end\n"
							+"callInt	.method int static public\n"
							+"p1		.parameter "+this.getClass().getName()+"\n"
							+"			.stack 5\n"
							+"			.begin\n"
							+"			aload p1\n"
							+"			lconst_1\n"
							+"			invokevirtual "+this.getClass().getName()+".callVirtualInt(J)I\n"
							+"			ireturn\n"
							+"			.end\n"
							+"callInt	.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("callVoid",this.getClass()).getReturnType(),void.class);
			Assert.assertEquals(clazz.getMethod("callInt",this.getClass()).getReturnType(),int.class);
		}

		try(final ClassContainer	cc = new ClassContainer();) {
			final LineParser		lp = new LineParser(cc,cdr,macros,null);

			ClassLineParserTest.processString(lp,
							" 			.package "+this.getClass().getPackage().getName()+"\n"
						    +" 			.import "+this.getClass().getName()+"\n"
						    +" 			.import "+ByteCodeTestInterface.class.getName()+"\n"
						    +" 			.import "+Exception.class.getName()+"\n"
							+"Test 		.class public\n"
							+"callVoid	.method void static public\n"
							+"p1		.parameter "+this.getClass().getName()+"\n"
							+"			.stack 5\n"
							+"			.begin\n"
							+"			aload p1\n"
							+"			lconst_1\n"
							+"			invokeinterface "+ByteCodeTestInterface.class.getName()+".callInterfaceVoid(J)V\n"
							+"			return\n"
							+"			.end\n"
							+"callVoid	.end\n"
							+"callInt	.method int static public\n"
							+"p1		.parameter "+this.getClass().getName()+"\n"
							+"			.stack 5\n"
							+"			.begin\n"
							+"			aload p1\n"
							+"			lconst_1\n"
							+"			invokeinterface "+ByteCodeTestInterface.class.getName()+".callInterfaceInt(J)I\n"
							+"			ireturn\n"
							+"			.end\n"
							+"callInt	.end\n"
							+"Test		.end\n"
							);
			final Class<?>	clazz = ClassLineParserTest.checkClass(cc,this.getClass().getPackage().getName()+".Test");
			
			Assert.assertEquals(clazz.getName(),this.getClass().getPackage().getName()+".Test");
			Assert.assertEquals(clazz.getMethod("callVoid",this.getClass()).getReturnType(),void.class);
			Assert.assertEquals(clazz.getMethod("callInt",this.getClass()).getReturnType(),int.class);
		}
	}
}
