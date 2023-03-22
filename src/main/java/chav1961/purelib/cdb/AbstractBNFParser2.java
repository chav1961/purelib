package chav1961.purelib.cdb;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;

public abstract class AbstractBNFParser2<NodeType extends Enum<?>, Cargo> implements RuleBasedParser<NodeType, Cargo> {
	protected static final int		EOF = 0;
	
	protected int	prevFrom;
	protected int	lexType;
	protected long	value;
	
	public AbstractBNFParser2() {
	}

	protected abstract int nextLexema(final char[] content, int from);
	protected boolean testInternal(final char[] content, final int from) {return true;}
	
	
	@Override
	public SyntaxTreeInterface<Cargo> getNamesTree() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean test(final char[] content, int from) throws SyntaxException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content array can't be null or empty");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1));
		}
		else {
			prevFrom = from;
			from = nextLexema(content, from);
			
			final boolean	result = testInternal(content, from);
			
			prevFrom = from;
			return result;
		}
	}


	@Override
	public int skip(final char[] content, int from) throws SyntaxException {
		// TODO Auto-generated method stub
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content array can't be null or empty");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1));
		}
		else {
			return 0;
		}
	}

	@Override
	public int parse(final char[] content, int from, SyntaxTreeInterface<Cargo> names) throws SyntaxException {
		// TODO Auto-generated method stub
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content array can't be null or empty");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (names == null) {
			throw new NullPointerException("Names tree can't be null");
		}
		else {
			return 0;
		}
	}

	@Override
	public int parse(final char[] content, int from, final SyntaxTreeInterface<Cargo> names, final SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException {
		// TODO Auto-generated method stub
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content array can't be null or empty");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (names == null) {
			throw new NullPointerException("Names tree can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Node root can't be null");
		}
		else {
			return 0;
		}
	}

	protected int skipBlank(final char[] content, int from) {
		return CharUtils.skipBlank(content, from, true);
	}

	protected int nextLexemaPredef(final char[] content, int from) {
		return 0;
	}

	protected void traceLex(final char[] content, int from) {
	}
}
