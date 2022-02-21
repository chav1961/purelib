package chav1961.purelib.cdb;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;

public abstract class AbstractBNFParser<NodeType extends Enum<?>, Cargo> implements RuleBasedParser<NodeType, Cargo>, Cloneable {
	protected static final Object[]					EMPTY_PARAMETERS = new Object[0]; 
	
	private final Class<NodeType>					clazz;
	private final SyntaxTreeInterface<Cargo>		keywords;
	private final SyntaxTreeInterface<Cargo>		names = new AndOrTree<>();
	private final SyntaxNode<NodeType, SyntaxNode> 	dummy; 
	
	protected AbstractBNFParser(final Class<NodeType> clazz, final SyntaxTreeInterface<Cargo> keywords) {
		this.clazz = clazz;
		this.keywords = keywords;
		this.dummy = new SyntaxNode<>(0,0,clazz.getEnumConstants()[0],0,null);
	}
	
	@Override
	public SyntaxTreeInterface<Cargo> getNamesTree() {
		return names;
	}

	@Override
	public int skip(final char[] content, int from, final SyntaxTreeInterface<Cargo> names) throws SyntaxException {
		return parseInternal(content, from, names, keywords, (SyntaxNode<NodeType, SyntaxNode>)dummy.clone());
	}

	@Override
	public int parse(final char[] content, int from, final SyntaxTreeInterface<Cargo> names) throws SyntaxException {
		return parseInternal(content, from, names, keywords, (SyntaxNode<NodeType, SyntaxNode>)dummy.clone());
	}

	@Override
	public int parse(final char[] content, int from, final SyntaxTreeInterface<Cargo> names, final SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException {
		return parseInternal(content, from, names, keywords, root);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	protected void throwSyntaxException(final int col, final String message, final Object[] parameters) throws SyntaxException {
		
	}
	
	static <NodeType extends Enum<?>, Cargo> int parseInternal(final char[] content, int from, final SyntaxTreeInterface<Cargo> names, final SyntaxTreeInterface<Cargo> keywords, final SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException {
		return 0;
	}
}
