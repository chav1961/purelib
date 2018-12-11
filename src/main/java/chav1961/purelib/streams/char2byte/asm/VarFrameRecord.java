package chav1961.purelib.streams.char2byte.asm;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class VarFrameRecord extends NestedEntity {
	private final SyntaxTreeInterface<?> 	tree;
	
	private short					varFrame = 0, maxVarFrame = 0;
	private VarStack				top = new VarStack(varFrame);
	
	VarFrameRecord(final SyntaxTreeInterface<?> tree) {
		this.tree = tree;
	}
	
	void processBegin() {
		final VarStack	stack = new VarStack(varFrame);
		
		stack.prev = top;
		top = stack;
	}
	
	void addVar(final short accessFlags, final long varId, final long typeId) {
		if (top.declarationsCompleted) {
			throw new IllegalStateException("Directive .var inside code. This directive must preceeds byte code commands in the '.begin' block");
		}
		else {
			top.tree.addRef((short)(varFrame+1),varId);
			top.tree.setCargo(typeId,varId);
			varFrame++;
			maxVarFrame = (short) Math.max(varFrame,maxVarFrame);
		}
	}

	void complete() {
		if (top.declarationsCompleted) {
			throw new IllegalStateException("Duplicate completion for var declarations");
		}
		else {
			top.declarationsCompleted = true;
		}
	}	
	
	boolean exists(final long varId) {
		for (VarStack actual = top; actual != null; actual = actual.prev) {
			if (actual.tree.getRef(varId) != 0) {
				return true;
			}
		}
		return false;
	}
	
	short getLocation(final long varId) throws ContentException {
		short	result;
		
		for (VarStack actual = top; actual != null; actual = actual.prev) {
			if ((result = actual.tree.getRef(varId)) != 0) {
				return (short) (result-1);
			}
		}
		throw new ContentException("Variable ["+tree.getName(varId)+"] is not declared in the method");
	}
	
	long getType(final long varId) throws ContentException {
		long	result;
		
		for (VarStack actual = top; actual != null; actual = actual.prev) {
			if ((result = actual.tree.getCargo(varId)) != 0) {
				return result;
			}
		}
		throw new ContentException("Variable ["+tree.getName(varId)+"] is not declared in the method");
	}
	
	void processEnd() {
		if (top == null) {
			throw new IllegalStateException("Stack exhausted");
		}
		else {
			varFrame = top.frameDispl;
			top.tree.clear();
			top = top.prev;
		}
	}

	short getActualVarFrameSize() {
		return varFrame;
	}
	
	void fillActualVarFrameSnapshot(byte[] shapshot) {
	}
	
	short getMaxVarFrameSize() {
		return maxVarFrame;
	}
	
	private static class VarStack {
		final LongIdTree<Long>	tree = new LongIdTree<Long>(1);
		final short				frameDispl;
		VarStack				prev;
		boolean					declarationsCompleted = false;
		
		public VarStack(short frameDispl) {
			this.frameDispl = frameDispl;
		}
	}
}
