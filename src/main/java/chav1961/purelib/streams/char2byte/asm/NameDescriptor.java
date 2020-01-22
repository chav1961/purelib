package chav1961.purelib.streams.char2byte.asm;

import java.util.Arrays;

class NameDescriptor {
	int				nameType;
	final short[]	cpIds = new short[ClassConstantsRepo.CPIDS_SIZE];	// locations for the different constant types in the constant pool
	
	{
		Arrays.fill(cpIds,Short.MAX_VALUE);
	}
	
	NameDescriptor(final int nameType) {
		this.nameType = nameType;
	}
}
