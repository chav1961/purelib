package chav1961.purelib.streams.char2byte.asm;

import java.util.List;

class ByteCodePiece {
	short 				startPiece;
	short 				endPiece;
	ByteCodePiece		explicitJump;
	ByteCodePiece		naturalJump;
	List<VarChange>		vars;
	List<StackChange>	stack;
	
	static class VarChange {
		short		displ;
		short		varIndex;
		int			valType;
		long		classId;
	}
	
	static class StackChange {
		short		displ;
		short		topDelta;
		int[]		valType;
		long[]		classIds;
	}
}
