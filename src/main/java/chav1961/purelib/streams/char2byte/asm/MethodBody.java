package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class MethodBody extends AbstractMethodBody {
	public static final short	STACK_CALCULATION_OPTIMISTIC = -1;
	public static final short	STACK_CALCULATION_PESSIMISTIC = -2;
	
	private static final int	INITIAL_REFS = 32;
	private static final int	INITIAL_BODY = 512;
	private static final ItemDescriptor	EMPTY_DESCRIPTOR = new ItemDescriptor(0,0);
	private static final Comparator<ItemDescriptor>	COMPARATOR = new Comparator<ItemDescriptor>(){
													@Override
													public int compare(final ItemDescriptor first, final ItemDescriptor second) {
														return (int) (first.id - second.id);
													}
										}; 

    private final SyntaxTreeInterface<?>		tree;										
	private final int				stackCalculationStrategy;
	private ItemDescriptor[]		labels = new ItemDescriptor[0];
	private int						labelsCursor = 0;
	private ItemDescriptor[]		brunches = new ItemDescriptor[0]; 
	private int						brunchesCursor = 0;
	private byte[]					body = new byte[0];
	private short					pc = 0, stack, maxStack;
	private long					uniqueLabel = 2;
	
	MethodBody(final SyntaxTreeInterface<?> tree){
		this(tree,STACK_CALCULATION_PESSIMISTIC);
	}

	MethodBody(final SyntaxTreeInterface<?> tree, final short stackCalculationStrategy){
		this.tree = tree;
		this.stackCalculationStrategy = stackCalculationStrategy;
		if (stackCalculationStrategy >= 0) {
			stack = maxStack = stackCalculationStrategy; 
		}
	}

	@Override
	long getUniqueLabelId() {
		return uniqueLabel += 16;
	}
	
	@Override
	short getPC() {return pc;}
	
	@Override
	void putCommand(final int stackDelta, final byte... data) {
		final int	len = data.length;
		
		if (data.length == 0) {
			throw new IllegalArgumentException("Attempt to add empty command (data.length == 0)!");
		}
		else {
			while (pc + len >= body.length) {
				body = Arrays.copyOf(body,body.length+INITIAL_BODY);
			}
			System.arraycopy(data,0,body,pc,len);
			
			switch (stackCalculationStrategy) {
				case STACK_CALCULATION_OPTIMISTIC	: 
					maxStack = (short) Math.max(maxStack,stack += stackDelta);
					break;
				case STACK_CALCULATION_PESSIMISTIC	:
					if (stackDelta > 0) {
						maxStack = (short) Math.max(maxStack,stack += stackDelta);
					}
					break;
			}
			pc += len;
		}
	}

	@Override
	void alignPC() {
		while ((getPC() & 0x03) != 0) {
			putCommand(0,(byte)0);
		}
	}

	@Override
	void putLabel(final long id) {
		if (labelsCursor >= labels.length) {
			labels = expandItems(labels); 
		}
		labels[labelsCursor++] = new ItemDescriptor(id,getPC()); 
	}

	@Override
	void registerBrunch(final long labelId, final boolean shortBranch) {
		registerBrunch(getPC(),getPC()+1,labelId,shortBranch);			
	}
	
	@Override
	void registerBrunch(final int address, final int placement, final long labelId, final boolean shortBranch) {
		if (brunchesCursor >= brunches.length) {
			brunches = expandItems(brunches); 
		}
		brunches[brunchesCursor++] = new ItemDescriptor(labelId,address,placement,shortBranch); 
	}

	@Override
	short getStackSize() {
		return stack;
	}
	
	@Override
	int getCodeSize() {
		return getPC();
	}

	@Override
	int dump(final OutputStream os) throws IOException {
		resolveBrunches(tree);
		os.write(body, 0, getPC());
		return pc;
	}
	
	private void resolveBrunches(final SyntaxTreeInterface<?> tree) throws AsmSyntaxException {
		StringBuilder	sb = null;
		
		Arrays.sort(labels,COMPARATOR);
		
loop:	for (ItemDescriptor item : brunches) {
			if (item.id != 0) {
				int low = 0, high = labels.length - 1, mid, delta;

				while (low <= high) {
					mid = (low + high) >>> 1;
					ItemDescriptor midVal = labels[mid];
					
					if (midVal.id < item.id) {
						low = mid + 1;
					}
					else if (midVal.id > item.id) {
						high = mid - 1;
					}
					else if (item.shortBrunch) {
						delta = midVal.displ - item.displ;
						
						if (delta < Short.MIN_VALUE || delta > Short.MAX_VALUE) {
							throw new AsmSyntaxException("Too long jump: ");
						}
						else {
							body[item.placement] = (byte) ((delta >> 8) & 0xFF);
							body[item.placement+1] = (byte) ((delta) & 0xFF);
							continue loop;
						}
					}
					else {
						delta = midVal.displ - item.displ;
						
						body[item.placement] = (byte) ((delta >> 24) & 0xFF);
						body[item.placement+1] = (byte) ((delta >> 16) & 0xFF);
						body[item.placement+2] = (byte) ((delta >> 8) & 0xFF);
						body[item.placement+3] = (byte) (delta & 0xFF);
						continue loop;
					}
				}
				if (sb == null) {
					sb = new StringBuilder();
				}
				if (tree.getNameLength(item.id) >= 0) {
					sb.append(tree.getName(item.id)).append(' ');
				}
			}
		}
		if (sb != null) {
			throw new AsmSyntaxException("Unresolved jumps: labels {"+sb.toString()+"} are not defined in the method body!");
		}
	}
	
	private ItemDescriptor[] expandItems(final ItemDescriptor[] source) {
		final int				len = source.length, maxLen = len + INITIAL_REFS;
		final ItemDescriptor[]	result = Arrays.copyOf(source,maxLen);
		
		for (int index = len; index < maxLen; index++) {
			result[index] = EMPTY_DESCRIPTOR; 
		}
		return result;
	}

	private static class ItemDescriptor {
		long	id;
		int		displ;
		int		placement;
		boolean	shortBrunch;

		public ItemDescriptor(long id, int displ) {
			this(id,displ,displ+1,false);
			
		}		
		public ItemDescriptor(long id, int displ, int placement, boolean shortBrunch) {
			this.id = id;	this.displ = displ;
			this.placement = placement;
			this.shortBrunch = shortBrunch;
		}
		@Override
		public String toString() {
			return "ItemDescriptor [id=" + id + ", displ=" + displ + ", placement=" + placement + ", shortBrunch=" + shortBrunch + "]";
		}
	}

}