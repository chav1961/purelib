package chav1961.purelib.cdb;

import java.io.IOException;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;
import chav1961.purelib.cdb.intern.Predefines;

public abstract class AbstractBNFParser<NodeType extends Enum<?>, Cargo> implements RuleBasedParser<NodeType, Cargo>, Cloneable {
	protected static final Object[]					EMPTY_PARAMETERS = new Object[0];
	private static final Appendable					NULL_APPENDABLE = new Appendable() {
														@Override public Appendable append(CharSequence csq, int start, int end) throws IOException {return this;}
														@Override public Appendable append(char c) throws IOException {return this;}
														@Override public Appendable append(CharSequence csq) throws IOException {return this;}
													};
	
	protected final int[]							tempInt = new int[2];
	protected final long[]							tempLong = new long[2];
	
	private final Class<NodeType>					clazz;
	private final SyntaxTreeInterface<NodeType>		keywords;
	private final SyntaxTreeInterface<Cargo>		names = new AndOrTree<>();
	private final SyntaxNode<NodeType, SyntaxNode> 	dummy;
	
	protected AbstractBNFParser(final Class<NodeType> clazz, final SyntaxTreeInterface<NodeType> keywords) {
		this.clazz = clazz;
		this.keywords = keywords;
		this.dummy = new SyntaxNode<>(0,0,clazz.getEnumConstants()[0],0,null);
	}

	protected abstract <NodeType extends Enum<?>, Cargo> int parseInternal(final char[] content, int from, final SyntaxTreeInterface<Cargo> names, final SyntaxTreeInterface<NodeType> keywords, final SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException;
	protected abstract <NodeType extends Enum<?>, Cargo> int skipInternal(final char[] content, int from, final SyntaxTreeInterface<NodeType> keywords) throws SyntaxException;
	
	@Override
	public SyntaxTreeInterface<Cargo> getNamesTree() {
		return names;
	}

	@Override
	public int skip(final char[] content, int from) throws SyntaxException {
		return skipInternal(content, from, keywords);
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
	
	protected static void throwSyntaxException(final int col, final String message, final Object[] parameters) throws SyntaxException {
		throw new SyntaxException(0, col, message);
	}
	
	protected static boolean testPredefined(final char[] content, int from, final Predefines predefinedName, final int[] tempInt, final long[] tempLong) {
		from = CharUtils.skipBlank(content, from, true);
		
		switch (predefinedName) {
			case DoubleQuotedString	:
				if (content[from] == '\"') {
					try{CharUtils.parseString(content, from, '\"', NULL_APPENDABLE);
						return true;
					} catch (IllegalArgumentException exc) {
						return false;
					}
				}
				else {
					return false;
				}
			case Empty				:
				return true;
			case FixedNumber : case FloatNumber	:
				return Character.isDigit(content[from]);
			case Name				:
				return Character.isJavaIdentifierStart(content[from]);
			case QuotedString		:
				if (content[from] == '\'') {
					try{CharUtils.parseString(content, from, '\'', NULL_APPENDABLE);
						return true;
					} catch (IllegalArgumentException exc) {
						return false;
					}
				}
				else {
					return false;
				}
			default :
				throw new UnsupportedOperationException("Predefine type ["+predefinedName+"] is not supported yet"); 
		}
	}

	protected static int skipPredefined(final char[] content, int from, final Predefines predefinedType, final int[] tempInt, final long[] tempLong) {
		if (testPredefined(content, from, predefinedType, tempInt, tempLong)) {
			final int[]	forNames = new int[2];
			from = CharUtils.parseName(content, from, forNames);
		}
		return from;
	}

	protected static int parsePredefined(final char[] content, int from, final Predefines predefinedType, final SyntaxNode node, final int[] tempInt, final long[] tempLong) {
		if (testPredefined(content, from, predefinedType, tempInt, tempLong)) {
			final int[]	forNames = new int[2];
			from = CharUtils.parseName(content, from, forNames);
		}
		return from;
	}
}
