package chav1961.purelib.streams.char2char.intern;

import java.util.Arrays;

public class ListManipulationStack {
	static final int	INITIAL_SIZE = 8;
	
	private ListType[]	stack = new ListType[INITIAL_SIZE]; 
	private int[]		counters = new int[INITIAL_SIZE];
	private int			current = -1;
	
	public enum ListType {
		TYPE_UL, TYPE_OL
	}
	
	public int size() {
		return current+1;
	}
	
	public int count() {
		return counters[current];
	}
	
	public ListType getTopType() {
		return stack[current];
	}
	
	public void push(ListType topType) {
		if (++current >= stack.length) {
			stack = Arrays.copyOf(stack, 2*stack.length);  
			counters = Arrays.copyOf(counters, 2*counters.length);  
		}
		stack[current] = topType;
		counters[current] = 0;
	}
	
	public ListType pop () {
		return stack[current--];
	}
	
	public void inc() {
		counters[current]++;
	}

	@Override
	public String toString() {
		return "ListRepo [stack=" + Arrays.toString(stack) + ", counters=" + Arrays.toString(counters) + ", current=" + current + "]";
	}
}
