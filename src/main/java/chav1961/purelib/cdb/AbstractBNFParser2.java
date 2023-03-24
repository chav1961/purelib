package chav1961.purelib.cdb;

import java.util.Arrays;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;
import chav1961.purelib.cdb.intern.Predefines;

public abstract class AbstractBNFParser2<NodeType extends Enum<?>, Cargo> implements RuleBasedParser<NodeType, Cargo> {
	protected static final int		EOF = 0;
	protected static final int		ILLEGAL = 1;
	protected static final int		FIRST_FREE  = 2;

	private static final int		INITIAL_STACK_DEPTH = 16;
	
	protected final SyntaxTreeInterface<Cargo>	tree = new AndOrTree<>();
	protected final int[]	tempInt = new int[2];
	protected final long[]	tempLong = new long[2];
	protected final int		predefAvailableMask;
	protected int[][]		stateStack = new int[INITIAL_STACK_DEPTH][];
	protected int			prevFrom;
	protected int			lexType;
	protected long			value;
	protected int			stateStackDepth = 0;
	
	protected AbstractBNFParser2(final int predefAvailableMask) {
		this.predefAvailableMask = predefAvailableMask;
	}

	protected abstract int nextLexema(final char[] content, int from);
	protected abstract int testInternal(final char[] content, final int from, final int rule);
	
	protected void print(final String content) {
		System.err.println(content);
	}
	
	@Override
	public SyntaxTreeInterface<Cargo> getNamesTree() {
		return tree;
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
			
			try{from = nextLexema(content, from);

				if (lexType != ILLEGAL) {
					return testInternal(content, from, 0) >= 0;
				}
				else {
					return false;
				}
			} finally {
				prevFrom = from;
			}
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

	protected boolean compareTo(final char[] content, int from, final String template) {
		final int	min = Math.min(content.length-from, template.length());
		
		if (min < template.length()) {
			return false;
		}
		else {
			for (int index = 0; index < min; index++) {
				if (template.charAt(index) != content[from + index]) {
					return false;
				}
			}
			return true;
		}
	}
	
	protected int nextLexemaPredef(final char[] content, int from) {
		if (predefAvailableMask == 0) {
			lexType = ILLEGAL;
			return from;
		}
		else if (from >= content.length) {
			lexType = EOF;
			return from;
		}
		else {
			switch (content[from]) {
				case '\'' :
					if ((predefAvailableMask & (1 << Predefines.QuotedString.ordinal())) != 0) {
						from = CharUtils.parseStringExtended(content, from, '\'', CharUtils.NULL_APPENDABLE);
						if (content[from] == '\'') {
							lexType = -1 - Predefines.QuotedString.ordinal();
							return from + 1;
						}
						else {
							lexType = ILLEGAL;
							return from;
						}
					}
					else {
						lexType = ILLEGAL;
						return from;
					}
				case '\"' :
					if ((predefAvailableMask & (1 << Predefines.DoubleQuotedString.ordinal())) != 0) {
						from = CharUtils.parseStringExtended(content, from, '\"', CharUtils.NULL_APPENDABLE);
						if (content[from] == '\"') {
							lexType = -1 - Predefines.DoubleQuotedString.ordinal();
							return from + 1;
						}
						else {
							lexType = ILLEGAL;
							return from;
						}
					}
					else {
						lexType = ILLEGAL;
						return from;
					}
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					if ((predefAvailableMask & (1 << Predefines.FixedNumber.ordinal())) != 0 || (predefAvailableMask & (1 << Predefines.FloatNumber.ordinal())) != 0) {
						try{
							from = CharUtils.parseNumber(content, from, tempLong, CharUtils.PREF_ANY, true);
						} catch (SyntaxException e) {
							lexType = ILLEGAL;
							return from;
						}
						switch ((int)tempLong[1]) {
							case CharUtils.PREF_INT	: case CharUtils.PREF_LONG :
								if ((predefAvailableMask & (1 << Predefines.FixedNumber.ordinal())) != 0) {
									lexType = -1 - Predefines.FixedNumber.ordinal();
								}
								else {
									lexType = ILLEGAL;
									return from;
								}
								break;
							case CharUtils.PREF_FLOAT : case CharUtils.PREF_DOUBLE :
								lexType = -1 - Predefines.FloatNumber.ordinal();
								break;
							default :
						}
						return from;
					}
					else {
						lexType = ILLEGAL;
						return from;
					}
				default :
					if (Character.isJavaIdentifierStart(content[from])) {
						from = CharUtils.parseName(content, from, tempInt);
						
						getNamesTree().placeName(content, tempInt[0], tempInt[1], null);
						lexType = -1 - Predefines.Name.ordinal();
						return from;
					}
					else {
						lexType = ILLEGAL;
						return from;
					}
			}
		}
	}

	protected void pushCurrentState(final int from) {
		if (stateStackDepth >= stateStack.length) {
			stateStack = Arrays.copyOf(stateStack, 2 * stateStack.length);
		}
		if (stateStack[stateStackDepth] == null) {
			stateStack[stateStackDepth] = new int[3];
		}
		stateStack[stateStackDepth][0] = prevFrom;
		stateStack[stateStackDepth][1] = lexType;
		stateStack[stateStackDepth][2] = from;
		stateStackDepth++;
	}
	
	protected int popCurrentState() {
		if (stateStackDepth <= 0) {
			throw new IllegalStateException("Current state stack exhausted"); 
		}
		else {
			stateStackDepth--;
			prevFrom = stateStack[stateStackDepth][0];
			lexType = stateStack[stateStackDepth][1];
			return stateStack[stateStackDepth][2];
		}
	}

	protected int restoreCurrentState() {
		if (stateStackDepth <= 0) {
			throw new IllegalStateException("Current state stack exhausted"); 
		}
		else {
			prevFrom = stateStack[stateStackDepth - 1][0];
			lexType = stateStack[stateStackDepth - 1][1];
			return stateStack[stateStackDepth - 1][2];
		}
	}
	
	protected void removeCurrentState() {
		if (stateStackDepth <= 0) {
			throw new IllegalStateException("Current state stack exhausted"); 
		}
		else {
			stateStackDepth--;
		}
	}
	
	protected void traceLex(final char[] content, final int col) {
		final int		from = Math.max(0, col - 10), to = Math.min(content.length, col + 10), pos = col < 10 ? col : 10;
		final char[]	piece = Arrays.copyOfRange(content, from, to);
		final char[]	pointer = new char[piece.length];
		final String	message;
		
		Arrays.fill(pointer,' ');
		pointer[Math.min(col, pointer.length-1)] = '^';
		
		if (lexType >= FIRST_FREE) {
			message = "<"+lexType+">";
		}
		else if (lexType < 0) {
			message = Predefines.values()[-1 - lexType].name();
		}
		else {
			switch (lexType) {
				case EOF 	: message = "EOF"; break;
				case ILLEGAL: message = "ILLEGAL"; break;
				default 	: message = "UNKNOWN"; break;
			}
		}
		print("Point["+col+"]="+message+", char=["+((int)content[col])+"]\n"+new String(piece)+"\n"+new String(pointer));
	}
}
