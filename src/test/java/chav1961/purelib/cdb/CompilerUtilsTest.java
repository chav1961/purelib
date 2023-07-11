package chav1961.purelib.cdb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;

public class CompilerUtilsTest {
	public static final int		testStatic = 0;
	static final int			testNonPublicStatic = 0;
	public final String			testInstance = "";

	public static void staticMethod(int x) {}
	String staticNonPublicMethod(int x) {return null;}
	
	@Test
	public void defineClassTypeAndRepresentationTest() {
		Assert.assertEquals(CompilerUtils.CLASSTYPE_BOOLEAN, CompilerUtils.defineClassType(boolean.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_BYTE, CompilerUtils.defineClassType(byte.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_CHAR, CompilerUtils.defineClassType(char.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE, CompilerUtils.defineClassType(double.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT, CompilerUtils.defineClassType(float.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT, CompilerUtils.defineClassType(int.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG, CompilerUtils.defineClassType(long.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE, CompilerUtils.defineClassType(String.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_SHORT, CompilerUtils.defineClassType(short.class));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_VOID, CompilerUtils.defineClassType(void.class));

		try{CompilerUtils.defineClassType(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals("boolean", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_BOOLEAN));
		Assert.assertEquals("byte", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_BYTE));
		Assert.assertEquals("char", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_CHAR));
		Assert.assertEquals("double", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_DOUBLE));
		Assert.assertEquals("float", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_FLOAT));
		Assert.assertEquals("int", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_INT));
		Assert.assertEquals("long", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_LONG));
		Assert.assertEquals("ref", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_REFERENCE));
		Assert.assertEquals("short", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_SHORT));
		Assert.assertEquals("void", CompilerUtils.getClassTypeRepresentation(CompilerUtils.CLASSTYPE_VOID));

		Assert.assertNull(CompilerUtils.getClassTypeRepresentation(666));
	}

	@Test
	public void findFieldsMethodsConstructorsTest() throws ContentException {
		Assert.assertEquals("testStatic", CompilerUtils.findField(PseudoCompilerUtilsTest.class, "testStatic", true).getName());
		Assert.assertEquals("testNonPublicStatic", CompilerUtils.findField(PseudoCompilerUtilsTest.class, "testNonPublicStatic", false).getName());

		try{CompilerUtils.findField(null, "testStatic", true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.findField(PseudoCompilerUtilsTest.class, null, true);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findField(PseudoCompilerUtilsTest.class, "", true);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findField(PseudoCompilerUtilsTest.class, "unknown", true);
			Assert.fail("Mandatory exception was not detected (non-existent field)");
		} catch (ContentException exc) {
		}
		try{CompilerUtils.findField(PseudoCompilerUtilsTest.class, "unknown", false);
			Assert.fail("Mandatory exception was not detected (non-existent field)");
		} catch (ContentException exc) {
		}

		Assert.assertEquals("staticMethod", CompilerUtils.findMethod(PseudoCompilerUtilsTest.class, "staticMethod", true, int.class).getName());
		Assert.assertEquals("staticNonPublicMethod", CompilerUtils.findMethod(PseudoCompilerUtilsTest.class, "staticNonPublicMethod", false, int.class).getName());
		
		try{CompilerUtils.findMethod(null, "staticMethod", true, int.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.findMethod(PseudoCompilerUtilsTest.class, null, true, int.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findMethod(PseudoCompilerUtilsTest.class, "", true, int.class);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.findMethod(PseudoCompilerUtilsTest.class, "staticMethod", true);
			Assert.fail("Mandatory exception was not detected (non-existent method)");
		} catch (ContentException exc) {
		}
		try{CompilerUtils.findMethod(PseudoCompilerUtilsTest.class, "staticNonPublicMethod", false);
			Assert.fail("Mandatory exception was not detected (non-existent method)");
		} catch (ContentException exc) {
		}

		Assert.assertNotNull(CompilerUtils.findConstructor(CompilerUtilsTest.class, true));
		Assert.assertNotNull(CompilerUtils.findConstructor(CompilerUtilsTest.class, false));

		try{CompilerUtils.findConstructor(null, true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.findConstructor(PseudoCompilerUtilsTest.class, false, int.class);
			Assert.fail("Mandatory exception was not detected (non-existent constructor)");
		} catch (ContentException exc) {
		}
	}	

	@Test
	public void walkingTest() throws ContentException {
		final Set<Field>			fieldSet = new HashSet<>();
		final Set<Method>			methodSet = new HashSet<>();
		final Set<Constructor<?>>	constructorSet = new HashSet<>();
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class,(cl,f)->{
			if (!f.getName().contains("$")) {	// jacoco - pidorgi!
				fieldSet.add(f);
			}
		});
		Assert.assertEquals(3, fieldSet.size());
		
		try{CompilerUtils.walkFields(null,(cl,f)->fieldSet.add(f));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkFields(PseudoCompilerUtilsTest.class,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		CompilerUtils.walkMethods(PseudoCompilerUtilsTest2.class,(cl,m)->{
			if (cl.equals(PseudoCompilerUtilsTest2.class) && !m.getName().contains("$")) {	// jacoco - pidorgi!
				methodSet.add(m);
			}
		});
		Assert.assertEquals(2, methodSet.size());

		try{CompilerUtils.walkMethods(null,(cl,m)->methodSet.add(m));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkMethods(PseudoCompilerUtilsTest2.class,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		CompilerUtils.walkConstructors(PseudoCompilerUtilsTest2.class,(cl,c)->{
			if (cl.equals(PseudoCompilerUtilsTest2.class)) {
				constructorSet.add(c);
			}
		});
		Assert.assertEquals(1, constructorSet.size());

		try{CompilerUtils.walkConstructors(null,(cl,c)->constructorSet.add(c));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkConstructors(PseudoCompilerUtilsTest2.class,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void extendedWalkingFieldsTest() throws ContentException {
		final Set<Field>			fieldSet = new HashSet<>();
		
		// -- field without patterns on class ---
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), false, true, "testStatic");
		Assert.assertEquals(1, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), false, true, "testStatic", int.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), false, true, "testStatic", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), false, false, "testStatic");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), false, true, "nonExistent");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), false, true, "testStatic", double.class);
		Assert.assertEquals(0, fieldSet.size());

		// -- field without patterns on interface ---
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), false, true, "testStatic");
		Assert.assertEquals(1, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), false, true, "testStatic", int.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), false, true, "testStatic", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), false, false, "testStatic");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), false, true, "nonExistent");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), false, true, "testStatic", double.class);
		Assert.assertEquals(0, fieldSet.size());
		
		
		// -- field with patterns on class ---
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), true, true, "testS.*");
		Assert.assertEquals(1, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), true, true, "testS.*", int.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), true, true, "testS.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), true, false, "testS.*");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), true, true, "nonE.*");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoCompilerUtilsTest.class, (cl,f)->fieldSet.add(f), true, true, "test.*", double.class);
		Assert.assertEquals(0, fieldSet.size());

		// -- field with patterns on interface ---
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "testS.*");
		Assert.assertEquals(1, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "testS.*", int.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "testS.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, fieldSet.size());
		
		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, false, "testS.*");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "nonE.*");
		Assert.assertEquals(0, fieldSet.size());

		fieldSet.clear();
		CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "test.*", double.class);
		Assert.assertEquals(0, fieldSet.size());
		
		try{CompilerUtils.walkFields(null, (cl,f)->fieldSet.add(f), true, true, "test.*", double.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkFields(PseudoInterface3.class, null, true, true, "test.*", double.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, null, double.class);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "", double.class);
			Assert.fail("Mandatory exception was not detected (empty 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "test.*", (Class<?>[])null);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkFields(PseudoInterface3.class, (cl,f)->fieldSet.add(f), true, true, "test.*", double.class, null);
			Assert.fail("Mandatory exception was not detected (nulls inside 6-th argument)");
		} catch (NullPointerException exc) {
		}
	}	

	@Test
	public void extendedWalkingMethodsTest() throws ContentException {
		final Set<Method>			methodSet = new HashSet<>();

		// -- method without patterns on class ---
		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), false, true, void.class, "staticMethod", int.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), false, true, CompilerUtils.AnyType.class, "staticMethod", int.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), false, false, CompilerUtils.AnyType.class, "staticMethod", int.class);
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), false, true, CompilerUtils.AnyType.class, "staticMethod");
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), false, true, CompilerUtils.AnyType.class, "staticMethod", double.class);
		Assert.assertEquals(0, methodSet.size());

		// -- method without patterns on interface ---
		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), false, true, void.class, "staticMethod", int.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), false, true, CompilerUtils.AnyType.class, "staticMethod", int.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), false, false, CompilerUtils.AnyType.class, "staticMethod", int.class);
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), false, true, CompilerUtils.AnyType.class, "staticMethod");
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), false, true, CompilerUtils.AnyType.class, "staticMethod", double.class);
		Assert.assertEquals(0, methodSet.size());

		// -- method with patterns on class ---
		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), true, true, void.class, "staticM.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", CompilerUtils.AnyTypeList.class);
		Assert.assertEquals(1, methodSet.size());
		
		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), true, false, CompilerUtils.AnyType.class, "staticM.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*");
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoCompilerUtilsTest.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", double.class);
		Assert.assertEquals(0, methodSet.size());

		// -- method with patterns on interface ---
		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, void.class, "staticM.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(1, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", CompilerUtils.AnyTypeList.class);
		Assert.assertEquals(1, methodSet.size());
		
		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, false, CompilerUtils.AnyType.class, "staticM.*", CompilerUtils.AnyType.class);
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*");
		Assert.assertEquals(0, methodSet.size());

		methodSet.clear();
		CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", double.class);
		Assert.assertEquals(0, methodSet.size());

		try{CompilerUtils.walkMethods(null, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", double.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkMethods(PseudoInterface3.class, null, true, true, CompilerUtils.AnyType.class, "staticM.*", double.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, null, "staticM.*", double.class);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, null, double.class);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "", double.class);
			Assert.fail("Mandatory exception was not detected (empty 6-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", (Class<?>[])null);
			Assert.fail("Mandatory exception was not detected (null 7-th argument)");
		} catch (NullPointerException exc) {
		}
		try{CompilerUtils.walkMethods(PseudoInterface3.class, (cl,m)->methodSet.add(m), true, true, CompilerUtils.AnyType.class, "staticM.*", double.class, null);
			Assert.fail("Mandatory exception was not detected (nulls inside 7-th argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void fieldManagementTest() throws ContentException {
		final Field		staticF = CompilerUtils.findField(CompilerUtilsTest.class, "testStatic", true);
		final Field		nonStaticF = CompilerUtils.findField(CompilerUtilsTest.class, "testInstance", true);
		
		Assert.assertEquals(CompilerUtilsTest.class.getCanonicalName()+"."+staticF.getName(),CompilerUtils.buildFieldPath(staticF));
		Assert.assertEquals(CompilerUtilsTest.class.getCanonicalName()+"."+nonStaticF.getName(),CompilerUtils.buildFieldPath(nonStaticF));

		try{CompilerUtils.buildFieldPath(null);
			Assert.fail("Mandator exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals("I",CompilerUtils.buildFieldSignature(staticF));
		Assert.assertEquals("Ljava/lang/String;",CompilerUtils.buildFieldSignature(nonStaticF));

		try{CompilerUtils.buildFieldSignature(null);
			Assert.fail("Mandator exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(" getstatic "+CompilerUtilsTest.class.getCanonicalName()+"."+staticF.getName()+"\n",CompilerUtils.buildGetter(staticF));
		Assert.assertEquals(" getfield "+CompilerUtilsTest.class.getCanonicalName()+"."+nonStaticF.getName()+"\n",CompilerUtils.buildGetter(nonStaticF));

		try{CompilerUtils.buildGetter(null);
			Assert.fail("Mandator exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(" putstatic "+CompilerUtilsTest.class.getCanonicalName()+"."+staticF.getName()+"\n",CompilerUtils.buildSetter(staticF));
		Assert.assertEquals(" putfield "+CompilerUtilsTest.class.getCanonicalName()+"."+nonStaticF.getName()+"\n",CompilerUtils.buildSetter(nonStaticF));

		try{CompilerUtils.buildSetter(null);
			Assert.fail("Mandator exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void methodManagementTest() throws ContentException {
		final Method	staticM = CompilerUtils.findMethod(CompilerUtilsTest.class, "staticMethod", false, int.class);
		final Method	nonStaticM = CompilerUtils.findMethod(CompilerUtilsTest.class, "staticNonPublicMethod", false, int.class);
	
		Assert.assertEquals(CompilerUtilsTest.class.getCanonicalName()+"."+staticM.getName(), CompilerUtils.buildMethodPath(staticM));
		Assert.assertEquals(CompilerUtilsTest.class.getCanonicalName()+"."+nonStaticM.getName(), CompilerUtils.buildMethodPath(nonStaticM));
		
		try{CompilerUtils.buildMethodPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals("(I)V", CompilerUtils.buildMethodSignature(staticM));
		Assert.assertEquals("(I)Ljava/lang/String;", CompilerUtils.buildMethodSignature(nonStaticM));
		
		try{CompilerUtils.buildMethodPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(staticM.getName()+" .method void public static\narg0 .parameter int final\n", CompilerUtils.buildMethodHeader(staticM));
		Assert.assertEquals(nonStaticM.getName()+" .method java.lang.String\narg0 .parameter int final\n", CompilerUtils.buildMethodHeader(nonStaticM));

		try{CompilerUtils.buildMethodHeader(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(" invokestatic "+CompilerUtilsTest.class.getCanonicalName()+"."+staticM.getName()+"(I)V\n", CompilerUtils.buildMethodCall(staticM));
		Assert.assertEquals(" invokevirtual "+CompilerUtilsTest.class.getCanonicalName()+"."+nonStaticM.getName()+"(I)Ljava/lang/String;\n", CompilerUtils.buildMethodCall(nonStaticM));

		try{CompilerUtils.buildMethodCall(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}	

	@Test
	public void constructorManagementTest() throws ContentException {
		final Constructor<?>	constr  = CompilerUtils.findConstructor(CompilerUtilsTest.class, false);
		
		Assert.assertEquals(CompilerUtilsTest.class.getCanonicalName()+"."+CompilerUtilsTest.class.getSimpleName(),CompilerUtils.buildConstructorPath(constr));

		try{CompilerUtils.buildConstructorPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals("()V",CompilerUtils.buildConstructorSignature(constr));

		try{CompilerUtils.buildConstructorSignature(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(" invokespecial "+CompilerUtilsTest.class.getCanonicalName()+"."+CompilerUtilsTest.class.getSimpleName()+"()V\n",CompilerUtils.buildConstructorCall(constr));

		try{CompilerUtils.buildConstructorCall(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}

class PseudoCompilerUtilsTest extends CompilerUtilsTest {
}

class PseudoCompilerUtilsTest2 {
	public static void staticMethod(int x) {}
	static void staticNonPublicMethod(int x) {}
}

interface PseudoInterface1 {
	int testStatic = 10;
	void staticMethod(int x);
}

interface PseudoInterface2 {
	int testAnotherStatic = 10;
}

interface PseudoInterface3 extends PseudoInterface1, PseudoInterface2 {
}
