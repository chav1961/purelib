package chav1961.purelib.cdb;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class SyntaxNodeUtils {
	@FunctionalInterface
	public interface WalkCallback<Type extends Enum<?>,Clazz extends SyntaxNode<Type,Clazz>> {
		ContinueMode process(final NodeEnterMode mode, final SyntaxNode<Type,Clazz> node);
	}
	
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

	public static <T extends Enum<?>,Clazz extends SyntaxNode<T,Clazz>> boolean walkUp(final SyntaxNode<T,Clazz> node, final WalkCallback<T,Clazz> callback) {
		if (node == null) {
			throw new NullPointerException("Node can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Node can't be null"); 
		}
		else {
			return walkUpInternal(node,callback) != ContinueMode.STOP;
		}
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
}
