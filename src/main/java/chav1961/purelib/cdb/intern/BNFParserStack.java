package chav1961.purelib.cdb.intern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.nanoservice.InternalUtils;

public class BNFParserStack<NodeType extends Enum<?>> implements ModuleAccessor {
	private static final SyntaxNode[]		EMPTY_ARRAY = new SyntaxNode[0]; 
	
	private final List<List<SyntaxNode>>	stack = new ArrayList<>();
	
	public BNFParserStack() {
	}
	
	@Override
	public void allowUnnamedModuleAccess(Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
	
	public void push() {
		System.err.println("Stack push");
		stack.add(0, new ArrayList<>());
	}
	
	public void add(final SyntaxNode<NodeType,SyntaxNode> entity) {
		if (entity == null) {
			throw new NullPointerException("Entity to add can't be null"); 
		}
		else {
			System.err.println("Stack add: "+entity);
			printSyntaxNode(entity);
			stack.get(0).add(entity);
		}
	}
	
	public SyntaxNode[] pop() {
		if (stack.isEmpty()) {
			throw new IllegalStateException("Stack exhausted");
		}
		else {
			final List<SyntaxNode>	result = stack.remove(0);
			int		count = 0;
			
			for (SyntaxNode item : result) {
				if (item.type != null) {
					count += calculateStackSize(item);
				}
			}
	
			System.err.println("Before stack pop...");
			if (count > 0) {
				final SyntaxNode[]	returned = new SyntaxNode[count]; 
				
				count = 0;
				for (SyntaxNode item : result) {
					if (item.type != null) {
						System.err.println("Stack extract: "+item);
						count = fillStackContent(returned,count,item);
						printSyntaxNode(item);
					}
					else {
						System.err.println("Stack skip: "+item);
						printSyntaxNode(item);
					}
				}
				System.err.println("Stack pop: "+Arrays.toString(returned));
				return returned;
			}
			else {
				System.err.println("Stack pop empty...");
				return EMPTY_ARRAY;
			}
		}
	}

	private int calculateStackSize(final SyntaxNode node) {
		if (node.type == null) {
			return 0;
		}
		else if ((node.type instanceof EntityType) && (node.children != null)) {
			int	count = 1;
			
			for (SyntaxNode item : node.children) {
				count += calculateStackSize(item);
			}
			return count;
		} else {
			return 1;
		}
	}

	private int fillStackContent(final SyntaxNode[] returned, int count, final SyntaxNode node) {
		// TODO Auto-generated method stub
		if (node.type != null) {
			if ((node.type instanceof EntityType) && (node.children != null)) {
				for (SyntaxNode item : node.children) {
					count = fillStackContent(returned, count, item);
				}
				return count;
			}
			else {
				returned[count] = node;
				return count + 1;
			}
		}
		else {
			return 0;
		}
	}
	
	private void printSyntaxNode(final SyntaxNode node) {
		printSyntaxNode("",node);
	}
	
	private void printSyntaxNode(final String prefix, final SyntaxNode node) {
		if (node != null) {
			System.err.print(prefix+">");
			if (node.type != null) {
				System.err.print(node.type);
			}
			if (node.value != 0) {
				System.err.print(" value="+node.value);
			}
			if (node.cargo != null) {
				System.err.print(" cargo="+node.cargo);
			}
			System.err.println();
			if (node.children != null) {
				for (SyntaxNode item : node.children) {
					printSyntaxNode(prefix+"  ", item);
				}
			}
		}
	}

}
