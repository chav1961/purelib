package chav1961.purelib.basic;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;

abstract class SimpleAbstractCalculator<LexType extends Enum<?>, NodeType extends SyntaxNode<?,?>> {
	protected final Class<LexType>	lexClazz;
	
	protected SimpleAbstractCalculator(Class<LexType> lexClazz) {
		this.lexClazz = lexClazz;
	}
	
	public Object calculate(final char[] expression, final int from) throws SyntaxException, ContentException {
		final SyntaxNode<LexType, NodeType> root = new SyntaxNode(0, 0, lexClazz.getEnumConstants()[0], 0, null, null);
		
		compile(expression, from, root);
		return calculate(root);
	}

	public int compile(final char[] expression, final int from, final SyntaxNode<LexType, NodeType> root) throws SyntaxException {
		return 0;
	}
	
	public Object calculate(final SyntaxNode<LexType, NodeType> root) throws ContentException {
		return null;
	}

	protected abstract Object extractVariable(final SyntaxNode<LexType, NodeType> item) throws ContentException;
	
	protected abstract Object extractFieldItem(final Object owner, final SyntaxNode<LexType, NodeType> item) throws ContentException;

	protected abstract Object extractArrayItem(final Object owner, final int itemIndex) throws ContentException;

	protected abstract Object invokeMethod(final SyntaxNode<LexType, NodeType> item, final Object owner, final Object... parameters) throws ContentException;
}
