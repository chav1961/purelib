package chav1961.purelib.cdb.intern;

import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.cdb.SyntaxNode;

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
		stack.add(0, new ArrayList<>());
	}
	
	public void add(final SyntaxNode<NodeType,SyntaxNode> entity) {
		if (entity == null) {
			throw new NullPointerException("Entity to add can't be null"); 
		}
		else {
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
					count++;
				}
			}
	
			if (count > 0) {
				final SyntaxNode[]	returned = new SyntaxNode[count]; 
				
				count = 0;
				for (SyntaxNode item : result) {
					if (item.type != null) {
						returned[count++] = item;
					}
				}
				return returned;
			}
			else {
				return EMPTY_ARRAY;
			}
		}
	}
}
