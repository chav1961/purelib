package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.StackSnapshot;

abstract class AbstractMethodBody {
	abstract long getUniqueLabelId();
	abstract short getPC();
	abstract void putCommand(int stackDelta, byte... data) throws ContentException;
	abstract void alignPC() throws ContentException;
	abstract boolean isLabelExists(long labelId);
	abstract void putLabel(long labelId, StackSnapshot snapshot) throws ContentException;
	abstract void markLabelRequired(boolean required);
	abstract void registerBrunch(long labelId, boolean shortBranch, StackSnapshot snapshot) throws ContentException;
	abstract void registerBrunch(int address, int placement, long labelId, boolean shortBranch, StackSnapshot snapshot) throws ContentException;
	abstract short getStackSize();
	abstract StackAndVarRepo getStackAndVarRepo();
	abstract int getCodeSize();
	abstract int dump(final InOutGrowableByteArray os) throws IOException, ContentException;
}
