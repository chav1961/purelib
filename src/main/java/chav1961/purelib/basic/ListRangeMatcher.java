package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.CompilerUtils;

class ListRangeMatcher {
	static final char	EOF = '\uFFFF'; 
	
	static IntPredicate parseIntRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		
		parseIntLex(content, lex);
		
		return null;
	}

	static LongPredicate parseLongRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		
		parseIntLex(content, lex);
		
		return null;
	}

	static DoublePredicate parseDoubleRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		
		parseRealLex(content, lex);
		
		return null;
	}
	
	static Predicate<?> parseRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		
		parseStringLex(content, lex);
		
		return null;
	}
	
	private static void parseIntLex(char[] content, List<Lexema> lex) {
		// TODO Auto-generated method stub
		
	}

	private static void parseRealLex(char[] content, List<Lexema> lex) {
		// TODO Auto-generated method stub
		
	}

	
	private static void parseStringLex(char[] content, List<Lexema> lex) {
		// TODO Auto-generated method stub
		
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
		OR
	}
}
