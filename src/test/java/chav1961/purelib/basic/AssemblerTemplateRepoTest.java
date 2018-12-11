package chav1961.purelib.basic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.AssemblerTemplateRepo.NameKeeper;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;

public class AssemblerTemplateRepoTest {
	private static final byte[]		content = "{PART1}\nline1\n${subst1}\nline3\n${subst2}\nline5\n{PART2}\nLINE1\n${subst1}\nLINE3\n${subst2}\nLINE5".getBytes(); 
	
	@Test
	public void basicTest() throws IOException, SyntaxException {
		final GrowableCharArray	gca = new GrowableCharArray(true); 
		
		try(final InputStream	is = new ByteArrayInputStream(content)) {
			final AssemblerTemplateRepo	atr = new AssemblerTemplateRepo(is);
			
			atr.append(gca,"PART1",atr.getNameKeeper().put("subst1",123).put("subst2","text"));
			try(final NameKeeper	child = atr.getNameKeeper().push()) {
				atr.append(gca,"PART2",child);
				atr.append(gca,"PART2",child.put("subst1","new string"));
			}
			
			try{atr.append(null,"PART1".toCharArray());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{atr.append(gca,(char[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{atr.append(gca,(String)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{atr.append(gca,"UNKNOWN".toCharArray());
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{atr.append(gca,"PART1".toCharArray(),null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
		
		Assert.assertEquals(new String(gca.extract()),"line1\n123\nline3\ntext\nline5\nLINE1\n123\nLINE3\ntext\nLINE5\nLINE1\nnew string\nLINE3\ntext\nLINE5\n");
	}
}
