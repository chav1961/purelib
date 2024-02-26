package chav1961.purelib.streams.char2byte.asm;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepoNew.StackSnapshot;

class MethodBody extends AbstractMethodBody {
	public static final short	STACK_CALCULATION_OPTIMISTIC = -1;
	public static final short	STACK_CALCULATION_PESSIMISTIC = -2;
	
	private static final int	INITIAL_BODY = 512;
	private static final Comparator<ItemDescriptor>	COMPARATOR = new Comparator<ItemDescriptor>(){
													@Override
													public int compare(final ItemDescriptor first, final ItemDescriptor second) {
														return (int) (first.id - second.id);
													}
										}; 

    private final SyntaxTreeInterface<?>		tree;
    private final StackAndVarRepoNew	stackAndVarNew;
    private final long					className, methodName;
    private final boolean				needVarTable;
	private final int					stackCalculationStrategy;
	private List<ItemDescriptor>		labels = new ArrayList<>();
	private List<ItemDescriptor>		brunches = new ArrayList<>(); 
	private List<StackDescriptor>		stacks = new ArrayList<>();
	private byte[]						body = new byte[0];
	private short						pc = 0, stack, maxStack;
	private long						uniqueLabel = 2;
	private boolean						labelRequired = false;
	
	MethodBody(final long className, final long methodName, final SyntaxTreeInterface<?> tree, final boolean needVarTable, final StackAndVarRepoNew stackAndVarNew){
		this(className, methodName, tree, needVarTable, STACK_CALCULATION_PESSIMISTIC, stackAndVarNew);
	}

	MethodBody(final long className, final long methodName, final SyntaxTreeInterface<?> tree, final boolean needVarTable, final short stackCalculationStrategy, final StackAndVarRepoNew stackAndVarNew){
		this.className = className;
		this.methodName = methodName;
		this.tree = tree;
		this.needVarTable = needVarTable;
		this.stackCalculationStrategy = stackCalculationStrategy;
		this.stackAndVarNew = stackAndVarNew;
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
	void putCommand(final int stackDelta, final byte... data) throws ContentException {
		if (labelRequired) {
			throw new ContentException("Dead code: previous command was jump/return/switch command, but current command has no label!");
		}
		else {
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
	}

	@Override
	void alignPC() throws ContentException {
		while ((getPC() & 0x03) != 0) {
			putCommand(0,(byte)0);
		}
	}

	@Override
	void markLabelRequired(final boolean required) {
		labelRequired = required;
	}
	
	@Override
	boolean isLabelExists(final long labelId) {
		for (ItemDescriptor item : labels) {
			if (item.id == labelId) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	void putLabel(final long id, final StackSnapshot snapshot) throws ContentException {
		final StackDescriptor		stack = findStack(id);
		StackSnapshot 				currentSnapshot = snapshot;
		
		if (labelRequired) {	// Current stack state is invalid, need use saved state
			if (stack == null) {
				throw new ContentException("Unknown program stack state: no forward brunches to the given mandatory label ["+tree.getName(id)+"] were registered earlier");
			}
			else {
				getStackAndVarRepoNew().loadStackSnapshot(currentSnapshot = stack.snapshot);
			}
		}
		if (stack != null) {
			if (!stack.snapshot.equals(currentSnapshot)) {
				throw new ContentException("Illegal forward brunch: current program stack content at the label ["+tree.getName(id)+"] differ with awaited program stack content at brunch point. "+prepareStackMismatchMessage(stack.snapshot,currentSnapshot)
										+" Forward branch is located at "+stack.brunchId+" displacement of your method code");
			}
		}
		else {
			stacks.add(new StackDescriptor(id,-1,currentSnapshot));
		}
		labelRequired = false;
		labels.add(new ItemDescriptor(id,getPC()));
	}

	@Override
	void registerBrunch(final long labelId, final boolean shortBranch, final StackSnapshot snapshot) throws ContentException {
		registerBrunch(getPC(), getPC()+1, labelId, shortBranch, snapshot);			
	}
	
	@Override
	void registerBrunch(final int address, final int placement, final long labelId, final boolean shortBranch, final StackSnapshot snapshot) throws ContentException {
		final ItemDescriptor	label = findLabel(labelId); 
		final StackDescriptor	stack = findStack(labelId); 
				
		if (label == null) {	// Forward brunch
			stacks.add(new StackDescriptor(labelId,address,snapshot));
		}
		else {					// Backward brunch
			if (stack == null || !stack.snapshot.equals(snapshot)) {
				throw new ContentException("Illegal backward brunch: program stack content at the label differ with program stack content at brunch point. "+prepareStackMismatchMessage(snapshot,stack.snapshot));
			}
		}
		brunches.add(new ItemDescriptor(labelId,address,placement,shortBranch));
	}
	
	@Override
	short getStackSize() {
		return stack;
	}
	
	@Override
	StackAndVarRepoNew getStackAndVarRepoNew() {
		return stackAndVarNew;
	}
	
	@Override
	int getCodeSize() {
		return getPC();
	}

	@Override
	int dump(final InOutGrowableByteArray os) throws IOException, ContentException {
		resolveBrunches(tree);
		final int	bodyLength = getPC();
		
		if (bodyLength > 0) {
			os.write(body, 0, bodyLength);
		}
		return pc;
	}

	private ItemDescriptor findLabel(final long labelId) {
		for (ItemDescriptor item : labels) {
			if (item.id == labelId) {
				return item;
			}
		}
		return null;
	}

	private StackDescriptor findStack(final long labelId) {
		for (StackDescriptor item : stacks) {
			if (item.id == labelId) {
				return item;
			}
		}
		return null;
	}

	private String prepareStackMismatchMessage(final StackSnapshot current, final StackSnapshot awaited) {
		return "Current stack state is: "+current.toString()+", awaited stack state is: "+awaited.toString();
	}

	
	private void resolveBrunches(final SyntaxTreeInterface<?> tree) throws ContentException {
		final ItemDescriptor[]	labelsArray = labels.toArray(new ItemDescriptor[labels.size()]);
		StringBuilder	sb = null;
		
		Arrays.sort(labelsArray,COMPARATOR);
		
loop:	for (ItemDescriptor item : brunches) {
			if (item.id != 0) {
				int low = 0, high = labelsArray.length - 1, mid, delta;

				while (low <= high) {
					mid = (low + high) >>> 1;
					ItemDescriptor midVal = labelsArray[mid];
					
					if (midVal.id < item.id) {
						low = mid + 1;
					}
					else if (midVal.id > item.id) {
						high = mid - 1;
					}
					else if (item.shortBrunch) {
						delta = midVal.displ - item.displ;
						
						if (delta < Short.MIN_VALUE || delta > Short.MAX_VALUE) {
							throw new ContentException("Too long jump: ");
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
				if (tree.getNameLength(item.id) > 0) {
					sb.append(tree.getName(item.id)).append(' ');
				}
			}
		}
		if (sb != null) {
			final String	clazz = tree.getName(className), method = tree.getName(methodName);
			
//			final PrintWriter	pw = new PrintWriter(System.err);
//			((AndOrTree)tree).print(pw);
//			pw.flush();
			
			throw new ContentException("Class ["+clazz+"], method ["+method+"] - unresolved jumps: labels {"+sb.toString()+"} are not defined in the method body!");
		}
	}
	
	private static class ItemDescriptor {
		long	id;
		int		displ;
		int		placement;
		boolean	shortBrunch;

		ItemDescriptor(long id, int displ) {
			this(id,displ,displ+1,false);
			
		}		
		
		ItemDescriptor(long id, int displ, int placement, boolean shortBrunch) {
			this.id = id;	this.displ = displ;
			this.placement = placement;
			this.shortBrunch = shortBrunch;
		}
		
		@Override
		public String toString() {
			return "ItemDescriptor [id=" + id + ", displ=" + displ + ", placement=" + placement + ", shortBrunch=" + shortBrunch + "]";
		}
	}

	private static class StackDescriptor {
		long			id;
		long			brunchId;
		StackSnapshot	snapshot;

		StackDescriptor(long id, long brunchId, StackSnapshot snapshot) {
			this.id = id;
			this.brunchId = brunchId;
			this.snapshot = snapshot;
		}

		@Override
		public String toString() {
			return "StackDescriptor [id=" + id + ", snapshot=" + snapshot + "]";
		}
	}
}