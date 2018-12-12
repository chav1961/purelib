package chav1961.purelib.basic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.FastPattern.CodeBuilder;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;

public class FastPatternTest {
	@Test
	public void simpleTest() {
		final FastPattern	fp = FastPattern.build("a",false);
		
		Assert.assertEquals(fp.match("a").count(),0L);
		Assert.assertNull(fp.match("A"));
	}
	
	@Test
	public void templateRepoTest() throws IOException, SyntaxException {
		try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/purelib/basic/templaterepocontent.txt")) {
			final AssemblerTemplateRepo		repo = new AssemblerTemplateRepo(is);
			final GrowableCharArray	gca = new GrowableCharArray(false);
			
			repo.append(gca,"simple".toCharArray(),(key,from,to)->{return "value1".toCharArray();});
			repo.append(gca,"complex".toCharArray(),(key,from,to)->{return "value2".toCharArray();});
			repo.append(gca,"advanced %1$s","line");
			
			Assert.assertEquals(new String(gca.extract()).replace("\r",""),"beforevalue1after\nbeforevalue2after\n\nadvanced line");
			
			try{repo.append(null,"simple".toCharArray(),(key,from,to)->{return "value1".toCharArray();});
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{repo.append(gca,(char[])null,(key,from,to)->{return "value1".toCharArray();});
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{repo.append(gca,(String)null,(key,from,to)->{return "value1".toCharArray();});
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{repo.append(gca,"unknown".toCharArray(),(key,from,to)->{return "value1".toCharArray();});
				Assert.fail("Mandatory exception was not detected (unknown 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{repo.append(gca,"simple".toCharArray(),null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
		
		try{new AssemblerTemplateRepo(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void codeBuilderTest() throws IOException, SyntaxException, InstantiationException, IllegalAccessException {
		try(final InputStream			is = new FileInputStream("./src/test/resources/chav1961/purelib/basic/pseudotest.txt")) {
			final AssemblerTemplateRepo	repo = new AssemblerTemplateRepo(is);
			final GrowableCharArray		gca = new GrowableCharArray(false); 
			
			try(final CodeBuilder		cb = new CodeBuilder(repo,"chav1961.purelib.basic.Test",gca)) {
				cb.getRepo().append(cb.getArray()," .stack 2\n iload_1\n iload_2\n iadd\n ireturn\n");
			}
			try(final Reader			rdr = gca.getReader()) {
				final Class<PseudoTest>	clazz = (Class<PseudoTest>) new ClassLoaderWrapper().createClass("chav1961.purelib.basic.Test",rdr);
				
				Assert.assertEquals(clazz.newInstance().add(3,5),8);
			} catch (IllegalArgumentException exc) {
				if (!(exc.getCause() instanceof LinkageError)) {
					throw exc;
				}
			}
		}
	}

	@Test
	public void singleCharComparisonTest() {
		final FastPattern	fp = FastPattern.build("a",false);
		
		Assert.assertEquals(fp.match("a").count(),0L);
		Assert.assertNull(fp.match("A"));

		final FastPattern	fpc = FastPattern.build("a",true);
		
		Assert.assertNull(fpc.match("A"));
		Assert.assertNotNull(fpc.match("a"));
		Assert.assertEquals(fpc.match("a").count(),0L);
	}

	@Test
	public void listCharComparisonTest() {
		final FastPattern	fp = FastPattern.build("[abcdent-z]",false);
		
		Assert.assertEquals(fp.match("a").count(),0L);
		Assert.assertEquals(fp.match("v").count(),0L);
		Assert.assertNull(fp.match("A"));

		final FastPattern	fpc = FastPattern.build("[abcdent-z]",true);
		
		Assert.assertNotNull(fpc.match("a"));
		Assert.assertNotNull(fpc.match("c"));
		Assert.assertNotNull(fpc.match("e"));
		Assert.assertNull(fpc.match("g"));
		Assert.assertNotNull(fpc.match("n"));
		Assert.assertNull(fpc.match("o"));
		Assert.assertNotNull(fpc.match("t"));
		Assert.assertNotNull(fpc.match("v"));
		Assert.assertNotNull(fpc.match("z"));
		Assert.assertNull(fpc.match("A"));
		Assert.assertEquals(fpc.match("a").count(),0L);

		final FastPattern	fp2 = FastPattern.build("[^abcdent-z]",false);

		Assert.assertNull(fp2.match("a"));
		Assert.assertNull(fp2.match("v"));
		Assert.assertEquals(fp2.match("A").count(),0);
		
		final FastPattern	fpc2 = FastPattern.build("[^abcdent-z]",false);
		
		Assert.assertNull(fpc2.match("a"));
		Assert.assertNull(fpc2.match("c"));
		Assert.assertNull(fpc2.match("e"));
		Assert.assertNotNull(fpc2.match("g"));
		Assert.assertNull(fpc2.match("n"));
		Assert.assertNotNull(fpc2.match("o"));
		Assert.assertNull(fpc2.match("t"));
		Assert.assertNull(fpc2.match("v"));
		Assert.assertNull(fpc2.match("z"));
		Assert.assertNotNull(fpc2.match("A"));
		Assert.assertEquals(fpc2.match("A").count(),0L);
	}
}