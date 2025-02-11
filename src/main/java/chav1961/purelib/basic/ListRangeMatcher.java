package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.SyntaxNode;

class ListRangeMatcher {
	static final char	EOF = '\uFFFF'; 
	
	static IntPredicate parseIntRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		
		parseIntLex(content, lex);
		
		return null;
	}

	static LongPredicate parseLongRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		final SyntaxNode<Operation, SyntaxNode<?,?>> root = new SyntaxNode<>(0, 0, Operation.ROOT, 0, null); 
		
		parseIntLex(content, lex);
		final Lexema[]	list = lex.toArray(new Lexema[lex.size()]);
		final int		parsed = buildSyntaxTree(list, 0, Depth.OR, root);
		
		if (list[parsed].type != LexType.EOF) {
			throw new SyntaxException(0, list[parsed].pos, "unparsed tail"); 
		}
		else {
			return null;
		}
	}
	static DoublePredicate parseDoubleRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		final SyntaxNode<Operation, SyntaxNode<?,?>> root = new SyntaxNode<>(0, 0, Operation.ROOT, 0, null); 
		
		parseRealLex(content, lex);
		final Lexema[]	list = lex.toArray(new Lexema[lex.size()]);
		final int		parsed = buildSyntaxTree(list, 0, Depth.OR, root);
		
		if (list[parsed].type != LexType.EOF) {
			throw new SyntaxException(0, list[parsed].pos, "unparsed tail"); 
		}
		else {
			return null;
		}
	}
	
	static Predicate<?> parseRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		final SyntaxNode<Operation, SyntaxNode<?,?>> root = new SyntaxNode<>(0, 0, Operation.ROOT, 0, null); 
		
		parseStringLex(content, lex);
		final Lexema[]	list = lex.toArray(new Lexema[lex.size()]);
		final int		parsed = buildSyntaxTree(list, 0, Depth.OR, root);
		
		if (list[parsed].type != LexType.EOF) {
			throw new SyntaxException(0, list[parsed].pos, "unparsed tail"); 
		}
		else {
			return null;
		}
	}
	
	private static void parseIntLex(final char[] content, final List<Lexema> lex) throws SyntaxException {
		final long[]	forValue = new long[1];
		int	from = 0;
		
loop:	for(;;) {
			while (content[from] <= ' ' && content[from] != EOF) {
				from++;
			}
			final int	start = from;
			
			switch (content[from]) {
				case EOF :
					lex.add(new Lexema(start, LexType.EOF));
					break loop;
				case '+' :
					lex.add(new Lexema(start, LexType.PLUS));
					from++;
					break;
				case '-' :
					lex.add(new Lexema(start, LexType.MINUS));
					from++;
					break;
				case ',' :
					lex.add(new Lexema(start, LexType.SPLIT));
					from++;
					break;
				case '.' :
					if (content[from+1] == '.') {
						lex.add(new Lexema(start, LexType.RANGE));
						from += 2;
					}
					else {
						throw new SyntaxException(0, start, "unknown lex");
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = UnsafedCharUtils.uncheckedParseLong(content, from, forValue, false);
					lex.add(new Lexema(start, forValue[0]));
					break;
				default :
					throw new SyntaxException(0, start, "unknown lex");
			}
		}
	}

	private static void parseRealLex(final char[] content, final List<Lexema> lex) throws SyntaxException {
		final double[]	forValue = new double[1];
		int	from = 0;
		
loop:	for(;;) {
			while (content[from] <= ' ' && content[from] != EOF) {
				from++;
			}
			final int	start = from;
			
			switch (content[from]) {
				case EOF :
					lex.add(new Lexema(start, LexType.EOF));
					break loop;
				case '+' :
					lex.add(new Lexema(start, LexType.PLUS));
					from++;
					break;
				case '-' :
					lex.add(new Lexema(start, LexType.MINUS));
					from++;
					break;
				case ',' :
					lex.add(new Lexema(start, LexType.SPLIT));
					from++;
					break;
				case '.' :
					if (content[from+1] == '.') {
						lex.add(new Lexema(start, LexType.RANGE));
						from += 2;
					}
					else {
						throw new SyntaxException(0, start, "unknown lex");
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = UnsafedCharUtils.uncheckedParseDouble(content, from, forValue, false);
					lex.add(new Lexema(start, forValue[0]));
					break;
				default :
					throw new SyntaxException(0, start, "unknown lex");
			}
		}
	}

	
	private static void parseStringLex(char[] content, List<Lexema> lex) throws SyntaxException {
		final int[]	forValue = new int[2];
		int	from = 0;
		
loop:	for(;;) {
			while (content[from] <= ' ' && content[from] != EOF) {
				from++;
			}
			final int	start = from;
			
			switch (content[from]) {
				case EOF :
					lex.add(new Lexema(start, LexType.EOF));
					break loop;
				case '+' :
					lex.add(new Lexema(start, LexType.PLUS));
					from++;
					break;
				case '-' :
					lex.add(new Lexema(start, LexType.MINUS));
					from++;
					break;
				case ',' :
					lex.add(new Lexema(start, LexType.SPLIT));
					from++;
					break;
				case '.' :
					if (content[from+1] == '.') {
						lex.add(new Lexema(start, LexType.RANGE));
						from += 2;
					}
					else {
						throw new SyntaxException(0, start, "unknown lex");
					}
					break;
				case '\'' :
					from = UnsafedCharUtils.uncheckedParseUnescapedString(content, from+1, '\'', false, forValue);
					lex.add(new Lexema(start, new String(content, start+1, from-start-1)));
					break;
				default :
					throw new SyntaxException(0, start, "unknown lex");
			}
		}
	}

	private static int buildSyntaxTree(Lexema[] array, int from, Depth or, SyntaxNode<Operation, SyntaxNode<?, ?>> root) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	static enum LexType {
		VALUE,
		PLUS,
		MINUS,
		RANGE,
		SPLIT,
		EOF
	}

	static class Lexema {
		final int		pos;
		final LexType	type;
		final int		valueType;
		final long		valueL;
		final String	valueC;
		
		Lexema(final int pos, final LexType type) {
			this(pos, type, CompilerUtils.CLASSTYPE_VOID, 0, null);
		}

		Lexema(final int pos, final int value) {
			this(pos, LexType.VALUE, CompilerUtils.CLASSTYPE_INT, value, null);
		}

		Lexema(final int pos, final long value) {
			this(pos, LexType.VALUE, CompilerUtils.CLASSTYPE_LONG, value, null);
		}

		Lexema(final int pos, final double value) {
			this(pos, LexType.VALUE, CompilerUtils.CLASSTYPE_DOUBLE, Double.doubleToLongBits(value), null);
		}

		Lexema(final int pos, final String value) {
			this(pos, LexType.VALUE, CompilerUtils.CLASSTYPE_REFERENCE, 0, value);
		}
		
		private Lexema(final int pos, final LexType type, final int valueType, final long valueL, final String valueC) {
			this.pos = pos;
			this.type = type;
			this.valueType = valueType;
			this.valueL = valueL;
			this.valueC = valueC;
		}
	}
	
	static enum Operation {
		LOAD,
		EQUALS,
		RANGE,
		OR,
		ROOT
	}
	
	static enum Depth {
		OR,
		RANGE,
		PREFIX,
		TERM
	}
}
