package chav1961.purelib.cdb;

import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.CharByCharAppendable;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;
import chav1961.purelib.cdb.intern.BNFParserStack;
import chav1961.purelib.cdb.intern.EntityType;
import chav1961.purelib.cdb.intern.Predefines;

public abstract class AbstractBNFParser<NodeType extends Enum<?>, Cargo> implements RuleBasedParser<NodeType, Cargo>, Cloneable {
	protected static final Object[]					EMPTY_PARAMETERS = new Object[0];
	protected static final SyntaxNode				TEMPLATE = new SyntaxNode(0, 0, EntityType.Root, 0, null);
	private static final Appendable					NULL_APPENDABLE = new Appendable() {
														@Override public Appendable append(CharSequence csq, int start, int end) throws IOException {return this;}
														@Override public Appendable append(char c) throws IOException {return this;}
														@Override public Appendable append(CharSequence csq) throws IOException {return this;}
													};
	
	static {
		TEMPLATE.type = null;
	}
	
	protected final int[]							tempInt = new int[2];
	protected final long[]							tempLong = new long[2];
	protected final BNFParserStack<NodeType>		stack = new BNFParserStack<>();
	
	private final Class<NodeType>					clazz;
	private final SyntaxTreeInterface<NodeType>		keywords = new AndOrTree<>(1,1);
	private final SyntaxTreeInterface<Cargo>		names;
	private final SyntaxNode<NodeType, SyntaxNode> 	dummy;
	
	protected AbstractBNFParser(final Class<NodeType> clazz,final SyntaxTreeInterface<Cargo> names) {
		this.clazz = clazz;
		InternalUtils.prepareKeywordsTree(clazz, keywords);
		this.names = names;
		this.dummy = new SyntaxNode<>(0,0,clazz.getEnumConstants()[0],0,null);
		Predefines.DoubleQuotedString.allowUnnamedModuleAccess(this.getClass().getModule());
		stack.allowUnnamedModuleAccess(this.getClass().getModule());
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
	
	protected static void throwSyntaxException(final char[] content, final int col, final String message, final Object[] parameters) throws SyntaxException {
		final int		from = Math.max(0, col - 10), to = Math.min(content.length, col + 10), pos = col < 10 ? col : 10;
		final char[]	piece = Arrays.copyOfRange(content, from, to);
		final char[]	pointer = new char[piece.length];
		
		Arrays.fill(pointer,' ');
		pointer[col] = '^';
		
		throw new SyntaxException(SyntaxException.toRow(content, col), SyntaxException.toCol(content, col), message+"\n"+new String(piece)+"\n"+new String(pointer));
	}
	
	protected static boolean testPredefined(final char[] content, int from, final Predefines predefinedType, final int[] tempInt, final long[] tempLong) {
		from = CharUtils.skipBlank(content, from, true);
		
		switch (predefinedType) {
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
				throw new UnsupportedOperationException("Predefine type ["+predefinedType+"] is not supported yet"); 
		}
	}

	protected static int skipPredefined(final char[] content, int from, final Predefines predefinedType, final int[] tempInt, final long[] tempLong) throws SyntaxException {
		from = CharUtils.skipBlank(content, from, true);
		
		if (testPredefined(content, from, predefinedType, tempInt, tempLong)) {
			switch (predefinedType) {
				case DoubleQuotedString	:
					from = CharUtils.parseString(content, from + 1, '\"', NULL_APPENDABLE);
					break;
				case Empty				:
					break;
				case FixedNumber		:
					from = CharUtils.parseNumber(content, from, tempLong, CharUtils.PREF_INT | CharUtils.PREF_LONG , false);
					break;
				case FloatNumber		:
					from = CharUtils.parseNumber(content, from, tempLong, CharUtils.PREF_ANY , false);
					break;
				case Name				:
					from = CharUtils.parseName(content, from, tempInt);
					break;
				case QuotedString		:
					from = CharUtils.parseString(content, from + 1, '\'', NULL_APPENDABLE);
					break;
				default:
					throw new UnsupportedOperationException("Predefine type ["+predefinedType+"] is not supported yet"); 
			}			
		}
		return from;
	}

	protected static int parsePredefined(final char[] content, int from, final Predefines predefinedType, final SyntaxTreeInterface names, final SyntaxNode node, final int[] tempInt, final long[] tempLong) throws SyntaxException {
		from = CharUtils.skipBlank(content, from, true);
		
		if (testPredefined(content, from, predefinedType, tempInt, tempLong)) {
			switch (predefinedType) {
				case DoubleQuotedString	:
					final StringBuilder	dqsb = new StringBuilder();
					
					from = CharUtils.parseString(content, from + 1, '\"', dqsb);
					node.cargo = dqsb.toString().toCharArray();
					break;
				case Empty				:
					break;
				case FixedNumber		:
					from = CharUtils.parseNumber(content, from, tempLong, CharUtils.PREF_INT | CharUtils.PREF_LONG , true);
					node.value = tempLong[0];
					break;
				case FloatNumber		:
					from = CharUtils.parseNumber(content, from, tempLong, CharUtils.PREF_ANY, true);
					switch ((int)tempLong[1]) {
						case CharUtils.PREF_INT	: case CharUtils.PREF_LONG :
							tempLong[0] = Double.doubleToLongBits(tempLong[0]);
							break;
						case CharUtils.PREF_FLOAT	:
							tempLong[0] = Double.doubleToLongBits(Float.intBitsToFloat((int)tempLong[0]));
							break;
						case CharUtils.PREF_DOUBLE	:
							break;
						default :
							throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Unsupported numeric constant type"); 
					}
					node.value = tempLong[0];
					break;
				case Name				:
					from = CharUtils.parseName(content, from, tempInt);
					node.value = names.placeOrChangeName(content, tempInt[0], tempInt[1], null);
					break;
				case QuotedString		:
					final StringBuilder	qsb = new StringBuilder();
					
					from = CharUtils.parseString(content, from + 1, '\'', qsb);
					node.cargo = qsb.toString().toCharArray();
					break;
				default:
					throw new UnsupportedOperationException("Predefine type ["+predefinedType+"] is not supported yet"); 
			}			
			node.type = predefinedType;
		}
		return from;
	}

	protected static void moveContent2Root(final SyntaxNode[] from, final SyntaxNode to) {
		final SyntaxNode	fromNode = from[0];
		
		to.row = fromNode.row;
		to.col = fromNode.col;
		to.type = fromNode.type;
		to.value = fromNode.value;
		to.cargo = fromNode.cargo;
		to.children = fromNode.children;
	}
	
	protected static Enum<?> extractEntityType(final int entityId) {
		return EntityType.values()[entityId];
	}
	
	protected static Object checkpoint(final Object obj, final String message) {	// Debugging inside the bytecode
		System.err.println(message+obj);
		return obj;
	}
}
