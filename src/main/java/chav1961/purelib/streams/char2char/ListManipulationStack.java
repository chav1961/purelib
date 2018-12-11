package chav1961.purelib.streams.char2char;

import java.util.Arrays;

class ListManipulationStack {
	static final int	INITIAL_SIZE = 8;
	
	private ListType[]	stack = new ListType[INITIAL_SIZE]; 
	private int[]		counters = new int[INITIAL_SIZE];
	private int			current = -1;
	
	enum ListType {
		TYPE_UL, TYPE_OL
	}
	
	int size() {
		return current+1;
	}
	
	int count() {
		return counters[current];
	}
	
	ListType getTopType() {
		return stack[current];
	}
	
	void push(ListType topType) {
		if (++current >= stack.length) {
			stack = Arrays.copyOf(stack, 2*stack.length);  
			counters = Arrays.copyOf(counters, 2*counters.length);  
		}
		stack[current] = topType;
		counters[current] = 0;
	}
	
	ListType pop () {
		return stack[current--];
	}
	
	void inc() {
		counters[current]++;
	}

	@Override
	public String toString() {
		return "ListRepo [stack=" + Arrays.toString(stack) + ", counters=" + Arrays.toString(counters) + ", current=" + current + "]";
	}
}
