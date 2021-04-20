package chav1961.purelib.streams.char2byte.asm.macro;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.testing.TestingUtils;

@Category(OrdinalTestCategory.class)
public class MacroCompilerTest {
	static boolean	alreadyTested = false;
	
	@AfterClass
	public static void markTested() {
		alreadyTested = true;
	}
	
	@Test
	public void basicTest() throws IOException, SyntaxException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
		
			MacroCompiler.compile("MacroTestClass", new MacroCommand("MacroTestClass".toCharArray()),gca,stringRepo);
			checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass",gca);
		}
	}

	@Test
	public void substitutionTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass1",pm.test("MacroTestClass1 .macro parm:int\n123456789\n .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass1",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(null,target);
				Assert.assertEquals(new String(target.extract()),"123456789\n");
			}
		}
	}

	@Test
	public void variableAccessTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass2",pm.test("MacroTestClass2 .macro parm:int\nvar .local int=100\n&var\n .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass2",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"100\n");
			}		
			
			final GrowableCharArray<?>	gcaF = new GrowableCharArray<>(true), targetF = new GrowableCharArray<>(true), stringRepoF = new GrowableCharArray<>(false);
			try(final PseudoMacros		pmF = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass3",pmF.test("MacroTestClass3 .macro parm:int\nvar .local int\n&var\n .mend"),gcaF,stringRepoF);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			clF = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass3",gcaF);
				final Constructor<MacroExecutor>	cF = clF.getConstructor(char[].class);
				final MacroExecutor					instF = cF.newInstance(stringRepo.extract());
				
				try{instF.exec(pmF.getRoot().getDeclarations(),targetF);
					Assert.fail("Mandatory exception was not detected (non-initialzed var)");
				} catch (CalculationException exc) {
					//exc.printStackTrace();
				}
			}
		}
	}

	@Test
	public void longExpressionTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass4",pm.test("MacroTestClass4 .macro parm:int\nvar .local int=100\ntarget .local int\ntarget .set (-var1/3+2)*3%5-1 \n&target\n .mend"),gca,stringRepo);
		
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass4",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"-4\n");
			}
			
			try(final PseudoMacros		pm1 = new PseudoMacros()) {
			
				gca.length(0);
				target.length(0);
				stringRepo.length(0);
				MacroCompiler.compile("MacroTestClass4a",pm1.test("MacroTestClass4a .macro parm:int\nvar .local int\nvar .set uniqueL()\n&var\nvar .set uniqueL()\n&var\n .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl1 = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass4a",gca);
				final Constructor<MacroExecutor>	c1 = cl1.getConstructor(char[].class);
				final MacroExecutor					inst1 = c1.newInstance(stringRepo.extract());
				
				inst1.exec(pm1.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"0\n1\n");
			}
		}
	}
	
	@Test
	public void doubleExpressionTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass5",pm.test("MacroTestClass5 .macro parm:real\nvar .local real=100\ntarget .local real\ntarget .set (-var1/3+2)*3%5-1 \n&target\n .mend"),gca,stringRepo);
		
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass5",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"-5.0\n");
			}
		}
	}

	@Test
	public void concatExpressionTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass6",pm.test("MacroTestClass6 .macro parm:string\nvar .local string=\"test\"\ntarget .local string\ntarget .set var#\"and\"#var \n&target\n .mend"),gca,stringRepo);
		
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass6",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"testandtest\n");
			}
		}
	}

	@Test
	public void ternaryAndCompareTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass7",pm.test("MacroTestClass7 .macro parm:string\nvar .local string=\"test\"\ntarget .local string\ncond .local bool\n"
									+ "target .set (var == \"test\") ? \"true\" : \"false\" \n&target\ntarget .set (var != \"test\") ? \"true\" : \"false\" \n&target\n"
									+ "target .set (var > \"test\") ? \"true\" : \"false\" \n&target\ntarget .set (var <= \"test\") ? \"true\" : \"false\" \n&target\n"
									+ "target .set (var >= \"test\") ? \"true\" : \"false\" \n&target\ntarget .set (var < \"test\") ? \"true\" : \"false\" \n&target\n"
									+ "cond .set var >= \"test\"\n&cond\n"
									+ " .mend"),gca,stringRepo);
		
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			clT = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass7",gca);
				final Constructor<MacroExecutor>	cT = clT.getConstructor(char[].class);
				final MacroExecutor					instT = cT.newInstance(stringRepo.extract());
				
				instT.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"true\nfalse\nfalse\ntrue\ntrue\nfalse\ntrue\n");
			}
		}
	}

	@Test
	public void booleansTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass8",pm.test("MacroTestClass8 .macro parm:string\nvarT .local bool=true\nvarF .local bool=false\ntarget .local bool\n"
						+ "target .set !varT\n&target\ntarget .set varT\n&target\n"
						+ "target .set varT&&varF\n&target\ntarget .set varT&&!varF\n&target\n"
						+ "target .set varT||varF\n&target\ntarget .set !varT||varF\n&target\n .mend"),gca,stringRepo);
		
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			clT = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass8",gca);
				final Constructor<MacroExecutor>	cT = clT.getConstructor(char[].class);
				final MacroExecutor					instT = cT.newInstance(stringRepo.extract());
				
				instT.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"false\ntrue\nfalse\ntrue\ntrue\nfalse\n");
			}
		}
	}
	
	@Test
	public void merrorTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);;
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass10",pm.test("MacroTestClass10 .macro parm:int\n .error \"123\"\n .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass10",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				try{inst.exec(pm.getRoot().getDeclarations(),target);
					Assert.fail("Mandatory exception was not detected (merror exception)");
				} catch (CalculationException exc) {
				}
			}
		}
	}

	@Test
	public void exitTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);;
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass11",pm.test("MacroTestClass11 .macro parm:int\n .exit\n .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass11",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
			} 
		}
	}

	@Test
	public void ifTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);;
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass12",pm.test("MacroTestClass12 .macro parm:int\nvar .local int\n"
						+ "var .set 10\n"
						+ "	.if (var == 10)\n"
						+ "&var=10\n"
						+ "	.elseif (var == 20)\n"
						+ "&var=20\n"
						+ "	.else\n"
						+ "&var=30\n"
						+ "	.endif\n"
						+ "var .set 20\n"
						+ "	.if (var == 10)\n"
						+ "&var=10\n"
						+ "	.elseif (var == 20)\n"
						+ "&var=20\n"
						+ "	.else\n"
						+ "&var=30\n"
						+ "	.endif\n"
						+ "var .set 30\n"
						+ "	.if (var == 10)\n"
						+ "&var=10\n"
						+ "	.elseif (var == 20)\n"
						+ "&var=20\n"
						+ "	.else\n"
						+ "&var=30\n"
						+ "	.endif\n"
						+ " .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass12",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"10=10\n20=20\n30=30\n");
			}
		}
	}

	@Test
	public void choiseTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);;
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass13",pm.test("MacroTestClass13 .macro parm:int\nvarI .local int=10\nvarR .local real=10\nvarS .local string=\"10\"\nvarB .local bool=false\n"
						+ "varI .set 10\n"
						+ "	.choise varI\n"
						+"	.of 10\n"
						+ "&varI=10\n"
						+"	.of 20\n"
						+ "&varI=20\n"
						+"	.otherwise\n"
						+ "&varI=30\n"
						+ "	.endchoise\n"
						+ "varI .set 20\n"
						+ "	.choise varI\n"
						+"	.of 10\n"
						+ "&varI=10\n"
						+"	.of 20\n"
						+ "&varI=20\n"
						+"	.otherwise\n"
						+ "&varI=30\n"
						+ "	.endchoise\n"
						+ "varI ."
						+ "set 30\n"
						+ "	.choise varI\n"
						+"	.of 10\n"
						+ "&varI=10\n"
						+"	.of 20\n"
						+ "&varI=20\n"
						+"	.otherwise\n"
						+ "&varI=30\n"
						+ "	.endchoise\n"
						
						+ "varR .set 10.0\n"
						+ "	.choise varR\n"
						+"	.of 10.0\n"
						+ "&varR=10.0\n"
						+"	.of 20.0\n"
						+ "&varR=20.0\n"
						+"	.otherwise\n"
						+ "&varR=30.0\n"
						+ "	.endchoise\n"
						+ "varR .set 20.0\n"
						+ "	.choise varR\n"
						+"	.of 10.0\n"
						+ "&varR=10.0\n"
						+"	.of 20.0\n"
						+ "&varR=20.0\n"
						+"	.otherwise\n"
						+ "&varI=30.0\n"
						+ "	.endchoise\n"
						+ "varR .set 30.0\n"
						+ "	.choise varR\n"
						+"	.of 10.0\n"
						+ "&varR=10.0\n"
						+"	.of 20.0\n"
						+ "&varR=20.0\n"
						+"	.otherwise\n"
						+ "&varR=30.0\n"
						+ "	.endchoise\n"
		
						+ "varS .set \"10\"\n"
						+ "	.choise varS\n"
						+"	.of \"10\"\n"
						+ "&varS=10\n"
						+"	.of \"20\"\n"
						+ "&varS=20\n"
						+"	.otherwise\n"
						+ "&varS=30\n"
						+ "	.endchoise\n"
						+ "varS .set \"20\"\n"
						+ "	.choise varS\n"
						+"	.of \"10\"\n"
						+ "&varS=10\n"
						+"	.of \"20\"\n"
						+ "&varS=20\n"
						+"	.otherwise\n"
						+ "&varS=30\n"
						+ "	.endchoise\n"
						+ "varS .set \"30\"\n"
						+ "	.choise varS\n"
						+"	.of \"10\"\n"
						+ "&varS=10\n"
						+"	.of \"20\"\n"
						+ "&varS=20\n"
						+"	.otherwise\n"
						+ "&varS=30\n"
						+ "	.endchoise\n"
		
						+ "varB .set false\n"
						+ "	.choise varB\n"
						+"	.of false\n"
						+ "&varB=false\n"
						+"	.otherwise\n"
						+ "&varB=true\n"
						+ "	.endchoise\n"
						+ "varB .set true\n"
						+ "	.choise varB\n"
						+"	.of false\n"
						+ "&varB=false\n"
						+"	.otherwise\n"
						+ "&varB=true\n"
						+ "	.endchoise\n"
						
						+ " .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass13",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"10=10\n20=20\n30=30\n10.0=10.0\n20.0=20.0\n30.0=30.0\n10=10\n20=20\n30=30\nfalse=false\ntrue=true\n");
			}
		}
	}

	@Test
	public void whileTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);;
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass14",pm.test("MacroTestClass14 .macro parm:int\nvar .local int=3\n"
						+ "	.while var > 0\n"
						+ "&var\n"
						+ "var .set var-1\n"
						+ "	.endwhile\n"
						+ " .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass14",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"3\n2\n1\n");
			}
		}
	}

	@Test
	public void forTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);;
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass15",pm.test("MacroTestClass15 .macro parm:int\nvar .local int\n"
						+ "	.for var = 1 to 3 step 1\n"
						+ "&var\n"
						+ "	.endfor\n"
						+ " .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass15",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"1\n2\n3\n");
			}
		}
	}

	@Test
	public void breakAndContinueTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass16",pm.test("MacroTestClass16 .macro parm:int\n"
						+"var 	.local int=10\n"
						+ "x:	.while var > 0\n"
						+ "var		.set var-1\n"
						+ "			.if var%2 == 0\n"
						+ "				.continue x\n"			
						+ "			.endif\n"
						+ "&var\n"
						+ "		.endwhile\n"
						+ "var		.set 10\n"
						+ "x:	.while var > 0\n"
						+ "var		.set var-1\n"
						+ "			.if var < 3 == 0\n"
						+ "				.break x\n"			
						+ "			.endif\n"
						+ "&var\n"
						+ "		.endwhile\n"
						+ " .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass16",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"9\n7\n5\n3\n1\n9\n8\n7\n6\n5\n4\n3\n");
			}
		}
	}

	@Test
	public void forAllTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass17",pm.test("MacroTestClass17 .macro parm:int\nvar .local str\n"
						+ "	.forall var in \"1,2,3\" splitted by \",\"\n"
						+ "&var\n"
						+ "	.endforall\n"
						+ " .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass17",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"1\n2\n3\n");
			}
		}
	}
	

	@Test
	public void arraysTest() throws IOException, SyntaxException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CalculationException {
		if (!alreadyTested) {	// Protection against repeatable tests with the same ClassLaoder
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(true), target = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
			
			try(final PseudoMacros		pm = new PseudoMacros()) {
			
				MacroCompiler.compile("MacroTestClass18",pm.test("MacroTestClass18 .macro parm:int\nvar .local str[]={\"val1\",\"val2\"}\nind .local int\n"
						+ "	.for ind = 0 to len(var)-1 step 1\n"
						+ "&var[ind]\n"
						+ "	.endfor\n"
						+ " .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass18",gca);
				final Constructor<MacroExecutor>	c = cl.getConstructor(char[].class);
				final MacroExecutor					inst = c.newInstance(stringRepo.extract());
				
				inst.exec(pm.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"val1\nval2\n");
			}

			try(final PseudoMacros		pm1 = new PseudoMacros()) {
				
				gca.length(0);
				target.length(0);
				stringRepo.length(0);
				MacroCompiler.compile("MacroTestClass18a",pm1.test("MacroTestClass18a .macro p1:str[]={\"val1\",\"val2\"}"
									+"\nvar .local str[5]"
									+"\nvar .setindex 0,\"value1\""
									+"\n&p1[0] &var[0]"
									+"\n .mend"),gca,stringRepo);
				
				@SuppressWarnings("unchecked")
				final Class<MacroExecutor>			cl1 = (Class<MacroExecutor>) checkClass("chav1961.purelib.streams.char2byte.asm.MacroTestClass18a",gca);
				final Constructor<MacroExecutor>	c1 = cl1.getConstructor(char[].class);
				final MacroExecutor					inst1 = c1.newInstance(stringRepo.extract());
				
				inst1.exec(pm1.getRoot().getDeclarations(),target);
				Assert.assertEquals(new String(target.extract()),"val1 value1\n");
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private Class<? extends MacroExecutor> checkClass(final String className, final GrowableCharArray<?> gca) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {

			try(final Reader			rdr = gca.getReader()) {
				Utils.copyStream(rdr,new OutputStreamWriter(TestingUtils.err()));
			}
			
			try(final Writer			asm = new AsmWriter(baos);
				final Reader			rdr = gca.getReader()) {
				Utils.copyStream(rdr,asm);
			}
			
			return (Class<? extends MacroExecutor>) loadClass(MacroExecutor.class.getClassLoader(),className,baos);
		}
	}
	
	public static Class<?> loadClass(final ClassLoader parent, final String className, final ByteArrayOutputStream baos) throws IOException {
		try(final SimpleURLClassLoader	clw = new SimpleURLClassLoader(new URL[0],parent)) {
			final byte[]				buffer = baos.toByteArray();
			final PrintStream			ps = TestingUtils.err();
	
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
			
			final Class<?>	result = clw.createClass(className,baos.toByteArray()); 
			
			Assert.assertNotNull(result);
			return result;
		}
	}
}


class PseudoMacros extends Macros {
	public MacroCommand test(final String macros) throws IOException, SyntaxException {
		int		lineNo = 1;
		for (String item : macros.split("\\n")) {
			final char[]	data = (item+'\n').toCharArray();
			
			processLine(0,lineNo++,data,0,data.length);
		}
		return getRoot();
	}
}