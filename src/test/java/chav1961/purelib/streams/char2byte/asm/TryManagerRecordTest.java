package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.StackSnapshot;
import chav1961.purelib.testing.OrdinalTestCategory;


@Category(OrdinalTestCategory.class)
public class TryManagerRecordTest {
	@Test
	public void lifeCycleTest() throws ContentException {
		final PseudoMethodBody4TryManager	mgr = new PseudoMethodBody4TryManager();
		final TryManagerRecord				tmr = new TryManagerRecord(mgr);
		
		tmr.processCatch((short)1,(short)2,(short)3);
		tmr.processFinally();
		Assert.assertArrayEquals(tmr.processEnd(),new short[][]{new short[]{(short)123, (short)126, (short)1, (short)126}, new short[]{(short)123, (short)126, (short)2, (short)126}, new short[]{(short)123, (short)126, (short)3, (short)126}, new short[]{(short)123, (short)126, (short)0, (short)129}});
		
		try{new TryManagerRecord(null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new TryManagerRecord(mgr).processCatch();
			Assert.fail("Mandatory exception was not detected (empty argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new TryManagerRecord(mgr).processCatch((short)10,(short)10);
			Assert.fail("Mandatory exception was not detected (duplicates in one catch)");
		} catch (ContentException exc) {
		}
		try{final TryManagerRecord	temp = new TryManagerRecord(mgr);
			temp.processCatch((short)10);
			temp.processCatch((short)10);
			Assert.fail("Mandatory exception was not detected (duplicates in different catch)");
		} catch (ContentException exc) {
		}
		try{final TryManagerRecord	temp = new TryManagerRecord(mgr);
			temp.processFinally();
			temp.processFinally();
			Assert.fail("Mandatory exception was not detected (duplicate finally)");
		} catch (ContentException exc) {
		}
	}
}

class PseudoMethodBody4TryManager extends AbstractMethodBody {
	public short	pc = 123;
	public int		bytesCount = 0;
	public long		label;
	
	@Override long getUniqueLabelId() {return 2;}
	@Override short getPC() {return pc;}
	@Override void putCommand(int stackDelta, byte... data) {bytesCount += data.length; pc += data.length;}
	@Override void alignPC() {}
	@Override void putLabel(long labelId,final StackSnapshot snapshot) {label = labelId;}
	@Override void registerBrunch(long labelId, boolean shortBranch,final StackSnapshot snapshot) {}
	@Override void registerBrunch(int address, int placement, long labelId, boolean shortBranch,final StackSnapshot snapshot) {}
	@Override short getStackSize() {return 0;}
	@Override int getCodeSize() {return 0;}
	@Override int dump(InOutGrowableByteArray os) throws IOException {return 0;}
	@Override void markLabelRequired(boolean required) {}
	@Override StackAndVarRepo getStackAndVarRepo() {return new StackAndVarRepo((a,b,c,d)->{});}
	@Override StackAndVarRepoNew getStackAndVarRepoNew() {return new StackAndVarRepoNew();}
	@Override boolean isLabelExists(long labelId) {return false;}
}
