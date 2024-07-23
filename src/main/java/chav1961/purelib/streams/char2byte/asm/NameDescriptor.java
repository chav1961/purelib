package chav1961.purelib.streams.char2byte.asm;

import java.util.Arrays;

import chav1961.purelib.cdb.JavaByteCodeConstants;

class NameDescriptor {
	int		nameType;
	long	nameTypeId;
	final short[]	cpIds = new short[JavaByteCodeConstants.CONSTANT_MaxPoolTypes];	// locations for the different constant types in the constant pool
	
	{
		Arrays.fill(cpIds, Short.MIN_VALUE);
	}
	
	NameDescriptor(final int nameType) {
		this.nameType = nameType;
	}

	@Override
	public String toString() {
		return "NameDescriptor [nameType=" + nameType + ", nameTypeId=" + nameTypeId + ", cpIds=" + Arrays.toString(cpIds) + "]";
	}
}
