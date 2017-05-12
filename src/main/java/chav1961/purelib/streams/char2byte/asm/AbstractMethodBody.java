package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.io.OutputStream;

import chav1961.purelib.basic.exceptions.ContentException;

abstract class AbstractMethodBody {
	abstract long getUniqueLabelId();
	abstract short getPC();
	abstract void putCommand(int stackDelta, byte... data);
	abstract void alignPC();
	abstract void putLabel(long labelId);
	abstract void registerBrunch(long labelId, boolean shortBranch);
	abstract void registerBrunch(int address, int placement, long labelId, boolean shortBranch);
	abstract short getStackSize();
	abstract int getCodeSize();
	abstract int dump(final OutputStream os) throws IOException, ContentException;
}
