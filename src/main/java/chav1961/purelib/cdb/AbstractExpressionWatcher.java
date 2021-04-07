package chav1961.purelib.cdb;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public abstract class AbstractExpressionWatcher<LexType extends Enum<?>, Node extends SyntaxNode<LexType,?>> {
	public interface WatcherGetter<LexType extends Enum<?>, Node extends SyntaxNode<LexType,?>> {
		Object extractVariable(Node item) throws ContentException;
		Object extractField(Node item, Object value) throws ContentException;
		Object extractIndex(Node item, Object value, int index) throws ContentException;
	}
	
	protected final Class<LexType> 	lexClass;
	
	protected AbstractExpressionWatcher(Class<LexType> lexClass) {
		this.lexClass = lexClass;
	}
	
	public Object calculate(final char[] expression, final int from, final WatcherGetter<LexType,Node> wg) throws SyntaxException, ContentException, NullPointerException, IllegalArgumentException {
		final Node	root = (Node) new SyntaxNode<LexType, Node>(0, 0, lexClass.getEnumConstants()[0], 0, null, (Node[])null);
		
		parse(expression, from, root);		
		return calculate(root,wg);
	}
	
	protected abstract Object calculate(Node root, WatcherGetter<LexType,Node> wg) throws ContentException, NullPointerException, IllegalArgumentException;
	protected abstract int parse(char[] expression, int from, Node root) throws SyntaxException, NullPointerException, IllegalArgumentException;
}
