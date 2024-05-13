package chav1961.purelib.streams.char2byte.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class StackMapItem implements Cloneable {
	final StackMapItem	next;
	final int			displ;
	final int			stackDelta;
	final int[]			stackChanges;
	final int			varIndex;
	final int[]			varChanges;
	
	public StackMapItem(final StackMapItem next, int displ, int stackDelta, int[] stackChanges, int varIndex, int[] varChanges) {
		this.next = next;
		this.displ = displ;
		this.stackDelta = stackDelta;
		this.stackChanges = stackChanges;
		this.varIndex = varIndex;
		this.varChanges = varChanges;
	}
	
	public StackMapItem(final StackMapItem next, int displ, int stackDelta, int... stackChanges) {
		this.next = next;
		this.displ = displ;
		this.stackDelta = stackDelta;
		this.stackChanges = stackChanges;
		this.varIndex = -1;
		this.varChanges = null;
	}
	
	public StackMapItem(final StackMapItem next, final int displ, final int stackDelta) {
		this.next = next;
		this.displ = displ;
		this.stackDelta = stackDelta;
		this.stackChanges = null;
		this.varIndex = -1;
		this.varChanges = null;
	}

	@Override
	public String toString() {
		return "StackMapItem [next=" + next + ", displ=" + displ + ", stackDelta=" + stackDelta + ", stackChanges="
				+ Arrays.toString(stackChanges) + ", varIndex=" + varIndex + ", varChanges="
				+ Arrays.toString(varChanges) + "]";
	}
	
}
