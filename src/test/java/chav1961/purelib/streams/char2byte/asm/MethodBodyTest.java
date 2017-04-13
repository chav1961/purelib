package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class MethodBodyTest {
	private byte[]	result;
	
	@Before
	public void prepare() {
		result = new byte[160]; 
		
		for (int index = 0; index < 16; index++){
			result[4 * index] = 0;		result[4 * index + 1] = 0;
			result[4 * index + 2] = 0;	result[4 * index + 3] = 0;
		}

		for (int index = 0; index < 16; index++){
			result[64 + 4 * index] = 0;		result[64 + 4 * index + 1] = 0;
			result[64 + 4 * index + 2] = 0;	result[64 + 4 * index + 3] = 64;
		}

		for (int index = 0; index < 16; index++){
			result[128 + 2 * index] = 0;
			result[128 + 2 * index + 1] = 64;
		}
	}

	//@Test
	public void basicTest() throws IOException {
		final SyntaxTreeInterface	aot = new AndOrTree(1);
		final MethodBody	mb = new MethodBody(aot);
		
		aot.placeName("label1".toCharArray(), 0, 6, 1, null);
		aot.placeName("label2".toCharArray(), 0, 6, 2, null);

		mb.putLabel(1);
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(1,false);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		mb.putLabel(2);
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,false);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,true);
			mb.putCommand((byte)0,(byte)0);
		}
		
		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			mb.dump(baos);	baos.flush();
			Assert.assertArrayEquals(baos.toByteArray(),result);
		}
	}

	@Test
	public void unresolvedLabelsTest() throws IOException {
		final SyntaxTreeInterface	aot = new AndOrTree(1);
		final MethodBody	mb = new MethodBody(aot);
		
		aot.placeName("label1".toCharArray(), 0, 6, 1,null);
		aot.placeName("label2".toCharArray(), 0, 6, 2,null);

		for (int index = 0; index < 16; index++){
			mb.registerBrunch(1,false);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		mb.putLabel(2);
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,false);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,true);
			mb.putCommand((byte)0,(byte)0);
		}
		
		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			mb.dump(baos);	baos.flush();
			Assert.fail("Mandatory exception was not detected (unresolved jumps)");
		} catch (AsmSyntaxException exc) {
		}
	}
}
