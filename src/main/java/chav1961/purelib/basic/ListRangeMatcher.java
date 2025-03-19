package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.internal.PureLibLocalizer;
import chav1961.purelib.sql.SQLUtils;

class ListRangeMatcher {
	static final char	EOF = '\uFFFF'; 
	
	static IntPredicate parseIntRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		final SyntaxNode<Operation, SyntaxNode<?,?>> root = new SyntaxNode<>(0, 0, Operation.ROOT, 0, null); 
		
		parseIntLex(content, lex);
		final Lexema[]	list = lex.toArray(new Lexema[lex.size()]);
		final int		parsed = buildSyntaxTree(list, 0, Depth.OR, root);
		
		if (list[parsed].type != LexType.EOF) {
			throw new SyntaxException(0, list[parsed].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNPARSED_TAIL)); 
		}
		else {
			return new IntPredicate() {
				@Override
				public boolean test(final int value) {
					try {
						final Object	result = execute(root, value);
						
						return (result instanceof Boolean) && ((Boolean)result);
					} catch (ContentException e) {
						return false;
					}
					
				}
			};
		}
	}

	static LongPredicate parseLongRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		final SyntaxNode<Operation, SyntaxNode<?,?>> root = new SyntaxNode<>(0, 0, Operation.ROOT, 0, null); 
		
		parseIntLex(content, lex);
		final Lexema[]	list = lex.toArray(new Lexema[lex.size()]);
		final int		parsed = buildSyntaxTree(list, 0, Depth.OR, root);
		
		if (list[parsed].type != LexType.EOF) {
			throw new SyntaxException(0, list[parsed].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNPARSED_TAIL)); 
		}
		else {
			return new LongPredicate() {
				@Override
				public boolean test(final long value) {
					try {
						final Object	result = execute(root, value);
						
						return (result instanceof Boolean) && ((Boolean)result);
					} catch (ContentException e) {
						return false;
					}
					
				}
			};
		}
	}
	static DoublePredicate parseDoubleRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		final SyntaxNode<Operation, SyntaxNode<?,?>> root = new SyntaxNode<>(0, 0, Operation.ROOT, 0, null); 
		
		parseRealLex(content, lex);
		final Lexema[]	list = lex.toArray(new Lexema[lex.size()]);
		final int		parsed = buildSyntaxTree(list, 0, Depth.OR, root);
		
		if (list[parsed].type != LexType.EOF) {
			throw new SyntaxException(0, list[parsed].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNPARSED_TAIL)); 
		}
		else {
			return new DoublePredicate() {
				@Override
				public boolean test(final double value) {
					try {
						final Object	result = execute(root, value);
						
						return (result instanceof Boolean) && ((Boolean)result);
					} catch (ContentException e) {
						return false;
					}
					
				}
			};
		}
	}
	
	static <T> Predicate<T> parseRange(final char[] content) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		final SyntaxNode<Operation, SyntaxNode<?,?>> root = new SyntaxNode<>(0, 0, Operation.ROOT, 0, null); 
		
		parseStringLex(content, lex);
		final Lexema[]	list = lex.toArray(new Lexema[lex.size()]);
		final int		parsed = buildSyntaxTree(list, 0, Depth.OR, root);
		
		if (list[parsed].type != LexType.EOF) {
			throw new SyntaxException(0, list[parsed].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNPARSED_TAIL)); 
		}
		else {
			return new Predicate<T>() {
				@Override
				public boolean test(final T value) {
					try {
						final Object	result = execute(root, value);
						
						return (result instanceof Boolean) && ((Boolean)result);
					} catch (ContentException e) {
						return false;
					}
					
				}
			};
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
						throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = UnsafedCharUtils.uncheckedParseLong(content, from, forValue, false);
					if (Math.abs(from) < Integer.MAX_VALUE) {
						lex.add(new Lexema(start, (int)forValue[0]));
					}
					else {
						lex.add(new Lexema(start, forValue[0]));
					}
					break;
				default :
					throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
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
						throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = UnsafedCharUtils.uncheckedParseDouble(content, from, forValue, false);
					lex.add(new Lexema(start, forValue[0]));
					break;
				default :
					throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
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
						throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
					}
					break;
				case '\'' :
					from = UnsafedCharUtils.uncheckedParseUnescapedString(content, from+1, '\'', false, forValue);
					lex.add(new Lexema(start, new String(content, forValue[0], forValue[1]-forValue[0]+1)));
					break;
				case '\"' :
					from = UnsafedCharUtils.uncheckedParseUnescapedString(content, from+1, '\"', false, forValue);
					lex.add(new Lexema(start, new String(content, forValue[0], forValue[1]-forValue[0]+1)));
					break;
				default :
					if (Character.isLetterOrDigit(content[from])) {
						while (Character.isLetterOrDigit(content[from])) {
							from++;
						}
						lex.add(new Lexema(start, new String(content, start, from-start)));
					}
					else {
						throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
					}
			}
		}
	}

	private static int buildSyntaxTree(final Lexema[] source, int from, final Depth depth, final SyntaxNode<Operation, SyntaxNode<?, ?>> root) throws SyntaxException {
		switch (depth) {
			case OR		:
				from = buildSyntaxTree(source, from, Depth.RANGE, root);
				if (source[from].type == LexType.SPLIT) {
					final List<SyntaxNode<Operation, SyntaxNode<?, ?>>>	list = new ArrayList<>();
					SyntaxNode<Operation, SyntaxNode<?, ?>> right = (SyntaxNode<Operation, SyntaxNode<?, ?>>) root.clone();
					
					list.add(right);					
					do {
						right = (SyntaxNode<Operation, SyntaxNode<?, ?>>) root.clone();
						from = buildSyntaxTree(source, from + 1, Depth.RANGE, right);
						list.add(right);					
					} while (source[from].type == LexType.SPLIT);
					root.type = Operation.OR;
					root.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case RANGE	:
				from = buildSyntaxTree(source, from, Depth.PREFIX, root);
				if (source[from].type == LexType.RANGE) {
					final SyntaxNode<Operation, SyntaxNode<?, ?>> left = (SyntaxNode<Operation, SyntaxNode<?, ?>>) root.clone();
					final SyntaxNode<Operation, SyntaxNode<?, ?>> right = (SyntaxNode<Operation, SyntaxNode<?, ?>>) root.clone();
					
					from = buildSyntaxTree(source, from + 1, Depth.PREFIX, right);
					root.type = Operation.RANGE;
					root.children = new SyntaxNode[] {left, right};
				}
				else {
					final SyntaxNode<Operation, SyntaxNode<?, ?>> child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) root.clone();
					root.type = Operation.EQUALS;
					root.children = new SyntaxNode[] {child};
				}
				break;
			case PREFIX	:
				if (source[from].type == LexType.PLUS) {
					final SyntaxNode<Operation, SyntaxNode<?, ?>> child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) root.clone();

					from = buildSyntaxTree(source, from + 1, Depth.TERM, child);
					root.type = Operation.PLUS;
					root.cargo = child;
				}
				else if (source[from].type == LexType.MINUS) {
					final SyntaxNode<Operation, SyntaxNode<?, ?>> child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) root.clone();

					from = buildSyntaxTree(source, from + 1, Depth.TERM, child);
					root.type = Operation.MINUS;
					root.cargo = child;
				}
				else {
					from = buildSyntaxTree(source, from, Depth.TERM, root);
				}
				break;
			case TERM	:
				if (source[from].type == LexType.VALUE) {
					root.type = Operation.LOAD;
					root.cargo = source[from];
					from++;
				}
				else {
					throw new SyntaxException(0, source[from].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_OPERAND));
				}
				break;
			default :
				throw new UnsupportedOperationException("Depth level ["+depth+"] is not supported yet"); 
		}
		return from;
	}

	private static <T> Object execute(final SyntaxNode<Operation, SyntaxNode<?, ?>> node, final T value) throws ContentException, NullPointerException {
		Object	nested, nested2;
		int		classType;
		
		switch (node.getType()) {
			case LOAD	:
				final Lexema	lex = (Lexema)node.cargo;
				
				switch (lex.valueType) {
					case CompilerUtils.CLASSTYPE_REFERENCE :
						return lex.valueC;
					case CompilerUtils.CLASSTYPE_INT 	:
						return Integer.valueOf((int)lex.valueL);
					case CompilerUtils.CLASSTYPE_LONG 	:
						return Long.valueOf(lex.valueL);
					case CompilerUtils.CLASSTYPE_DOUBLE	:
						return Double.valueOf(Double.longBitsToDouble(lex.valueL));
					default :
						throw new UnsupportedOperationException("Value type ["+lex.valueType+"] is not supported yet");
				}
			case MINUS	:
				nested = execute((SyntaxNode)node.cargo, value);
				switch (classType = CompilerUtils.defineClassType(CompilerUtils.fromWrappedClass(nested.getClass()))) {
					case CompilerUtils.CLASSTYPE_REFERENCE :
						return nested;
					case CompilerUtils.CLASSTYPE_INT 	:
						return -((Integer)nested).intValue();
					case CompilerUtils.CLASSTYPE_LONG 	:
						return -((Long)nested).longValue();
					case CompilerUtils.CLASSTYPE_DOUBLE	:
						return -((Double)nested).doubleValue();
					default :
						throw new UnsupportedOperationException("Value type ["+classType+"] is not supported yet");
				}
			case PLUS	:
				nested = execute((SyntaxNode)node.cargo, value);
				switch (classType = CompilerUtils.defineClassType(CompilerUtils.fromWrappedClass(nested.getClass()))) {
					case CompilerUtils.CLASSTYPE_REFERENCE :
					case CompilerUtils.CLASSTYPE_INT 	:
					case CompilerUtils.CLASSTYPE_LONG 	:
					case CompilerUtils.CLASSTYPE_DOUBLE	:
						return nested;
					default :
						throw new UnsupportedOperationException("Value type ["+classType+"] is not supported yet");
				}
			case EQUALS	:
				nested = execute((SyntaxNode)node.children[0], value);
				switch (classType = CompilerUtils.defineClassType(CompilerUtils.fromWrappedClass(value.getClass()))) {
					case CompilerUtils.CLASSTYPE_REFERENCE :
						return value.toString().equals(nested.toString());
					case CompilerUtils.CLASSTYPE_INT 	:
						return ((Integer)value).intValue() == ((Number)SQLUtils.convert(int.class, nested)).intValue();
					case CompilerUtils.CLASSTYPE_LONG 	:
						return ((Long)value).longValue() == ((Number)SQLUtils.convert(long.class, nested)).longValue();
					case CompilerUtils.CLASSTYPE_DOUBLE	:
						return ((Double)value).doubleValue() == ((Number)SQLUtils.convert(long.class, nested)).doubleValue();
					default :
						throw new UnsupportedOperationException("Value type ["+classType+"] is not supported yet");
				}
			case RANGE	:
				nested = execute((SyntaxNode)node.children[0], value);
				nested2 = execute((SyntaxNode)node.children[1], value);
				switch (classType = CompilerUtils.defineClassType(CompilerUtils.fromWrappedClass(value.getClass()))) {
					case CompilerUtils.CLASSTYPE_REFERENCE :
				 		return value.toString().compareTo(nested.toString()) >= 0 
								&& value.toString().compareTo(nested2.toString()) <= 0;
					case CompilerUtils.CLASSTYPE_INT 	:
						return ((Integer)value).intValue() >= ((Number)SQLUtils.convert(int.class, nested)).intValue() 
								&& ((Integer)value).intValue() <= ((Number)SQLUtils.convert(int.class, nested2)).intValue(); 
					case CompilerUtils.CLASSTYPE_LONG 	:
						return ((Long)value).longValue() >= ((Number)SQLUtils.convert(long.class, nested)).longValue()
								&& ((Long)value).longValue() <= ((Number)SQLUtils.convert(long.class, nested2)).longValue();
					case CompilerUtils.CLASSTYPE_DOUBLE	:
						return ((Double)value).doubleValue() >= ((Number)SQLUtils.convert(long.class, nested)).doubleValue()
								&& ((Double)value).doubleValue() <= ((Number)SQLUtils.convert(long.class, nested2)).doubleValue();
					default :
						throw new UnsupportedOperationException("Value type ["+classType+"] is not supported yet");
				}
			case OR		:
				for(SyntaxNode item : node.children) {
					nested = execute(item, value);
					if ((nested instanceof Boolean) && ((Boolean)nested).booleanValue()) {
						return true;
					}
				}
				return false;
			case ROOT	:
			default :
				throw new UnsupportedOperationException("Operator ["+node.getType()+"] is not supported yet");
		}
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
		PLUS,
		MINUS,
		EQUALS,
		RANGE,
		OR,
		ROOT;
	}
	
	static enum Depth {
		OR,
		RANGE,
		PREFIX,
		TERM
	}
}
