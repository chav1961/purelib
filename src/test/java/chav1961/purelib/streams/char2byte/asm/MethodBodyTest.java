package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.StackSnapshot;

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

//	@Test
	public void basicTest() throws IOException, ContentException {
		final SyntaxTreeInterface<Object>	aot = new AndOrTree<Object>(1,16);
		final MethodBody	mb = new MethodBody(0,0,aot,false,new StackAndVarRepo((a,b,c,d)->{}));
		final StackSnapshot	ss = mb.getStackAndVarRepo().makeStackSnapshot();
		
		aot.placeName("label1".toCharArray(), 0, 6, 1, null);
		aot.placeName("label2".toCharArray(), 0, 6, 2, null);

		mb.putLabel(1,ss);
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(1,false,ss);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		mb.putLabel(2,ss);
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,false,ss);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,true,ss);
			mb.putCommand((byte)0,(byte)0);
		}
		
		final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(true); 

		mb.dump(iogba);
		Assert.assertArrayEquals(iogba.toArray(),result);
	}

	@Test
	public void unresolvedLabelsTest() throws IOException, ContentException {
		final SyntaxTreeInterface<Object>	aot = new AndOrTree<Object>(1,16);
		final MethodBody	mb = new MethodBody(3,4,aot,false,new StackAndVarRepo((a,b,c,d)->{}));
		final StackSnapshot	ss = mb.getStackAndVarRepo().makeStackSnapshot();
		
		aot.placeName("label1".toCharArray(), 0, 6, 1,null);
		aot.placeName("label2".toCharArray(), 0, 6, 2,null);
		aot.placeName("class".toCharArray(), 0, 5, 3,null);
		aot.placeName("method".toCharArray(), 0, 6, 4,null);

		for (int index = 0; index < 16; index++){
			mb.registerBrunch(1,false,ss);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		mb.putLabel(2,ss);
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,false,ss);
			mb.putCommand((byte)0,(byte)0,(byte)0,(byte)0);
		}
		for (int index = 0; index < 16; index++){
			mb.registerBrunch(2,true,ss);
			mb.putCommand((byte)0,(byte)0);
		}

		final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(true); 
		
		try{mb.dump(iogba);
			Assert.fail("Mandatory exception was not detected (unresolved jumps)");
		} catch (ContentException exc) {
		}
	}
}
