package chav1961.purelib.cdb;

import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.interfaces.LexProcessor;
import chav1961.purelib.cdb.interfaces.LexemaInterface;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

/**
 * <p>This utility class contains a set of methods to walk syntax node tree</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @see SyntaxNode
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
public class SyntaxNodeUtils {
	private SyntaxNodeUtils() {}
	
	/**
	 * <p>This lambda-styled interface is used as callback for walking operations on syntax node tree</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @see SyntaxNode
	 * @since 0.0.3
	 * @param <Type> node type
	 * @param <Clazz> node class (usually {@linkplain SyntaxNode}
	 */
	@FunctionalInterface
	public interface WalkCallback<Type extends Enum<?>,Clazz extends SyntaxNode<Type,Clazz>> {
		ContinueMode process(final NodeEnterMode mode, final SyntaxNode<Type,Clazz> node);
	}
	
	/**
	 * <p>Walk down on syntax node tree</p>
	 * @param <T> node type
	 * @param <Clazz> node class type
	 * @param node current node (usually root node)
	 * @param callback callback to process nodes
	 * @return false if walking was stopped, true otherwise
	 * @see SyntaxNode
	 */
	public static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> boolean walkDown(final SyntaxNode<T,Clazz> node, final WalkCallback<T,Clazz> callback) {
		if (node == null) {
			throw new NullPointerException("Node can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Node can't be null"); 
		}
		else {
			return walkDownInternal(node,callback) != ContinueMode.STOP;
		}
	}

	/**
	 * <p>Walk down on syntax node tree</p>
	 * @param <T> node type
	 * @param <Clazz> node class type
	 * @param node current node (usually any leaf)
	 * @param callback callback to process nodes
	 * @return false if walking was stopped, true otherwise
	 * @see SyntaxNode
	 */
	public static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> boolean walkUp(final SyntaxNode<T,Clazz> node, final WalkCallback<T,Clazz> callback) {
		if (node == null) {
			throw new NullPointerException("Node can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Node callback can't be null"); 
		}
		else {
			return walkUpInternal(node,callback) != ContinueMode.STOP;
		}
	}
	
	/**
	 * <p>Build DNF by source syntax tree. Syntax tree implies keeping operands for OR and AND nodes in the 'children' field and for NOT nodes in the 'cargo' one</p>
	 * @param <T> node type
	 * @param <Clazz> node class type
	 * @param node root node. Can't be null
	 * @param orNode mark of OR node type. Can't be null
	 * @param andNode mark of AND node type. Can't be null
	 * @param notNode  mark of NOT node type. Can't be null
	 * @param callback callback to process one of the DNF records. Can't be null
	 * @return true if all DNFs were processed, false otherwise
	 * @throws NullPointerException on any parameter is null
	 */
	public static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> boolean buildDNF(final SyntaxNode<T,Clazz> node, final T orNode, final T andNode, final T notNode, final WalkCallback<T,Clazz> callback) throws NullPointerException {
		if (node == null) {
			throw new NullPointerException("Node to build DNF for can't be null"); 
		}
		else if (orNode == null) {
			throw new NullPointerException("OR node type can't be null"); 
		}
		else if (andNode == null) {
			throw new NullPointerException("AND node type can't be null"); 
		}
		else if (notNode == null) {
			throw new NullPointerException("NOT node type can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Node callback can't be null"); 
		}
		else {
			final List<DNFStack<T, Clazz>>	stack = new ArrayList<>();
			ContinueMode					total;
		
			firstBuildDNF(node, orNode, andNode, notNode, false, stack);
			do {final SyntaxNode<T,Clazz>	item = stackToNode(stack, node, andNode);
				final ContinueMode			enter = callback.process(NodeEnterMode.ENTER, item);
				final ContinueMode			exit = callback.process(NodeEnterMode.EXIT, item);
				
				total = Utils.resolveContinueMode(enter, exit);
			} while (total == ContinueMode.CONTINUE && nextBuildDNF(node, orNode, andNode, notNode, false, stack));
			
			return total == ContinueMode.CONTINUE;
		}
	}

	private static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> void firstBuildDNF(final SyntaxNode<T, Clazz> node, final T orNode, final T andNode, final T notNode, boolean notNodeDetected, final List<DNFStack<T, Clazz>> stack) {
		if (node.getType() == orNode) {	// OR - take the same first item and store it's number in the stack
			firstBuildDNF(node.children[0], orNode, andNode, notNode, notNodeDetected, stack);
			stack.add(new DNFStack<>(true, 0, node));
		}
		else if (node.getType() == andNode) {	// AND - place all items into the stack
			for (int index = 0; index < node.children.length; index++) {
				firstBuildDNF(node.children[index], orNode, andNode, notNode, notNodeDetected, stack);
			}
		}
		else if (node.getType() == notNode) {	// NOT - swap OR and AND markers and set NOT detection
			firstBuildDNF((SyntaxNode<T, Clazz>) node.cargo, andNode, orNode, notNode, !notNodeDetected, stack);
		}
		else {
			if (notNodeDetected) {	// Otherwise - place content into stack (possibly with NOT)
				final SyntaxNode<T, Clazz> not = (SyntaxNode<T, Clazz>) node.clone();
				
				not.type = notNode;
				not.cargo = node;
				stack.add(new DNFStack<>(false, -1, not));
			}
			else {
				stack.add(new DNFStack<>(false, -1, node));
			}
		}
	}

	private static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> boolean nextBuildDNF(final SyntaxNode<T, Clazz> node, final T orNode, final T andNode, final T notNode, boolean notNodeDetected, final List<DNFStack<T, Clazz>> stack) {
		if (node.getType() == orNode) {
			final DNFStack<T,Clazz>	current = stack.remove(stack.size()-1);
			
			if (current.isOrNode) {	// OR - extract OR description form stack and try to build next path
				if (nextBuildDNF(node.children[current.orIndex], orNode, andNode, notNode, notNodeDetected, stack)) {   // Nest path for the given item successful
					stack.add(current);
					return true;
				}
				else if (current.orIndex < node.children.length-1) {	// Next path for the given item failed - use next item in OR
					firstBuildDNF(node.children[++current.orIndex], orNode, andNode, notNode, notNodeDetected, stack);
					stack.add(current);
					return true;
				}
				else {
					return false;
				}
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		else if (node.getType() == andNode) {	// AND - pop stack content in inverse order.
			for (int index = node.children.length - 1; index >= 0; index--) {
				if (nextBuildDNF(node.children[index], orNode, andNode, notNode, notNodeDetected, stack)) {	// Try to build next path
					for (int forward = index+1; forward < node.children.length; forward++) {	// Next path successful - process tail of the AND list
						firstBuildDNF(node.children[forward], orNode, andNode, notNode, notNodeDetected, stack);
					}						
					return true;
				}
			}
			return false;
		}
		else if (node.getType() == notNode) {	// NOT - swap OR and AND markers and set NOT detection
			return nextBuildDNF((SyntaxNode<T, Clazz>) node.cargo, andNode, orNode, notNode, !notNodeDetected, stack);
		}
		else {	// Otherwise - remove content from stack
			stack.remove(stack.size()-1);
			return false;
		}
	}

	private static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> SyntaxNode<T, Clazz> stackToNode(final List<DNFStack<T, Clazz>> stack, final SyntaxNode<T, Clazz> node, final T andNode) {
		final List<SyntaxNode<T, Clazz>>	children = new ArrayList<>();
		final SyntaxNode<T, Clazz> 			result = (SyntaxNode<T, Clazz>) node.clone(); 
		
		for (DNFStack<T, Clazz> item : stack) {
			if (!item.isOrNode) {
				children.add(item.child);
			}
		}
		result.type = andNode;
		result.children = (Clazz[]) children.toArray(new SyntaxNode[children.size()]);
		return result;
	}

	
	@FunctionalInterface
	public interface LexProducer<LexType extends Enum<?>> {
		LexemaInterface<LexType> newLexema(int row, int col, LexType lexType, char[] content, int from, int to);
	}
	
	public static <LexType extends Enum<?>, LI extends LexemaInterface<LexType>> LexProcessor<LexType,LI> buildLexProcessor(final Class<LexType> clazz, LexProducer<LexType> lexProducer, final SimpleURLClassLoader loader) throws ContentException {
		return null;
	}
	
	private static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> ContinueMode walkDownInternal(final SyntaxNode<T,Clazz> node, final WalkCallback<T,Clazz> callback) {
		ContinueMode	rc;
		
		switch (rc = callback.process(NodeEnterMode.ENTER,node)) {
			case CONTINUE		:
loop:			for (Clazz item : node.children) {
					switch (rc = walkDownInternal(item,callback)) {
						case CONTINUE : case SIBLINGS_ONLY :
							break;
						case PARENT_ONLY : case SKIP_CHILDREN : case SKIP_PARENT : case SKIP_SIBLINGS :
							break loop;
						case STOP:
							callback.process(NodeEnterMode.EXIT,node);
							return ContinueMode.STOP;
						default: throw new UnsupportedOperationException("Continue node type ["+rc+"] is not supported yet"); 
					}
				}	
				// break not needed!!!
			case PARENT_ONLY : case SIBLINGS_ONLY : case SKIP_CHILDREN : case SKIP_PARENT :
				return callback.process(NodeEnterMode.EXIT,node);
			case SKIP_SIBLINGS	:
				return callback.process(NodeEnterMode.EXIT,node) == ContinueMode.STOP ? ContinueMode.STOP : ContinueMode.SKIP_CHILDREN;
			case STOP			:
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.STOP;
			default: throw new UnsupportedOperationException("Continue node type ["+rc+"] is not supported yet"); 
		}
	}

	private static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> ContinueMode walkUpInternal(final SyntaxNode<T,Clazz> node, final WalkCallback<T,Clazz> callback) {
		ContinueMode	rc;
		
		switch (rc = callback.process(NodeEnterMode.ENTER,node)) {
			case CONTINUE		:
				if (node.parent != null) {
					switch (rc = walkUpInternal(node.parent,callback)) {
						case CONTINUE : case SIBLINGS_ONLY : case PARENT_ONLY : case SKIP_CHILDREN :
						case SKIP_PARENT : case SKIP_SIBLINGS :
							break;
						case STOP:
							callback.process(NodeEnterMode.EXIT,node);
							return ContinueMode.STOP;
						default: throw new UnsupportedOperationException("Continue node type ["+rc+"] is not supported yet"); 
					}
					return (rc = callback.process(NodeEnterMode.EXIT,node)) == ContinueMode.STOP ? ContinueMode.STOP : rc;
				}
			case PARENT_ONLY : case SIBLINGS_ONLY : case SKIP_CHILDREN : case SKIP_PARENT : case SKIP_SIBLINGS	:
				return callback.process(NodeEnterMode.EXIT,node);
			case STOP			:
				callback.process(NodeEnterMode.EXIT,node);
				return ContinueMode.STOP;
			default: throw new UnsupportedOperationException("Continue node type ["+rc+"] is not supported yet"); 
		}
	}
	
	private static class DNFStack<T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> {
		boolean					isOrNode;
		int						orIndex;
		SyntaxNode<T, Clazz>	child;
		
		public DNFStack(final boolean isOrNode, final int orIndex, final SyntaxNode<T, Clazz> child) {
			this.isOrNode = isOrNode;
			this.orIndex = orIndex;
			this.child = child;
		}

		@Override
		public String toString() {
			return "DNFStack [isOrNode=" + isOrNode + ", orIndex=" + orIndex + ", child=" + child + "]";
		}
	}
}
